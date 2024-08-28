package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.HibernateUtil;
import com.example.salesmanagement.document.Document;
import com.example.salesmanagement.product.Product;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface StockMovementRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static List<StockMouvement> findAll() {
        List<StockMouvement> stockMouvements = new ArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            stockMouvements.addAll(session.createQuery("select S from StockMovement S where S.isCanceled = false order by S.dateTime desc", StockMouvement.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stockMouvements;
    }

    static List<StockMouvement> findAllByDocument(Document document) {
        List<StockMouvement> stockMouvements = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            String queryString = "select S from StockMovement S join MovementSource M on S.movementSource = M join DocumentBasedMovementSource D on M.id = D.id where D.source = :document order by S.dateTime desc";
            Query<StockMouvement> query = session.createQuery(queryString, StockMouvement.class);
            query.setParameter("document", document);
            stockMouvements.addAll(query.list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stockMouvements;
    }

    static Optional<StockMouvement> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            StockMouvement stockMouvement = session.find(StockMouvement.class, id);
            session.refresh(stockMouvement);

            return Optional.ofNullable(stockMouvement);
        }
    }

    static boolean save(StockMouvement stockMouvement) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(stockMouvement);
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

    static Optional<StockMouvement> update(StockMouvement stockMouvement) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            StockMouvement originalStockMouvement = session.find(StockMouvement.class, stockMouvement.getId());

            if (originalStockMouvement == null) {
                return Optional.empty();
            }

            originalStockMouvement.setMovementType(stockMouvement.getMovementType());
            originalStockMouvement.setCanceled(stockMouvement.isCanceled());
            originalStockMouvement.setDateTime(stockMouvement.getDateTime());
            originalStockMouvement.setQuantity(stockMouvement.getQuantity());
            originalStockMouvement.setProduct(stockMouvement.getProduct());
            originalStockMouvement.setMovementSource(stockMouvement.getMovementSource());

            session.merge(originalStockMouvement);
            session.getTransaction().commit();

            return Optional.of(originalStockMouvement);
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

            StockMouvement deletedStockMouvement = session.getReference(StockMouvement.class, id);
            if (deletedStockMouvement == null) {
                return false;
            }

            session.remove(deletedStockMouvement);
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


    static List<StockMouvement> findAllByProduct(Product product) {
        List<StockMouvement> stockMouvements = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            String queryString = "select S from StockMovement S where S.product = :product order by S.dateTime desc";
            Query<StockMouvement> query = session.createQuery(queryString, StockMouvement.class);
            query.setParameter("product", product);
            stockMouvements.addAll(query.list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stockMouvements;
    }
}
