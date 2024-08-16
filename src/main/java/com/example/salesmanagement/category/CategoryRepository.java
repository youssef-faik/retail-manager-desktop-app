package com.example.salesmanagement.category;

import com.example.salesmanagement.HibernateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Optional;

public interface CategoryRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<Category> findAll() {
        ObservableList<Category> categories = FXCollections.observableArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            categories.addAll(session.createQuery("select C from Category C", Category.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }

    static boolean save(Category category) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(category);
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

    static Optional<Category> update(Category updatedCategory) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            Category Category = session.find(Category.class, updatedCategory.getId());

            if (Category == null) {
                return Optional.empty();
            }

            Category.setName(updatedCategory.getName());
            Category.setDescription(updatedCategory.getDescription());

            session.getTransaction().commit();

            return Optional.of(Category);
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

            Category deletedCategory = session.getReference(Category.class, id);
            if (deletedCategory == null) {
                return false;
            }

            session.remove(deletedCategory);
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
