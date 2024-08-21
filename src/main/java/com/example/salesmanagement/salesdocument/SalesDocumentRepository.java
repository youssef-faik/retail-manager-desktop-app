package com.example.salesmanagement.salesdocument;

import com.example.salesmanagement.HibernateUtil;
import com.example.salesmanagement.configuration.AppConfiguration;
import com.example.salesmanagement.configuration.ConfigKey;
import jakarta.persistence.Entity;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SalesDocumentRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static <T extends SalesDocument> List<T> findAll(Class<T> salesDocumentClass) {
        List<T> salesDocuments;

        try (Session session = sessionFactory.openSession()) {
            List<T> documents =
                    session
                            .createQuery("select S from " + salesDocumentClass.getAnnotation(Entity.class).name() + " S", salesDocumentClass)
                            .list();

            salesDocuments = new ArrayList<>(documents);
        }

        return salesDocuments;
    }

    static Optional<SalesDocument> findById(Long id) {

        try (Session session = sessionFactory.openSession()) {
            SalesDocument salesDocument = session.find(SalesDocument.class, id);
            session.refresh(salesDocument);

            return Optional.ofNullable(salesDocument);
        }
    }

    static Optional<SalesDocument> add(SalesDocument salesDocument) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            salesDocument = session.merge(salesDocument);

            // Process the sales document within the same transaction
            processSalesDocument(salesDocument, session);

            session.getTransaction().commit();

            return Optional.ofNullable(salesDocument);
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

    private static void processSalesDocument(SalesDocument salesDocument, Session session) {
        try {
            AppConfiguration configuration = AppConfiguration.getInstance();
            ConfigKey nextSalesDocumentNumber = getNextSalesDocumentNumberKey(salesDocument);

            if (nextSalesDocumentNumber != null && !isDraftStatus(salesDocument)) {
                long lastSalesDocumentNumber = Long.parseLong(configuration.getConfigurationValue(nextSalesDocumentNumber).getValue());
                salesDocument.setReference(lastSalesDocumentNumber);

                session.persist(salesDocument);

                lastSalesDocumentNumber++;
                configuration.setConfigurationValues(Map.of(nextSalesDocumentNumber, String.valueOf(lastSalesDocumentNumber)));
            }

        } catch (Exception e) {
            // If any exception occurs, it will propagate back to the caller, which will trigger a rollback
            throw e;
        }
    }

    private static ConfigKey getNextSalesDocumentNumberKey(SalesDocument salesDocument) {
        if (salesDocument instanceof Quotation) {
            return ConfigKey.NEXT_QUOTATION_NUMBER;
        } else if (salesDocument instanceof DeliveryNote) {
            return ConfigKey.NEXT_DELIVERY_NOTE_NUMBER;
        } else if (salesDocument instanceof Invoice) {
            return ConfigKey.NEXT_INVOICE_NUMBER;
        } else if (salesDocument instanceof CreditInvoice) {
            return ConfigKey.NEXT_CREDIT_INVOICE_NUMBER;
        }
        return null;
    }

    private static boolean isDraftStatus(SalesDocument salesDocument) {
        if (salesDocument instanceof Quotation quotation) {
            return quotation.getStatus() == QuotationStatus.DRAFT;
        } else if (salesDocument instanceof DeliveryNote deliveryNote) {
            return deliveryNote.getStatus() == DeliveryNoteStatus.DRAFT;
        } else if (salesDocument instanceof Invoice invoice) {
            return invoice.getStatus() == InvoiceStatus.DRAFT;
        } else if (salesDocument instanceof CreditInvoice creditInvoice) {
            return creditInvoice.getStatus() == CreditInvoiceStatus.DRAFT;
        }
        return false;
    }

    static Optional<SalesDocument> update(SalesDocument updatedSalesDocument) {
        Session session = sessionFactory.openSession();

        try {
            SalesDocument originalSalesDocument = session.find(SalesDocument.class, updatedSalesDocument.getId());

            if (originalSalesDocument == null) {
                return Optional.empty();
            }

            session.beginTransaction();

            updateCommonFields(originalSalesDocument, updatedSalesDocument);
            updateItems(originalSalesDocument, updatedSalesDocument, session);

            if (originalSalesDocument instanceof Quotation) {
                updateQuotation((Quotation) originalSalesDocument, (Quotation) updatedSalesDocument);
            } else if (originalSalesDocument instanceof DeliveryNote) {
                updateDeliveryNote((DeliveryNote) originalSalesDocument, (DeliveryNote) updatedSalesDocument);
            } else if (originalSalesDocument instanceof Invoice) {
                updateInvoice((Invoice) originalSalesDocument, (Invoice) updatedSalesDocument, session);
            } else if (originalSalesDocument instanceof CreditInvoice) {
                updateCreditInvoice((CreditInvoice) originalSalesDocument, (CreditInvoice) updatedSalesDocument, session);
            }

            session.merge(originalSalesDocument);
            session.getTransaction().commit();

            return Optional.of(originalSalesDocument);
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

    private static void updateCommonFields(SalesDocument original, SalesDocument updated) {
        original.setIssueDate(updated.getIssueDate());
        original.setClient(updated.getClient());
        original.setTotalExcludingTaxes(updated.getTotalExcludingTaxes());
        original.setTotalTaxes(updated.getTotalTaxes());
        original.setTotalIncludingTaxes(updated.getTotalIncludingTaxes());
    }

    private static void updateItems(SalesDocument original, SalesDocument updated, Session session) {
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
        QuotationStatus originalStatus = original.getStatus();

        original.setValidUntil(updated.getValidUntil());
        original.setStatus(updated.getStatus());

        if (originalStatus == QuotationStatus.DRAFT && updated.getStatus() != QuotationStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_QUOTATION_NUMBER);
        }
    }

    private static void updateDeliveryNote(DeliveryNote original, DeliveryNote updated) {
        DeliveryNoteStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());

        if (originalStatus == DeliveryNoteStatus.DRAFT && updated.getStatus() != DeliveryNoteStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_DELIVERY_NOTE_NUMBER);
        }
    }

    private static void updateInvoice(Invoice original, Invoice updated, Session session) {
        InvoiceStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());
        original.setDueDate(updated.getDueDate());
        original.setPaidAmount(updated.getPaidAmount());

        updatePayments(original, updated, session);

        if (originalStatus == InvoiceStatus.DRAFT && updated.getStatus() != InvoiceStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_INVOICE_NUMBER);
        }
    }

    private static void updateCreditInvoice(CreditInvoice original, CreditInvoice updated, Session session) {
        CreditInvoiceStatus originalStatus = original.getStatus();

        original.setStatus(updated.getStatus());
        original.setPaidAmount(updated.getPaidAmount());

        updatePayments(original, updated, session);

        if (originalStatus == CreditInvoiceStatus.DRAFT && updated.getStatus() != CreditInvoiceStatus.DRAFT) {
            assignNewReference(original, ConfigKey.NEXT_CREDIT_INVOICE_NUMBER);
        }
    }

    private static <T extends SalesDocument> void updatePayments(T original, T updated, Session session) {
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

    private static void assignNewReference(SalesDocument salesDocument, ConfigKey configKey) {
        AppConfiguration configuration = AppConfiguration.getInstance();
        long lastSalesDocumentNumber = Long.parseLong(configuration.getConfigurationValue(configKey).getValue());

        salesDocument.setReference(lastSalesDocumentNumber);

        lastSalesDocumentNumber++;
        configuration.setConfigurationValues(Map.of(configKey, String.valueOf(lastSalesDocumentNumber)));
    }

    static boolean deleteById(Long id) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            SalesDocument deletedSalesDocument = session.find(SalesDocument.class, id);

            if (deletedSalesDocument == null) {
                return false;
            }

            session.remove(deletedSalesDocument);
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

    static Long count(Class<? extends SalesDocument> salesDocumentClass) {
        Long count;

        try (Session session = sessionFactory.openSession()) {
            String query = "SELECT COUNT(*) count_salesDocument FROM " + salesDocumentClass.getAnnotation(Entity.class).name();
            Query<Long> nativeQuery = session.createQuery(query, long.class);
            count = nativeQuery.getSingleResult();
        }

        return count;
    }

    static Optional<SalesDocument> findFirstByOrderByIdDesc(Class<? extends SalesDocument> salesDocumentClass) {
        try (Session session = sessionFactory.openSession()) {
            String query = "SELECT I FROM SalesDocument I WHERE TYPE(I) = :type AND I.reference is not null ORDER BY I.reference DESC LIMIT 1";
            Query<? extends SalesDocument> nativeQuery = session.createQuery(query, salesDocumentClass);
            nativeQuery.setParameter("type", salesDocumentClass);

            return Optional.of(nativeQuery.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
