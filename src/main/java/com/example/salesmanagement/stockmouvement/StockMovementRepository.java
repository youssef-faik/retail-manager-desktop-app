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
import java.util.Objects;
import java.util.Optional;

public interface StockMovementRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static List<StockMovement> findAll() {
        List<StockMovement> stockMovements = new ArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            stockMovements.addAll(session.createQuery("select S from StockMovement S where S.isCanceled = false order by S.dateTime desc", StockMovement.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stockMovements;
    }

    static List<StockMovement> findAllByDocument(Document document) {
        List<StockMovement> stockMovements = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            String queryString = "select S from StockMovement S join MovementSource M on S.movementSource = M join DocumentBasedMovementSource D on M.id = D.id where D.source = :document order by S.dateTime desc";
            Query<StockMovement> query = session.createQuery(queryString, StockMovement.class);
            query.setParameter("document", document);
            stockMovements.addAll(query.list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stockMovements;
    }

    static Optional<StockMovement> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            StockMovement stockMovement = session.find(StockMovement.class, id);
            session.refresh(stockMovement);

            return Optional.ofNullable(stockMovement);
        }
    }

    static Long stockMovementsCountByStockCorrection(StockCorrection stockCorrection) {
        Long count;

        try (Session session = sessionFactory.openSession()) {
            String query = "select count(*) from StockMovement S join MovementSource M on S.movementSource = M join StockCorrectionBasedMouvementSource D on M.id = D.id where D.source = :source";
            Query<Long> nativeQuery = session.createQuery(query, long.class);
            nativeQuery.setParameter("source", stockCorrection);
            count = nativeQuery.getSingleResult();
        }

        return count;
    }

    static boolean save(StockMovement stockMovement) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            session.persist(stockMovement);
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

    static Optional<StockMovement> update(StockMovement stockMovement) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            StockMovement originalStockMovement = session.find(StockMovement.class, stockMovement.getId());

            if (originalStockMovement == null) {
                return Optional.empty();
            }

            originalStockMovement.setMovementType(stockMovement.getMovementType());
            originalStockMovement.setCanceled(stockMovement.isCanceled());
            originalStockMovement.setDateTime(stockMovement.getDateTime());
            originalStockMovement.setQuantity(stockMovement.getQuantity());
            originalStockMovement.setProduct(stockMovement.getProduct());
            originalStockMovement.setMovementSource(stockMovement.getMovementSource());

            session.merge(originalStockMovement);
            session.getTransaction().commit();

            return Optional.of(originalStockMovement);
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

            StockMovement deletedStockMovement = session.find(StockMovement.class, id);
            if (deletedStockMovement == null) {
                return false;
            }

            session.remove(deletedStockMovement);

            StockCorrection stockCorrection = ((StockCorrectionBasedMovementSource) deletedStockMovement.getMovementSource()).getSource();
            if (stockMovementsCountByStockCorrection(stockCorrection) <= 1) {
                session.remove(stockCorrection);
            } else {
                StockCorrectionRepository.findById(stockCorrection.getId()).flatMap(
                        correction -> correction.getItems()
                                .stream()
                                .filter(
                                        stockCorrectionItem -> Objects.equals(stockCorrectionItem.getProduct().getId(), deletedStockMovement.getProduct().getId()))
                                .findFirst()).ifPresent(object ->
                {
                    StockCorrectionItem managedItem = session.merge(object);  // Re-attach to the session
                    stockCorrection.removeItem(managedItem);
                    stockCorrection.removeItem(object);
                    session.remove(managedItem);
                });
            }


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


    static List<StockMovement> findAllByProduct(Product product) {
        List<StockMovement> stockMovements = new ArrayList<>();

        Session session = sessionFactory.openSession();

        try (session) {
            String queryString = "select S from StockMovement S where S.product = :product order by S.dateTime desc";
            Query<StockMovement> query = session.createQuery(queryString, StockMovement.class);
            query.setParameter("product", product);
            stockMovements.addAll(query.list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stockMovements;
    }
}
