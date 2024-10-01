package com.example.salesmanagement.user;

import com.example.salesmanagement.HibernateUtil;
import jakarta.persistence.NoResultException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<User> findAll() {
        ObservableList<User> users = FXCollections.observableArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            users.addAll(session.createQuery("select U from User U", User.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }


    static boolean save(User user) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(user);
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

    static Optional<User> update(User updatedUser) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            User oldUser = session.find(User.class, updatedUser.getId());

            if (oldUser == null) {
                return Optional.empty();
            }

            oldUser.setFirstName(updatedUser.getFirstName());
            oldUser.setLastName(updatedUser.getLastName());
            oldUser.setUsername(updatedUser.getUsername());
            oldUser.setAddress(updatedUser.getAddress());
            oldUser.setPhoneNumber(updatedUser.getPhoneNumber());

            session.getTransaction().commit();

            return Optional.of(oldUser);
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

            User deletedUser = session.getReference(User.class, id);
            if (deletedUser == null) {
                return false;
            }

            session.remove(deletedUser);
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

    static Optional<User> findByUsername(String username) {
        Session session = sessionFactory.openSession();

        try (session) {
            Query<User> query = session.createQuery("select U from User U where U.username = :username", User.class);
            query.setParameter("username", username);
            User singleResult = query.getSingleResult();
            return Optional.of(singleResult);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    static List<User> findAllByRole(Role role) {
        List<User> users = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            Query<User> query = session.createQuery("select U from User U where U.role = :role", User.class);
            query.setParameter("role", role);
            users.addAll(query.list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    static boolean updatePassword(Long id, String encryptPassword) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            User oldUser = session.find(User.class, id);

            if (oldUser == null) {
                return false;
            }

            oldUser.setPassword(encryptPassword);
            oldUser.setMustChangePassword(false);

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

    static boolean resetPassword(Long id, String encryptPassword) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            User oldUser = session.find(User.class, id);

            if (oldUser == null) {
                return false;
            }

            oldUser.setPassword(encryptPassword);
            oldUser.setMustChangePassword(true);

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
