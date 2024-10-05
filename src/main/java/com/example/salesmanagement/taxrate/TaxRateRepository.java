package com.example.salesmanagement.taxrate;

import com.example.salesmanagement.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TaxRateRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static List<TaxRate> findAll() {
        List<TaxRate> taxRates = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            taxRates.addAll(session.createQuery("select T from TaxRate T", TaxRate.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return taxRates;
    }

    static Optional<TaxRate> findByLabel(String label) {
        try (Session session = sessionFactory.openSession()) {
            Query<TaxRate> query = session.createQuery("select c from TaxRate c where lower(c.label) = lower(:label)", TaxRate.class);
            query.setParameter("label", label);
            TaxRate taxRate = query.uniqueResult();

            if (taxRate != null) {
                session.refresh(taxRate);
            }

            return Optional.ofNullable(taxRate);
        }
    }

    static Optional<TaxRate> findByValue(BigDecimal value) {
        try (Session session = sessionFactory.openSession()) {
            Query<TaxRate> query = session.createQuery("select c from TaxRate c where value = :value", TaxRate.class);
            query.setParameter("value", value);
            TaxRate taxRate = query.uniqueResult();

            if (taxRate != null) {
                session.refresh(taxRate);
            }

            return Optional.ofNullable(taxRate);
        }
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
