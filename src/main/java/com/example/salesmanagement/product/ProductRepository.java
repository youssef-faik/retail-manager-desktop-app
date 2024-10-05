package com.example.salesmanagement.product;

import com.example.salesmanagement.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static List<Product> findAll() {
        List<Product> products = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            products.addAll(session.createQuery("select P from Product P", Product.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    static Optional<Product> findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("select c from Product c where lower(c.name) = lower(:name)", Product.class);
            query.setParameter("name", name);
            Product product = query.uniqueResult();

            if (product != null) {
                session.refresh(product);
            }

            return Optional.ofNullable(product);
        }
    }

    static boolean save(Product product) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(product);
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

    static Optional<Product> update(Product updatedProduct) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            Product oldProduct = session.find(Product.class, updatedProduct.getId());

            if (oldProduct == null) {
                return Optional.empty();
            }

            oldProduct.setName(updatedProduct.getName());
            oldProduct.setDescription(updatedProduct.getDescription());
            oldProduct.setPurchasePriceExcludingTax(updatedProduct.getPurchasePriceExcludingTax());
            oldProduct.setSellingPriceExcludingTax(updatedProduct.getSellingPriceExcludingTax());
            oldProduct.setTaxRate(updatedProduct.getTaxRate());
            oldProduct.setCategory(updatedProduct.getCategory());

            session.getTransaction().commit();

            return Optional.of(oldProduct);
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

            Product deletedProduct = session.getReference(Product.class, id);
            if (deletedProduct == null) {
                return false;
            }

            session.remove(deletedProduct);
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
