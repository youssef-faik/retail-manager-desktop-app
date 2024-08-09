package com.example.gestioncommercial.configuration;

import com.example.gestioncommercial.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The ConfigOptionRepository class is a repository for managing configuration options.
 */
public interface ConfigOptionRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    /**
     * Finds all configuration options.
     *
     * @return a list of all configuration options
     */
    static Set<ConfigOption> findAll() {
        Set<ConfigOption> configOptions = new HashSet<>();

        Session session = sessionFactory.openSession();

        try (session) {
            configOptions = session.createQuery("select C from ConfigOption C", ConfigOption.class).stream().collect(Collectors.toSet());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return configOptions;
    }

    /**
     * Finds a configuration option by its key.
     *
     * @param key the configuration key
     * @return an Optional containing the configuration option if found, or an empty Optional if not found
     */
    static Optional<ConfigOption> findByKey(ConfigKey key) {
        return findAll().stream()
                .filter(option -> option.getKey().equals(key))
                .findFirst();
    }

    /**
     * Saves a configuration option.
     *
     * @param option the configuration option to save
     */
    static void save(ConfigOption option) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(option);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();

            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE
                    || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK) {
                session.getTransaction().rollback();
            }
        } finally {
            session.close();
        }

    }

    /**
     * Updates a configuration option.
     *
     * @param updatedOption the configuration option to update
     */

    static void update(ConfigOption updatedOption) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            ConfigOption oldOption = session.find(ConfigOption.class, updatedOption.getKey());
            oldOption.setValue(updatedOption.getValue());

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();

            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE
                    || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK) {
                session.getTransaction().rollback();
            }
        } finally {
            session.close();
        }
    }
}
