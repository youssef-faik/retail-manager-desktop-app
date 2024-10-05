package com.example.salesmanagement.client;

import com.example.salesmanagement.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static List<Client> findAll() {
        List<Client> clients = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            clients.addAll(session.createQuery("select c from Client c", Client.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }

    static Optional<Client> findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Client> query = session.createQuery("select c from Client c where lower(c.name) = lower(:name)", Client.class);
            query.setParameter("name", name);
            Client client = query.uniqueResult();

            if (client != null) {
                session.refresh(client);
            }

            return Optional.ofNullable(client);
        }
    }

    static Optional<Client> findByICE(String ice) {
        try (Session session = sessionFactory.openSession()) {
            Query<Client> query = session.createQuery("select c from Client c where c.commonCompanyIdentifier = :ice", Client.class);
            query.setParameter("ice", ice);
            Client client = query.uniqueResult();

            if (client != null) {
                session.refresh(client);
            }

            return Optional.ofNullable(client);
        }
    }

    static Optional<Client> findByIF(String IF) {
        try (Session session = sessionFactory.openSession()) {
            Query<Client> query = session.createQuery("select c from Client c where c.taxIdentificationNumber = :IF", Client.class);
            query.setParameter("IF", IF);
            Client client = query.uniqueResult();

            if (client != null) {
                session.refresh(client);
            }

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
