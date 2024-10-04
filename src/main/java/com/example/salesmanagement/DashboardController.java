package com.example.salesmanagement;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    public LineChart<Number, Number> lastMonthResultsLineChart;
    @FXML
    public BarChart<String, Number> monthlyResultsBarChart;
    @FXML
    public TableView<ChartData> topSellingProductsTableView;
    @FXML
    public PieChart topSellingCategoriesPieChart;
    LocalDate startDate = LocalDate.now().minusDays(30);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureCharts();
        loadDashboardData();
    }

    private void configureCharts() {
        // Configure the LineChart axes
        NumberAxis xAxis = (NumberAxis) lastMonthResultsLineChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) lastMonthResultsLineChart.getYAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Résultats en Dhs");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(30);
        xAxis.setTickUnit(1);

        LocalDate startDate = LocalDate.now().minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-dd");
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number object) {
                return startDate.plusDays(object.longValue()).format(formatter);
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        });

        // Set up BarChart categories
        CategoryAxis monthlyResultsBarChartXAxis = (CategoryAxis) monthlyResultsBarChart.getXAxis();
        monthlyResultsBarChartXAxis.setLabel("Mois");
        monthlyResultsBarChartXAxis.setCategories(FXCollections.observableArrayList(getMonthsUpToCurrent()));

        topSellingCategoriesPieChart.setLegendSide(Side.RIGHT);
        setupTableViewColumns();
    }

    private void setupTableViewColumns() {
        TableColumn<ChartData, String> labelColumn = new TableColumn<>("Libellé");
        TableColumn<ChartData, String> quantityColumn = new TableColumn<>("Quantité");
        labelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey().toString()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().toString()));
        topSellingProductsTableView.getColumns().addAll(labelColumn, quantityColumn);
        topSellingProductsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private List<String> getMonthsUpToCurrent() {
        List<String> monthsUpToCurrent = new ArrayList<>();
        for (int i = 1; i <= LocalDate.now().getMonthValue(); i++) {
            monthsUpToCurrent.add(getMonthName(i));
        }
        return monthsUpToCurrent;
    }

    private void loadDashboardData() {
        loadLastMonthResults();
        loadMonthlyResults();
        loadTopSellingCategories();
        loadTopSellingProducts();
    }

    private void loadLastMonthResults() {
        Task<Void> lastMonthSalesTask = new Task<>() {
            @Override
            protected Void call() {
                // Fetch and load each dataset separately
                XYChart.Series<Number, Number> salesSeries = loadLastMonthSalesData();
                XYChart.Series<Number, Number> returnsSeries = loadLastMonthReturnsData();
                XYChart.Series<Number, Number> purchasesSeries = loadLastMonthPurchasesData();

                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    lastMonthResultsLineChart.getData().addAll(salesSeries, returnsSeries, purchasesSeries);
                });
                return null;
            }
        };

        lastMonthSalesTask.setOnFailed(event -> handleTaskFailure(event));
        new Thread(lastMonthSalesTask).start();
    }

    private XYChart.Series<Number, Number> loadLastMonthSalesData() {
        XYChart.Series<Number, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Ventes");

        DashboardRepository.getLastMonthSales().forEach(data ->
                salesSeries.getData().add(new XYChart.Data<>(getDaysBetween(data.getKey()), data.getValue())));

        return salesSeries;
    }

    private XYChart.Series<Number, Number> loadLastMonthReturnsData() {
        XYChart.Series<Number, Number> returnsSeries = new XYChart.Series<>();
        returnsSeries.setName("Retours");

        DashboardRepository.getLastMonthReturns().forEach(data ->
                returnsSeries.getData().add(new XYChart.Data<>(getDaysBetween(data.getKey()), data.getValue())));

        return returnsSeries;
    }

    private XYChart.Series<Number, Number> loadLastMonthPurchasesData() {
        XYChart.Series<Number, Number> purchasesSeries = new XYChart.Series<>();
        purchasesSeries.setName("Achats");

        DashboardRepository.getLastMonthPurchases().forEach(data ->
                purchasesSeries.getData().add(new XYChart.Data<>(getDaysBetween(data.getKey()), data.getValue())));

        return purchasesSeries;
    }

    private void loadMonthlyResults() {
        Task<Void> monthlySalesTask = new Task<>() {
            @Override
            protected Void call() {
                // Fetch and load each dataset separately
                XYChart.Series<String, Number> salesSeries = loadMonthlySalesData();
                XYChart.Series<String, Number> returnsSeries = loadMonthlyReturnsData();
                XYChart.Series<String, Number> purchasesSeries = loadMonthlyPurchasesData();

                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    monthlyResultsBarChart.getData().addAll(salesSeries, returnsSeries, purchasesSeries);
                });
                return null;
            }
        };

        monthlySalesTask.setOnFailed(event -> handleTaskFailure(event));
        new Thread(monthlySalesTask).start();
    }

    private XYChart.Series<String, Number> loadMonthlySalesData() {
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Ventes");

        DashboardRepository.getMonthlySales().forEach(data ->
                salesSeries.getData().add(new XYChart.Data<>(getMonthName(data.getKey().intValue()), data.getValue())));

        return salesSeries;
    }

    private XYChart.Series<String, Number> loadMonthlyReturnsData() {
        XYChart.Series<String, Number> returnsSeries = new XYChart.Series<>();
        returnsSeries.setName("Retours");

        DashboardRepository.getMonthlyReturns().forEach(data ->
                returnsSeries.getData().add(new XYChart.Data<>(getMonthName(data.getKey().intValue()), data.getValue())));

        return returnsSeries;
    }

    private XYChart.Series<String, Number> loadMonthlyPurchasesData() {
        XYChart.Series<String, Number> purchasesSeries = new XYChart.Series<>();
        purchasesSeries.setName("Achats");

        DashboardRepository.getMonthlyPurchases().forEach(data ->
                purchasesSeries.getData().add(new XYChart.Data<>(getMonthName(data.getKey().intValue()), data.getValue())));

        return purchasesSeries;
    }

    private void loadTopSellingCategories() {
        Task<Void> topSellingCategoriesTask = new Task<>() {
            @Override
            protected Void call() {
                List<PieChart.Data> categories = new ArrayList<>();
                DashboardRepository.getTopSellingCategories().forEach(data ->
                        categories.add(new PieChart.Data(data.getKey().toString(), (Long) data.getValue())));

                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    topSellingCategoriesPieChart.getData().addAll(categories);
                });
                return null;
            }
        };

        topSellingCategoriesTask.setOnFailed(event -> handleTaskFailure(event));
        new Thread(topSellingCategoriesTask).start();
    }

    private void loadTopSellingProducts() {
        Task<Void> topSellingProductsTask = new Task<>() {
            @Override
            protected Void call() {
                List<ChartData> products = DashboardRepository.getTopSellingProducts();

                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    topSellingProductsTableView.getItems().clear();
                    topSellingProductsTableView.getItems().addAll(products);
                });
                return null;
            }
        };

        topSellingProductsTask.setOnFailed(event -> handleTaskFailure(event));
        new Thread(topSellingProductsTask).start();
    }

    private long getDaysBetween(LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    private String getMonthName(int monthNumber) {
        // Create an instance of DateFormatSymbols for French locale
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.FRENCH);
        String[] months = symbols.getMonths();

        // Return the month name (adjust for zero-based indexing)
        return months[monthNumber - 1]; // monthNumber is 1-based, while array is 0-based
    }

    private void handleTaskFailure(Event event) {
        // Handle task failure (show error message or log)
        System.err.println("Task failed: " + event);
    }
}
