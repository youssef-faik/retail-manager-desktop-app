package com.example.salesmanagement.client;

import com.example.salesmanagement.HibernateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Optional;

public interface ClientRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<Client> findAll() {
        ObservableList<Client> clients = FXCollections.observableArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            clients.addAll(session.createQuery("select c from Client c", Client.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }

    static Optional<Client> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Client client = session.find(Client.class, id);
            session.refresh(client);

            return Optional.ofNullable(client);
        }
    }

    static boolean save(Client client) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(client);
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

    static Optional<Client> update(Client updatedClient) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            Client oldClient = session.find(Client.class, updatedClient.getId());

            if (oldClient == null) {
                return Optional.empty();
            }

            oldClient.setName(updatedClient.getName());
            oldClient.setAddress(updatedClient.getAddress());
            oldClient.setPhoneNumber(updatedClient.getPhoneNumber());
            oldClient.setCommonCompanyIdentifier(updatedClient.getCommonCompanyIdentifier());
            oldClient.setTaxIdentificationNumber(updatedClient.getTaxIdentificationNumber());

            session.getTransaction().commit();

            return Optional.of(oldClient);
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

            Client deletedClient = session.getReference(Client.class, id);
            if (deletedClient == null) {
                return false;
            }

            session.remove(deletedClient);
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
