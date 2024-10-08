package com.example.salesmanagement.supplier;

import com.example.salesmanagement.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            suppliers.addAll(session.createQuery("select S from Supplier S", Supplier.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    static Optional<Supplier> findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Supplier> query = session.createQuery("select c from Supplier c where lower(c.name) = lower(:name)", Supplier.class);
            query.setParameter("name", name);
            Supplier supplier = query.uniqueResult();

            if (supplier != null) {
                session.refresh(supplier);
            }

            return Optional.ofNullable(supplier);
        }
    }

    static Optional<Supplier> findByICE(String ice) {
        try (Session session = sessionFactory.openSession()) {
            Query<Supplier> query = session.createQuery("select c from Supplier c where c.commonCompanyIdentifier = :ice", Supplier.class);
            query.setParameter("ice", ice);
            Supplier supplier = query.uniqueResult();

            if (supplier != null) {
                session.refresh(supplier);
            }

            return Optional.ofNullable(supplier);
        }
    }

    static Optional<Supplier> findByIF(String IF) {
        try (Session session = sessionFactory.openSession()) {
            Query<Supplier> query = session.createQuery("select c from Supplier c where c.taxIdentificationNumber = :IF", Supplier.class);
            query.setParameter("IF", IF);
            Supplier supplier = query.uniqueResult();

            if (supplier != null) {
                session.refresh(supplier);
            }

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
