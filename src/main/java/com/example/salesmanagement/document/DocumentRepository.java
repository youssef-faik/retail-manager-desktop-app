package com.example.salesmanagement.document;

import com.example.salesmanagement.HibernateUtil;
import com.example.salesmanagement.configuration.AppConfiguration;
import com.example.salesmanagement.configuration.ConfigKey;
import com.example.salesmanagement.stockmouvement.StockMouvement;
import com.example.salesmanagement.stockmouvement.StockMovementRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface DocumentRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static <T extends Document> List<T> findAll(Class<T> documentClass) {
        List<T> documents;

        try (Session session = sessionFactory.openSession()) {
            List<T> documentsList =
                    session
                            .createQuery("select S from " + documentClass.getAnnotation(Entity.class).name() + " S", documentClass)
                            .list();

            documents = new ArrayList<>(documentsList);
        }

        return documents;
    }

    static Optional<Document> findById(Long id) {

        try (Session session = sessionFactory.openSession()) {
            Document document = session.find(Document.class, id);
            session.refresh(document);

            return Optional.ofNullable(document);
        }
    }

    static Optional<Document> add(Document document) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            document = session.merge(document);

            // Process the sales document within the same transaction
            processDocument(document, session);

            if (document instanceof PurchaseDeliveryNote purchaseDeliveryNote
                    && purchaseDeliveryNote.getStatus() != PurchaseDeliveryNoteStatus.DRAFT) {
                saveStockMouvements(document, session);
            }

            if (document instanceof DeliveryNote deliveryNote && deliveryNote.getStatus() != DeliveryNoteStatus.DRAFT) {
                saveStockMouvements(document, session);
            }

            if (document instanceof Invoice invoice && invoice.getStatus() != InvoiceStatus.DRAFT) {
                saveStockMouvements(document, session);
            }

            if (document instanceof CreditInvoice creditInvoice && creditInvoice.getStatus() != CreditInvoiceStatus.DRAFT) {
                saveStockMouvements(document, session);
            }

            session.getTransaction().commit();

            return Optional.ofNullable(document);
        } catch (Exception e) {
            e.printStackTrace();

            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE
                    || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK) {
                session.getTransaction().rollback();
            }

            return Optional.empty();
        } finally {
            session.close();
        }
    }

    private static void processDocument(Document document, Session session) {
        try {
            AppConfiguration configuration = AppConfiguration.getInstance();
            ConfigKey nextDocumentNumber = getNextDocumentNumberKey(document);

            if (nextDocumentNumber != null && !isDraftStatus(document)) {
                long lastDocumentNumber = Long.parseLong(configuration.getConfigurationValue(nextDocumentNumber).getValue());
                document.setReference(lastDocumentNumber);

                session.persist(document);

                lastDocumentNumber++;
                configuration.setConfigurationValues(Map.of(nextDocumentNumber, String.valueOf(lastDocumentNumber)));
            }

        } catch (Exception e) {
            // If any exception occurs, it will propagate back to the caller, which will trigger a rollback
            throw e;
        }
    }

    private static ConfigKey getNextDocumentNumberKey(Document document) {
        if (document instanceof PurchaseOrder) {
            return ConfigKey.NEXT_PURCHASE_ORDER_NUMBER;
        } else if (document instanceof PurchaseDeliveryNote) {
            return ConfigKey.NEXT_PURCHASE_DELIVERY_NOTE_NUMBER;
        } else if (document instanceof Quotation) {
            return ConfigKey.NEXT_QUOTATION_NUMBER;
        } else if (document instanceof DeliveryNote) {
            return ConfigKey.NEXT_DELIVERY_NOTE_NUMBER;
        } else if (document instanceof Invoice) {
            return ConfigKey.NEXT_INVOICE_NUMBER;
        } else if (document instanceof CreditInvoice) {
            return ConfigKey.NEXT_CREDIT_INVOICE_NUMBER;
        }
        return null;
    }

    private static boolean isDraftStatus(Document document) {
        if (document instanceof PurchaseOrder purchaseOrder) {
            return purchaseOrder.getStatus() == PurchaseOrderStatus.DRAFT;
        } else if (document instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
            return purchaseDeliveryNote.getStatus() == PurchaseDeliveryNoteStatus.DRAFT;
        } else if (document instanceof Quotation quotation) {
            return quotation.getStatus() == QuotationStatus.DRAFT;
        } else if (document instanceof DeliveryNote deliveryNote) {
            return deliveryNote.getStatus() == DeliveryNoteStatus.DRAFT;
        } else if (document instanceof Invoice invoice) {
            return invoice.getStatus() == InvoiceStatus.DRAFT;
        } else if (document instanceof CreditInvoice creditInvoice) {
            return creditInvoice.getStatus() == CreditInvoiceStatus.DRAFT;
        }
        return false;
    }

    static Optional<Document> update(Document updatedDocument) {
        Session session = sessionFactory.openSession();

        try {
            Document originalDocument = session.find(Document.class, updatedDocument.getId());

            if (originalDocument == null) {
                return Optional.empty();
            }

            session.beginTransaction();

            updateCommonFields(originalDocument, updatedDocument);
            updateItems(originalDocument, updatedDocument, session);

            if (originalDocument instanceof PurchaseOrder purchaseOrder) {
                updatePurchaseOrder(purchaseOrder, (PurchaseOrder) updatedDocument);
            } else if (originalDocument instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
                updatePurchaseDeliveryNote(purchaseDeliveryNote, (PurchaseDeliveryNote) updatedDocument, session);
            } else if (originalDocument instanceof Quotation) {
                updateQuotation((Quotation) originalDocument, (Quotation) updatedDocument);
            } else if (originalDocument instanceof DeliveryNote) {
                updateDeliveryNote((DeliveryNote) originalDocument, (DeliveryNote) updatedDocument, session);
            } else if (originalDocument instanceof Invoice) {
                updateInvoice((Invoice) originalDocument, (Invoice) updatedDocument, session);
            } else if (originalDocument instanceof CreditInvoice) {
                updateCreditInvoice((CreditInvoice) originalDocument, (CreditInvoice) updatedDocument, session);
            }

            session.merge(originalDocument);
            session.getTransaction().commit();

            return Optional.of(originalDocument);
        } catch (Exception e) {
            e.printStackTrace();

            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE
                    || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK) {
                session.getTransaction().rollback();
            }
            return Optional.empty();
        } finally {
            session.close();
        }
    }

    private static void updateCommonFields(Document original, Document updated) {
        original.setIssueDate(updated.getIssueDate());
        original.setTotalExcludingTaxes(updated.getTotalExcludingTaxes());
        original.setTotalTaxes(updated.getTotalTaxes());
        original.setTotalIncludingTaxes(updated.getTotalIncludingTaxes());
    }

    private static void updateItems(Document original, Document updated, Session session) {
        updated.getItems().forEach(
                updatedItem -> {
                    if (original.getItems().contains(updatedItem)) {
                        original.updateItem(updatedItem);
                    } else {
                        original.addItem(updatedItem);
                    }
                }
        );

        original.getItems().forEach(
                originalItem -> {
                    if (!updated.getItems().contains(originalItem)) {
                        original.removeItem(originalItem);
                        session.remove(originalItem);
                    }
                }
        );
    }

    private static void updateQuotation(Quotation original, Quotation updated) {
        original.setClient(updated.getClient());
        QuotationStatus originalStatus = original.getStatus();

        original.setValidUntil(updated.getValidUntil());
        original.setStatus(updated.getStatus());

        if (originalStatus == QuotationStatus.DRAFT && updated.getStatus() != QuotationStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_QUOTATION_NUMBER);
        }
    }

    private static void updatePurchaseOrder(PurchaseOrder original, PurchaseOrder updated) {
        original.setSupplier(updated.getSupplier());
        PurchaseOrderStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());

        if (originalStatus == PurchaseOrderStatus.DRAFT && updated.getStatus() != PurchaseOrderStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_PURCHASE_ORDER_NUMBER);
        }
    }

    private static void updatePurchaseDeliveryNote(PurchaseDeliveryNote original, PurchaseDeliveryNote updated, Session session) {
        original.setSupplier(updated.getSupplier());
        PurchaseDeliveryNoteStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());

        List<StockMouvement> stockMouvements;

        if (originalStatus == PurchaseDeliveryNoteStatus.DRAFT && updated.getStatus() != PurchaseDeliveryNoteStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_PURCHASE_DELIVERY_NOTE_NUMBER);
            stockMouvements = saveStockMouvements(original, session);
        } else {
            stockMouvements = StockMovementRepository.findAllByDocument(original);
        }

        AtomicReference<LocalDateTime> stockEntryMouvementDateTime = new AtomicReference<>(LocalDateTime.now());

        if (!stockMouvements.isEmpty()) {
            stockMouvements.stream().findFirst().ifPresent(mouvement -> stockEntryMouvementDateTime.set(mouvement.getDateTime()));
        }

        if (updated.getStatus() != PurchaseDeliveryNoteStatus.DRAFT) {
            original.getItems().forEach(item ->
                    stockMouvements
                            .stream()
                            .filter(stockMouvement -> stockMouvement.getProduct().equals(item.getProduct()))
                            .findFirst()
                            .ifPresentOrElse(
                                    stockMouvement -> {
                                        stockMouvement.setQuantity(item.getQuantity());
                                        session.merge(stockMouvement);
                                    },
                                    () -> {
                                        //create new stock mouvement
                                        StockMouvement stockEntryMouvement = StockMouvement.createStockEntryMouvement(item.getProduct(), item.getQuantity(), original);
                                        stockEntryMouvement.setDateTime(stockEntryMouvementDateTime.get());
                                        session.persist(stockEntryMouvement);

                                        stockMouvements.add(stockEntryMouvement);
                                    }
                            ));


            List<StockMouvement> stockMouvementsToRemove = new ArrayList<>();

            stockMouvements.forEach(stockMouvement -> {
                boolean noneMatch = original.getItems()
                        .stream()
                        .noneMatch(item -> stockMouvement.getProduct().equals(item.getProduct()));

                if (noneMatch) {
                    // mark items for delete
                    stockMouvementsToRemove.add(stockMouvement);
                }
            });

            stockMouvementsToRemove.stream().map(session::merge).forEach(session::remove);

            stockMouvements.removeAll(stockMouvementsToRemove);
        }


        // X -> CANCELLED
        if ((originalStatus != PurchaseDeliveryNoteStatus.CANCELLED && originalStatus != PurchaseDeliveryNoteStatus.DRAFT)
                && updated.getStatus() == PurchaseDeliveryNoteStatus.CANCELLED) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(true);
                session.merge(stockMouvement);
            });
        }

        // CANCELLED -> X
        if (originalStatus == PurchaseDeliveryNoteStatus.CANCELLED
                && (updated.getStatus() != PurchaseDeliveryNoteStatus.DRAFT && updated.getStatus() != PurchaseDeliveryNoteStatus.CANCELLED)) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(false);
                session.merge(stockMouvement);
            });
        }
    }

    private static void updateDeliveryNote(DeliveryNote original, DeliveryNote updated, Session session) {
        original.setClient(updated.getClient());
        DeliveryNoteStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());

        List<StockMouvement> stockMouvements;

        if (originalStatus == DeliveryNoteStatus.DRAFT && updated.getStatus() != DeliveryNoteStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_DELIVERY_NOTE_NUMBER);
            stockMouvements = saveStockMouvements(original, session);
        } else {
            stockMouvements = StockMovementRepository.findAllByDocument(original);
        }

        AtomicReference<LocalDateTime> stockEntryMouvementDateTime = new AtomicReference<>(LocalDateTime.now());

        if (!stockMouvements.isEmpty()) {
            stockMouvements.stream().findFirst().ifPresent(mouvement -> stockEntryMouvementDateTime.set(mouvement.getDateTime()));
        }

        if (updated.getStatus() != DeliveryNoteStatus.DRAFT) {
            original.getItems().forEach(item ->
                    stockMouvements
                            .stream()
                            .filter(stockMouvement -> stockMouvement.getProduct().equals(item.getProduct()))
                            .findFirst()
                            .ifPresentOrElse(
                                    stockMouvement -> {
                                        stockMouvement.setQuantity(item.getQuantity());
                                        session.merge(stockMouvement);
                                    },
                                    () -> {
                                        //create new stock mouvement
                                        StockMouvement stockEntryMouvement = StockMouvement.createStockEntryMouvement(item.getProduct(), item.getQuantity(), original);
                                        stockEntryMouvement.setDateTime(stockEntryMouvementDateTime.get());
                                        session.persist(stockEntryMouvement);

                                        stockMouvements.add(stockEntryMouvement);
                                    }
                            ));


            List<StockMouvement> stockMouvementsToRemove = new ArrayList<>();

            stockMouvements.forEach(stockMouvement -> {
                boolean noneMatch = original.getItems()
                        .stream()
                        .noneMatch(item -> stockMouvement.getProduct().equals(item.getProduct()));

                if (noneMatch) {
                    // mark items for delete
                    stockMouvementsToRemove.add(stockMouvement);
                }
            });

            stockMouvementsToRemove.stream().map(session::merge).forEach(session::remove);

            stockMouvements.removeAll(stockMouvementsToRemove);
        }

        if ((originalStatus != DeliveryNoteStatus.CANCELLED && originalStatus != DeliveryNoteStatus.DRAFT)
                && updated.getStatus() == DeliveryNoteStatus.CANCELLED) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(true);
                session.merge(stockMouvement);
            });
        }

        if (originalStatus == DeliveryNoteStatus.CANCELLED
                && (updated.getStatus() != DeliveryNoteStatus.DRAFT && updated.getStatus() != DeliveryNoteStatus.CANCELLED)) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(false);
                session.merge(stockMouvement);
            });
        }
    }

    private static void updateInvoice(Invoice original, Invoice updated, Session session) {
        original.setClient(updated.getClient());
        InvoiceStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());
        original.setDueDate(updated.getDueDate());
        original.setPaidAmount(updated.getPaidAmount());

        updatePayments(original, updated, session);

        List<StockMouvement> stockMouvements;

        if (originalStatus == InvoiceStatus.DRAFT && updated.getStatus() != InvoiceStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_INVOICE_NUMBER);
            stockMouvements = saveStockMouvements(original, session);
        } else {
            stockMouvements = StockMovementRepository.findAllByDocument(original);
        }

        AtomicReference<LocalDateTime> stockEntryMouvementDateTime = new AtomicReference<>(LocalDateTime.now());

        if (!stockMouvements.isEmpty()) {
            stockMouvements.stream().findFirst().ifPresent(mouvement -> stockEntryMouvementDateTime.set(mouvement.getDateTime()));
        }

        if (updated.getStatus() != InvoiceStatus.DRAFT) {
            original.getItems().forEach(item ->
                    stockMouvements
                            .stream()
                            .filter(stockMouvement -> stockMouvement.getProduct().equals(item.getProduct()))
                            .findFirst()
                            .ifPresentOrElse(
                                    stockMouvement -> {
                                        stockMouvement.setQuantity(item.getQuantity());
                                        session.merge(stockMouvement);
                                    },
                                    () -> {
                                        //create new stock mouvement
                                        StockMouvement stockEntryMouvement = StockMouvement.createStockEntryMouvement(item.getProduct(), item.getQuantity(), original);
                                        stockEntryMouvement.setDateTime(stockEntryMouvementDateTime.get());
                                        session.persist(stockEntryMouvement);

                                        stockMouvements.add(stockEntryMouvement);
                                    }
                            ));


            List<StockMouvement> stockMouvementsToRemove = new ArrayList<>();

            stockMouvements.forEach(stockMouvement -> {
                boolean noneMatch = original.getItems()
                        .stream()
                        .noneMatch(item -> stockMouvement.getProduct().equals(item.getProduct()));

                if (noneMatch) {
                    // mark items for delete
                    stockMouvementsToRemove.add(stockMouvement);
                }
            });

            stockMouvementsToRemove.stream().map(session::merge).forEach(session::remove);

            stockMouvements.removeAll(stockMouvementsToRemove);
        }


        if ((originalStatus != InvoiceStatus.CANCELLED && originalStatus != InvoiceStatus.DRAFT)
                && updated.getStatus() == InvoiceStatus.CANCELLED) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(true);
                session.merge(stockMouvement);
            });
        }

        if (originalStatus == InvoiceStatus.CANCELLED
                && (updated.getStatus() != InvoiceStatus.DRAFT && updated.getStatus() != InvoiceStatus.CANCELLED)) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(false);
                session.merge(stockMouvement);
            });
        }
    }

    private static void updateCreditInvoice(CreditInvoice original, CreditInvoice updated, Session session) {
        original.setClient(updated.getClient());
        CreditInvoiceStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());
        original.setPaidAmount(updated.getPaidAmount());

        updatePayments(original, updated, session);
        List<StockMouvement> stockMouvements;

        if (originalStatus == CreditInvoiceStatus.DRAFT && updated.getStatus() != CreditInvoiceStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_CREDIT_INVOICE_NUMBER);
            stockMouvements = saveStockMouvements(original, session);
        } else {
            stockMouvements = StockMovementRepository.findAllByDocument(original);
        }

        AtomicReference<LocalDateTime> stockEntryMouvementDateTime = new AtomicReference<>(LocalDateTime.now());

        if (!stockMouvements.isEmpty()) {
            stockMouvements.stream().findFirst().ifPresent(mouvement -> stockEntryMouvementDateTime.set(mouvement.getDateTime()));
        }

        if (updated.getStatus() != CreditInvoiceStatus.DRAFT) {
            original.getItems().forEach(item ->
                    stockMouvements
                            .stream()
                            .filter(stockMouvement -> stockMouvement.getProduct().equals(item.getProduct()))
                            .findFirst()
                            .ifPresentOrElse(
                                    stockMouvement -> {
                                        stockMouvement.setQuantity(item.getQuantity());
                                        session.merge(stockMouvement);
                                    },
                                    () -> {
                                        //create new stock mouvement
                                        StockMouvement stockEntryMouvement = StockMouvement.createStockEntryMouvement(item.getProduct(), item.getQuantity(), original);
                                        stockEntryMouvement.setDateTime(stockEntryMouvementDateTime.get());
                                        session.persist(stockEntryMouvement);

                                        stockMouvements.add(stockEntryMouvement);
                                    }
                            ));

            List<StockMouvement> stockMouvementsToRemove = new ArrayList<>();

            stockMouvements.forEach(stockMouvement -> {
                boolean noneMatch = original.getItems()
                        .stream()
                        .noneMatch(item -> stockMouvement.getProduct().equals(item.getProduct()));

                if (noneMatch) {
                    // mark items for delete
                    stockMouvementsToRemove.add(stockMouvement);
                }
            });

            stockMouvementsToRemove.stream().map(session::merge).forEach(session::remove);

            stockMouvements.removeAll(stockMouvementsToRemove);
        }

        if ((originalStatus != CreditInvoiceStatus.CANCELLED && originalStatus != CreditInvoiceStatus.DRAFT)
                && updated.getStatus() == CreditInvoiceStatus.CANCELLED) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(true);
                session.merge(stockMouvement);
            });
        }

        if (originalStatus == CreditInvoiceStatus.CANCELLED
                && (updated.getStatus() != CreditInvoiceStatus.DRAFT && updated.getStatus() != CreditInvoiceStatus.CANCELLED)) {
            stockMouvements.forEach(stockMouvement -> {
                stockMouvement.setCanceled(false);
                session.merge(stockMouvement);
            });
        }
    }

    private static <T extends Document> void updatePayments(T original, T updated, Session session) {
        if (updated instanceof Invoice updatedDocument && original instanceof Invoice originalDocument) {
            updatedDocument.getPayments().forEach(
                    updatedPayment -> {
                        if (originalDocument.getPayments().contains(updatedPayment)) {
                            originalDocument.updatePayment(updatedPayment);
                        } else {
                            originalDocument.addPayment(updatedPayment);
                        }
                    }
            );

            originalDocument.getPayments().forEach(
                    originalPayment -> {
                        if (!updatedDocument.getPayments().contains(originalPayment)) {
                            originalDocument.removePayment(originalPayment);
                            session.remove(originalPayment);
                        }
                    }
            );
        }

        if (updated instanceof CreditInvoice updatedDocument && original instanceof CreditInvoice originalDocument) {
            updatedDocument.getPayments().forEach(
                    updatedPayment -> {
                        if (originalDocument.getPayments().contains(updatedPayment)) {
                            originalDocument.updatePayment(updatedPayment);
                        } else {
                            originalDocument.addPayment(updatedPayment);
                        }
                    }
            );

            originalDocument.getPayments().forEach(
                    originalPayment -> {
                        if (!updatedDocument.getPayments().contains(originalPayment)) {
                            originalDocument.removePayment(originalPayment);
                            session.remove(originalPayment);
                        }
                    }
            );
        }
    }

    private static void assignNewReference(Document document, ConfigKey configKey) {
        AppConfiguration configuration = AppConfiguration.getInstance();
        long lastDocumentNumber = Long.parseLong(configuration.getConfigurationValue(configKey).getValue());

        document.setReference(lastDocumentNumber);

        lastDocumentNumber++;
        configuration.setConfigurationValues(Map.of(configKey, String.valueOf(lastDocumentNumber)));
    }

    static boolean deleteById(Long id) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            Document deletedDocument = session.find(Document.class, id);

            if (deletedDocument == null) {
                return false;
            }

            session.remove(deletedDocument);
            session.getTransaction().commit();

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE
                    || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK) {
                session.getTransaction().rollback();
            }
            return false;
        } finally {
            session.close();
        }
    }

    static Long count(Class<? extends Document> documentClass) {
        Long count;

        try (Session session = sessionFactory.openSession()) {
            String query = "SELECT COUNT(*) count_document FROM " + documentClass.getAnnotation(Entity.class).name();
            Query<Long> nativeQuery = session.createQuery(query, long.class);
            count = nativeQuery.getSingleResult();
        }

        return count;
    }

    static Optional<Document> findFirstByOrderByIdDesc(Class<? extends Document> documentClass) {
        try (Session session = sessionFactory.openSession()) {
            String query = "SELECT D FROM Document D WHERE TYPE(D) = :type AND D.reference is not null ORDER BY D.reference DESC LIMIT 1";
            Query<? extends Document> nativeQuery = session.createQuery(query, documentClass);
            nativeQuery.setParameter("type", documentClass);

            return Optional.of(nativeQuery.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private static List<StockMouvement> saveStockMouvements(Document document, Session session) {
        List<StockMouvement> stockMouvements = new ArrayList<>();

        if (document instanceof PurchaseDeliveryNote || document instanceof CreditInvoice) {
            document.getItems()
                    .forEach(item -> {
                        stockMouvements.add(StockMouvement.createStockEntryMouvement(item.getProduct(), item.getQuantity(), document));
                    });
        }

        if (document instanceof DeliveryNote || document instanceof Invoice) {
            document.getItems()
                    .forEach(item -> {
                        stockMouvements.add(StockMouvement.createOutOfStockMouvement(item.getProduct(), item.getQuantity(), document));
                    });
        }

        try {
            stockMouvements.forEach(session::persist);
        } catch (Exception e) {
            // If any exception occurs, it will propagate back to the caller, which will trigger a rollback
            throw e;
        }

        return stockMouvements;
    }
}
