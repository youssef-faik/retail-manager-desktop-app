package com.example.salesmanagement;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    public LineChart<Object, Object> lastMonthSalesLineChart;
    @FXML
    public BarChart monthlySalesLineChart;
    @FXML
    public TableView topSellingProductsTableView;
    @FXML
    public PieChart topSellingCategoriesPieChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lastMonthSalesLineChart.getXAxis().setLabel("Jours");
        lastMonthSalesLineChart.getYAxis().setLabel("Ventes en Dhs");
        lastMonthSalesLineChart.setLegendSide(Side.RIGHT);

        XYChart.Series<Object, Object> lastMonthSalesSeries = new XYChart.Series<>();
        lastMonthSalesSeries.setName("Ventes");
        DashboardRepository.getLastMonthSales().forEach(data -> lastMonthSalesSeries.getData().add(new XYChart.Data<>(data.getKey().toString(), data.getValue())));
        lastMonthSalesLineChart.getData().add(lastMonthSalesSeries);

        XYChart.Series<Object, Object> lastMonthReturnsSeries = new XYChart.Series<>();
        lastMonthReturnsSeries.setName("Retours");
        DashboardRepository.getLastMonthReturns().forEach(data -> lastMonthReturnsSeries.getData().add(new XYChart.Data<>(data.getKey().toString(), data.getValue())));
        lastMonthSalesLineChart.getData().add(lastMonthReturnsSeries);

        XYChart.Series<Object, Object> lastMonthPurchasesSeries = new XYChart.Series<>();
        lastMonthPurchasesSeries.setName("Achtas");
        DashboardRepository.getLastMonthPurchases().forEach(data -> lastMonthPurchasesSeries.getData().add(new XYChart.Data<>(data.getKey().toString(), data.getValue())));
        lastMonthSalesLineChart.getData().add(lastMonthPurchasesSeries);


        monthlySalesLineChart.getXAxis().setLabel("Mois");
        monthlySalesLineChart.getYAxis().setLabel("Ventes en Dhs");
        monthlySalesLineChart.setLegendSide(Side.RIGHT);

        XYChart.Series<Object, Object> monthlySalesSeries = new XYChart.Series<>();
        DashboardRepository.getMonthlySales().forEach(data -> monthlySalesSeries.getData().add(new XYChart.Data<>(data.getKey().toString(), data.getValue())));
        monthlySalesSeries.setName("Ventes");
        monthlySalesLineChart.getData().add(monthlySalesSeries);

        XYChart.Series<Object, Object> monthlyReturnsSeries = new XYChart.Series<>();
        DashboardRepository.getMonthlyReturns().forEach(data -> monthlyReturnsSeries.getData().add(new XYChart.Data<>(data.getKey().toString(), data.getValue())));
        monthlyReturnsSeries.setName("Retours");
        monthlySalesLineChart.getData().add(monthlyReturnsSeries);


        XYChart.Series<Object, Object> monthlyPurchasesSeries = new XYChart.Series<>();
        DashboardRepository.getMonthlyPurchases().forEach(data -> monthlyPurchasesSeries.getData().add(new XYChart.Data<>(data.getKey().toString(), data.getValue())));
        monthlyPurchasesSeries.setName("Achats");
        monthlySalesLineChart.getData().add(monthlyPurchasesSeries);


        DashboardRepository.getTopSellingCategories().forEach(data -> topSellingCategoriesPieChart.getData().add(new PieChart.Data(data.getKey().toString(), (Long) data.getValue())));
        topSellingCategoriesPieChart.setLegendSide(Side.RIGHT);


        TableColumn<ChartData, String> labelColumn = new TableColumn<>("Libellé");
        TableColumn<ChartData, String> quantityColumn = new TableColumn<>("Quantité");

        labelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey().toString()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().toString()));

        topSellingProductsTableView.getColumns().addAll(labelColumn, quantityColumn);
        topSellingProductsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        DashboardRepository.getTopSellingProducts().forEach(data -> topSellingProductsTableView.getItems().add(data));
    }
}
