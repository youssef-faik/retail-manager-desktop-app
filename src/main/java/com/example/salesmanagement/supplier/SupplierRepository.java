package com.example.salesmanagement.supplier;

import com.example.salesmanagement.HibernateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Optional;

public interface SupplierRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<Supplier> findAll() {
        ObservableList<Supplier> suppliers = FXCollections.observableArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            suppliers.addAll(session.createQuery("select S from Supplier S", Supplier.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    static Optional<Supplier> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Supplier supplier = session.find(Supplier.class, id);
            session.refresh(supplier);

            return Optional.ofNullable(supplier);
        }
    }

    static boolean save(Supplier supplier) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(supplier);
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

    static Optional<Supplier> update(Supplier updatedSupplier) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            Supplier oldSupplier = session.find(Supplier.class, updatedSupplier.getId());

            if (oldSupplier == null) {
                return Optional.empty();
            }

            oldSupplier.setName(updatedSupplier.getName());
            oldSupplier.setAddress(updatedSupplier.getAddress());
            oldSupplier.setPhoneNumber(updatedSupplier.getPhoneNumber());
            oldSupplier.setCommonCompanyIdentifier(updatedSupplier.getCommonCompanyIdentifier());
            oldSupplier.setTaxIdentificationNumber(updatedSupplier.getTaxIdentificationNumber());

            session.getTransaction().commit();

            return Optional.of(oldSupplier);
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

            Supplier deletedSupplier = session.getReference(Supplier.class, id);
            if (deletedSupplier == null) {
                return false;
            }

            session.remove(deletedSupplier);
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
