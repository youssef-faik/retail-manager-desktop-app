package com.example.salesmanagement.stockmouvement;

import com.example.salesmanagement.HibernateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface StockCorrectionRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static ObservableList<StockCorrection> findAll() {
        ObservableList<StockCorrection> stockCorrections = FXCollections.observableArrayList();

        Session session = sessionFactory.openSession();

        try (session) {
            stockCorrections.addAll(session.createQuery("select S from StockCorrection S order by S.dateTime desc", StockCorrection.class).list());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stockCorrections;
    }

    static Optional<StockCorrection> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            StockCorrection stockCorrection = session.find(StockCorrection.class, id);
            session.refresh(stockCorrection);

            return Optional.ofNullable(stockCorrection);
        }
    }

    static Optional<StockCorrection> save(StockCorrection stockCorrection) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();
            stockCorrection = session.merge(stockCorrection);

            saveStockMouvements(stockCorrection, session);
            session.getTransaction().commit();

            return Optional.ofNullable(stockCorrection);
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


    private static void saveStockMouvements(StockCorrection stockCorrection, Session session) {
        List<StockMovement> stockMovements = new ArrayList<>();

        stockCorrection.getItems()
                .forEach(item -> {
                    switch (item.getCorrectionType()) {
                        case POSITIVE ->
                                stockMovements.add(StockMovement.createStockEntryMouvement(item.getProduct(), item.getQuantity(), stockCorrection));
                        case NEGATIVE ->
                                stockMovements.add(StockMovement.createOutOfStockMouvement(item.getProduct(), item.getQuantity(), stockCorrection));
                    }
                });

        try {
            stockMovements.forEach(session::persist);
        } catch (Exception e) {
            // If any exception occurs, it will propagate back to the caller, which will trigger a rollback
            throw e;
        }
    }


//    static Optional<StockMovement> update(StockMovement stockMovement) {
//        Session session = sessionFactory.openSession();
//
//        try {
//            session.beginTransaction();
//
//            StockMovement oldStockMovement = session.find(StockMovement.class, stockMovement.getId());
//
//            if (oldStockMovement == null) {
//                return Optional.empty();
//            }
//
//            oldStockMovement.setMovementType(stockMovement.getMovementType());
//            oldStockMovement.setDateTime(stockMovement.getDateTime());
//            oldStockMovement.setQuantity(stockMovement.getQuantity());
//            oldStockMovement.setProduct(stockMovement.getProduct());
//            oldStockMovement.setMovementSource(stockMovement.getMovementSource());
//
//            session.getTransaction().commit();
//
//            return Optional.of(oldStockMovement);
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE
//                    || session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK) {
//                session.getTransaction().rollback();
//            }
//
//            return Optional.empty();
//        } finally {
//            session.close();
//        }
//    }

    static boolean deleteById(Long id) {
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            StockCorrection deletedStockCorrection = session.getReference(StockCorrection.class, id);
            if (deletedStockCorrection == null) {
                return false;
            }

            session.remove(deletedStockCorrection);
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
