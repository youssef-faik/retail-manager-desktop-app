package com.example.salesmanagement;

import com.example.salesmanagement.document.CreditInvoiceStatus;
import com.example.salesmanagement.document.InvoiceStatus;
import com.example.salesmanagement.document.PurchaseDeliveryNoteStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public interface DashboardRepository {
    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static List<TemporalChartData> getLastMonthSales() {
        // Default to the last 30 days
        LocalDate startDate = LocalDate.now().minusDays(30);

        // Default to today's date
        LocalDate endDate = LocalDate.now();

        Session session = sessionFactory.openSession();

        List<TemporalChartData> data = null;

        try (session) {
            String queryString = "select  D.issueDate as date, sum(D.totalIncludingTaxes) as total_sales from Invoice as I join Document D on D.id = I.id where I.status not in (:draft_status, :cancelled_status) and D.issueDate between :start_date and :end_date group by D.issueDate order by D.issueDate";
            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("start_date", startDate);
            query.setParameter("end_date", endDate);
            query.setParameter("draft_status", InvoiceStatus.DRAFT);
            query.setParameter("cancelled_status", InvoiceStatus.CANCELLED);

            data = query.list().
                    stream()
                    .map(entry -> new TemporalChartData((LocalDate) entry[0], (Number) entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static List<TemporalChartData> getLastMonthReturns() {

        // Default to the last 30 days
        LocalDate startDate = LocalDate.now().minusDays(30);

        // Default to today's date
        LocalDate endDate = LocalDate.now();

        Session session = sessionFactory.openSession();

        List<TemporalChartData> data = null;

        try (session) {
            String queryString = "select  D.issueDate as date, sum(D.totalIncludingTaxes) as total_sales from CreditInvoice as CI join Document D on D.id = CI.id where CI.status not in (:draft_status, :cancelled_status) and D.issueDate between :start_date and :end_date group by D.issueDate order by D.issueDate";
            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("start_date", startDate);
            query.setParameter("end_date", endDate);
            query.setParameter("draft_status", CreditInvoiceStatus.DRAFT);
            query.setParameter("cancelled_status", CreditInvoiceStatus.CANCELLED);

            data = query.list().
                    stream()
                    .map(entry -> new TemporalChartData((LocalDate) entry[0], (Number) entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static List<TemporalChartData> getLastMonthPurchases() {

        // Default to the last 30 days
        LocalDate startDate = LocalDate.now().minusDays(30);

        // Default to today's date
        LocalDate endDate = LocalDate.now();

        Session session = sessionFactory.openSession();

        List<TemporalChartData> data = null;

        try (session) {
            String queryString = "select  D.issueDate as date, sum(D.totalIncludingTaxes) as total_sales from PurchaseDeliveryNote as CI join Document D on D.id = CI.id where CI.status not in (:draft_status, :cancelled_status) and D.issueDate between :start_date and :end_date group by D.issueDate order by D.issueDate";
            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("start_date", startDate);
            query.setParameter("end_date", endDate);
            query.setParameter("draft_status", PurchaseDeliveryNoteStatus.DRAFT);
            query.setParameter("cancelled_status", PurchaseDeliveryNoteStatus.CANCELLED);

            data = query.list().
                    stream()
                    .map(entry -> new TemporalChartData((LocalDate) entry[0], (Number) entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static List<NumericChartData> getMonthlySales() {

        // Default to the 1st jan of the current year
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);

        // Default to 31 th of december of the curent year
        LocalDate endDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        Session session = sessionFactory.openSession();

        List<NumericChartData> data = null;

        try (session) {
            String queryString = "select month(D.issueDate) as date, sum(D.totalIncludingTaxes) as total_sales from Invoice as I join Document D on D.id = I.id where I.status not in (:draft_status, :cancelled_status) and D.issueDate between :start_date and :end_date group by month(D.issueDate) order by month(D.issueDate)";
            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("start_date", startDate);
            query.setParameter("end_date", endDate);
            query.setParameter("draft_status", InvoiceStatus.DRAFT);
            query.setParameter("cancelled_status", InvoiceStatus.CANCELLED);

            data = query.list().
                    stream()
                    .map(entry -> new NumericChartData((Number) entry[0], (Number) entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static List<NumericChartData> getMonthlyReturns() {

        // Default to the 1st jan of the current year
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);

        // Default to 31 th of december of the curent year
        LocalDate endDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        Session session = sessionFactory.openSession();

        List<NumericChartData> data = null;

        try (session) {
            String queryString = "select month(D.issueDate) as date, sum(D.totalIncludingTaxes) as total_sales from CreditInvoice as I join Document D on D.id = I.id where I.status not in (:draft_status, :cancelled_status) and D.issueDate between :start_date and :end_date group by month(D.issueDate) order by month(D.issueDate)";
            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("start_date", startDate);
            query.setParameter("end_date", endDate);
            query.setParameter("draft_status", CreditInvoiceStatus.DRAFT);
            query.setParameter("cancelled_status", CreditInvoiceStatus.CANCELLED);

            data = query.list().
                    stream()
                    .map(entry -> new NumericChartData((Number) entry[0], (Number) entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static List<NumericChartData> getMonthlyPurchases() {

        // Default to the 1st jan of the current year
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);

        // Default to 31 th of december of the curent year
        LocalDate endDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        Session session = sessionFactory.openSession();

        List<NumericChartData> data = null;

        try (session) {
            String queryString = "select month(D.issueDate) as date, sum(D.totalIncludingTaxes) as total_sales from PurchaseDeliveryNote as I join Document D on D.id = I.id where I.status not in (:draft_status, :cancelled_status) and D.issueDate between :start_date and :end_date group by month(D.issueDate) order by month(D.issueDate)";
            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("start_date", startDate);
            query.setParameter("end_date", endDate);
            query.setParameter("draft_status", PurchaseDeliveryNoteStatus.DRAFT);
            query.setParameter("cancelled_status", PurchaseDeliveryNoteStatus.CANCELLED);

            data = query.list().
                    stream()
                    .map(entry -> new NumericChartData((Number) entry[0], (Number) entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static List<ChartData> getTopSellingProducts() {
        Session session = sessionFactory.openSession();

        List<ChartData> data = null;

        try (session) {
            String queryString = """
                    select\s
                        DI.product.name as name, \s
                        sum(DI.quantity) quantity_sold\s
                    from DocumentItem as DI
                        join DI.document D
                        join Invoice I on I = D
                    where I.status not in (:draft_status, :cancelled_status)\s
                    group by\s
                        DI.product,\s
                        DI.product.name\s
                    order by sum(DI.quantity) desc""";

            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("draft_status", InvoiceStatus.DRAFT);
            query.setParameter("cancelled_status", InvoiceStatus.CANCELLED);

            List<Object[]> list = query.list();
            data = list.
                    stream()
                    .map(entry -> new ChartData(entry[0], entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    static List<ChartData> getTopSellingCategories() {
        Session session = sessionFactory.openSession();

        List<ChartData> data = null;

        try (session) {
            String queryString = """
                    select\s
                        DI.product.category.name as name, \s
                        sum(DI.quantity) quantity_sold\s
                    from DocumentItem as DI
                        join DI.document D
                        join Invoice I on I = D
                        join DI.product.category C
                    where I.status not in (:draft_status, :cancelled_status)\s
                    group by\s
                        C.name\s
                    order by sum(DI.quantity) desc""";

            Query<Object[]> query = session.createQuery(queryString, Object[].class);
            query.setParameter("draft_status", InvoiceStatus.DRAFT);
            query.setParameter("cancelled_status", InvoiceStatus.CANCELLED);

            List<Object[]> list = query.list();
            data = list.
                    stream()
                    .map(entry -> new ChartData(entry[0], entry[1]))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

}
