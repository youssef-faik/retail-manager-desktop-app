package com.example.salesmanagement.invoice;

import com.example.salesmanagement.HibernateUtil;
import com.example.salesmanagement.configuration.AppConfiguration;
import com.example.salesmanagement.configuration.ConfigKey;
import jakarta.persistence.NoResultException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Map;
import java.util.Optional;

public interface InvoiceRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<Invoice> findAll() {
        ObservableList<Invoice> invoices = FXCollections.observableArrayList();

        try (Session session = sessionFactory.openSession()) {
            invoices.addAll(session.createQuery("select c from Invoice c", Invoice.class).list());
        }

        return invoices;
    }

    static Optional<Invoice> findById(Long id) {

        try (Session session = sessionFactory.openSession()) {
            Invoice invoice = session.find(Invoice.class, id);
            session.refresh(invoice);

            return Optional.ofNullable(invoice);
        }
    }

    static boolean add(Invoice invoice) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            invoice = session.merge(invoice);
            if (invoice.getStatus() != InvoiceStatus.DRAFT) {
                AppConfiguration configuration = AppConfiguration.getInstance();

                long lastInvoiceNumber = Long.parseLong(configuration.getConfigurationValue(ConfigKey.NEXT_INVOICE_NUMBER).getValue());
                invoice.setReference(lastInvoiceNumber);

                session.persist(invoice);

                lastInvoiceNumber++;
                configuration.setConfigurationValues(Map.of(ConfigKey.NEXT_INVOICE_NUMBER, String.valueOf(lastInvoiceNumber)));
            } else {
                session.persist(invoice);
            }

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

    static boolean update(Invoice updatedInvoice) {
        Session session = sessionFactory.openSession();

        try {
            Invoice originalInvoice = session.find(Invoice.class, updatedInvoice.getId());

            if (originalInvoice == null) {
                return false;
            }

            session.beginTransaction();
            InvoiceStatus originalInvoiceStatus = originalInvoice.getStatus();

            originalInvoice.setStatus(updatedInvoice.getStatus());
            originalInvoice.setIssueDate(updatedInvoice.getIssueDate());
            originalInvoice.setDueDate(updatedInvoice.getDueDate());
            originalInvoice.setClient(updatedInvoice.getClient());

            originalInvoice.setTotalExcludingTaxes(updatedInvoice.getTotalExcludingTaxes());
            originalInvoice.setTotalTaxes(updatedInvoice.getTotalTaxes());
            originalInvoice.setTotalIncludingTaxes(updatedInvoice.getTotalIncludingTaxes());
            originalInvoice.setPaidAmount(updatedInvoice.getPaidAmount());

            // update invoice items
            updatedInvoice.getInvoiceItems().forEach(
                    updatedInvoiceItem -> {
                        if (originalInvoice.getInvoiceItems().contains(updatedInvoiceItem)) {
                            originalInvoice.updateInvoiceItem(updatedInvoiceItem);
                        } else {
                            originalInvoice.addInvoiceItem(updatedInvoiceItem);
                        }
                    }
            );

            originalInvoice.getInvoiceItems().forEach(
                    originalInvoiceItem -> {
                        if (!updatedInvoice.getInvoiceItems().contains(originalInvoiceItem)) {
                            originalInvoice.removeInvoiceItem(originalInvoiceItem);
                            session.remove(originalInvoiceItem);
                        }
                    }
            );

            // update payments
            updatedInvoice.getPayments().forEach(
                    updatedpayment -> {
                        if (originalInvoice.getPayments().contains(updatedpayment)) {
                            originalInvoice.updatePayment(updatedpayment);
                        } else {
                            originalInvoice.addPayment(updatedpayment);
                        }
                    }
            );

            originalInvoice.getPayments().forEach(
                    originalPayment -> {
                        if (!updatedInvoice.getPayments().contains(originalPayment)) {
                            originalInvoice.removePayment(originalPayment);
                            session.remove(originalPayment);
                        }
                    }
            );

            if (originalInvoiceStatus == InvoiceStatus.DRAFT
                    && updatedInvoice.getStatus() != InvoiceStatus.DRAFT
            ) {
                AppConfiguration configuration = AppConfiguration.getInstance();
                long lastInvoiceNumber = Long.parseLong(configuration.getConfigurationValue(ConfigKey.NEXT_INVOICE_NUMBER).getValue());

                originalInvoice.setReference(lastInvoiceNumber);

                lastInvoiceNumber++;
                configuration.setConfigurationValues(Map.of(ConfigKey.NEXT_INVOICE_NUMBER, String.valueOf(lastInvoiceNumber)));
            }

            session.merge(originalInvoice);
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

    static boolean deleteById(Long id) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            Invoice deletedInvoice = session.find(Invoice.class, id);

            if (deletedInvoice == null) {
                return false;
            }

            session.remove(deletedInvoice);
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

    static Long count() {
        Long count;

        try (Session session = sessionFactory.openSession()) {
            String query = "SELECT COUNT(*) count_invoice FROM Invoice I";
            Query<Long> nativeQuery = session.createQuery(query, long.class);
            count = nativeQuery.getSingleResult();
        }

        return count;
    }

    static Optional<Invoice> findFirstByOrderByIdDesc() {
        try (Session session = sessionFactory.openSession()) {
            String query = "SELECT I FROM Invoice I WHERE I.reference is not null ORDER BY I.reference  DESC LIMIT 1";
            Query<Invoice> nativeQuery = session.createQuery(query, Invoice.class);
            return Optional.of(nativeQuery.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
