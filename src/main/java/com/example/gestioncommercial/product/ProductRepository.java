package com.example.gestioncommercial.product;

import com.example.gestioncommercial.HibernateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Optional;

public interface ProductRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<Product> findAll() {
        ObservableList<Product> products = FXCollections.observableArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            products.addAll(session.createQuery("select P from Product P", Product.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    static Optional<Product> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Product product = session.find(Product.class, id);
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
            oldProduct.setQuantity(updatedProduct.getQuantity());

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
