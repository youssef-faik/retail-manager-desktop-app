package com.example.salesmanagement.taxrate;

import com.example.salesmanagement.HibernateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Optional;

public interface TaxRateRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<TaxRate> findAll() {
        ObservableList<TaxRate> taxRates = FXCollections.observableArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            taxRates.addAll(session.createQuery("select T from TaxRate T", TaxRate.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return taxRates;
    }

    static boolean save(TaxRate taxRate) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(taxRate);
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

    static Optional<TaxRate> update(TaxRate updatedTaxRate) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            TaxRate TaxRate = session.find(TaxRate.class, updatedTaxRate.getId());

            if (TaxRate == null) {
                return Optional.empty();
            }

            TaxRate.setLabel(updatedTaxRate.getLabel());
            TaxRate.setValue(updatedTaxRate.getValue());

            session.getTransaction().commit();

            return Optional.of(TaxRate);
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

    static boolean deleteById(Long id) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            TaxRate deletedTaxRate = session.getReference(TaxRate.class, id);
            if (deletedTaxRate == null) {
                return false;
            }

            session.remove(deletedTaxRate);
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
}
