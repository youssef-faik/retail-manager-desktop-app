package com.example.salesmanagement.product;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListProductsController implements Initializable {
    private static final int ROWS_PER_PAGE = 13;
    @FXML
    public Button newButton, deleteButton, updateButton;
    FilteredList<Product> filteredList;
    SortedList<Product> sortedList;
    ObservableList<Product> observableList;
    @FXML
    private TableView<Product> productsTableView;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField searchTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DropShadow dropShadow = new DropShadow(
                BlurType.ONE_PASS_BOX,
                Color.color(0.6392, 0.6392, 0.6392, 1.0),
                10.0,
                0,
                0,
                0
        );

        updateButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        newButton.setEffect(dropShadow);
        newButton.setTextFill(Color.color(1, 1, 1));
        newButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initProductsTableView();
        refreshProductsTable();

        int dataSize = (productsTableView.getItems().size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(dataSize);
        pagination.setPageFactory(this::createPage);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return product.getName().toLowerCase().contains(lowerCaseFilter)
                        || (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerCaseFilter))
                        || (product.getPurchasePriceExcludingTax() != null && product.getPurchasePriceExcludingTax().toPlainString().contains(lowerCaseFilter))
                        || product.getSellingPriceExcludingTax().toPlainString().contains(lowerCaseFilter)
                        || (product.getCategory() != null && product.getCategory().getName().toLowerCase().contains(lowerCaseFilter));

            });

            // Update pagination after filtering
            int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
            pagination.setPageCount(pageCount);
            // Reset to first page after filter change
            pagination.setCurrentPageIndex(0);
            // Update the table view with the new first page
            createPage(0);
        });
    }

    public void addProduct(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-product.fxml"));
        Parent root = fxmlLoader.load();

        ProductController productController = fxmlLoader.getController();
        productController.setListProductsController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        productsTableView.getSelectionModel().clearSelection();
    }

    public void updateProduct(ActionEvent actionEvent) throws IOException {
        Product selectedProduct = productsTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-product.fxml"));
            Parent root = fxmlLoader.load();

            ProductController productController = fxmlLoader.getController();
            productController.setListProductsController(this);
            productController.initProductUpdate(selectedProduct);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            productsTableView.getSelectionModel().clearSelection();
        }
    }

    public void deleteProduct(ActionEvent actionEvent) {
        Product selectedProduct = productsTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer ce produit?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (ProductRepository.deleteById(selectedProduct.getId())) {
                    productsTableView.getSelectionModel().clearSelection();
                    productsTableView.getItems().remove(selectedProduct);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert("Cet enregistrement ne peut pas être supprimé, car il est référencé par d'autres enregistrements.");
                }
            }

            updateButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    private void initProductsTableView() {
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Product, String> nameColumn = new TableColumn<>("Nom");
        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
        TableColumn<Product, String> quantityColumn = new TableColumn<>("Quantité");
        TableColumn<Product, BigDecimal> purchasePriceExcludingTaxColumn = new TableColumn<>("Prix d'achat (HT)");
        TableColumn<Product, BigDecimal> sellingPriceExcludingTaxColumn = new TableColumn<>("Prix de vente (HT)");
        TableColumn<Product, String> taxRateColumn = new TableColumn<>("Taux TVA");
        TableColumn<Product, String> categoryColumn = new TableColumn<>("Catégorie");

        productsTableView.getColumns().addAll(
                idColumn,
                nameColumn,
                descriptionColumn,
                categoryColumn,
                quantityColumn,
                purchasePriceExcludingTaxColumn,
                sellingPriceExcludingTaxColumn,
                taxRateColumn
        );

        productsTableView.setOnMouseClicked(e -> {
            if (productsTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setVisible(false);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        sellingPriceExcludingTaxColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPriceExcludingTax"));
        purchasePriceExcludingTaxColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePriceExcludingTax"));
        categoryColumn.setCellValueFactory(cellData ->
                {
                    if (cellData.getValue().getCategory() != null) {
                        return new SimpleStringProperty(cellData.getValue().getCategory().getName());
                    }
                    return new SimpleStringProperty("N/A");
                }
        );

        taxRateColumn.setCellValueFactory(cellData ->
                {
                    if (cellData.getValue().getTaxRate() != null) {
                        return new SimpleStringProperty(cellData.getValue().getTaxRate().getValue() + " %");
                    }
                    return new SimpleStringProperty("N/A");
                }
        );

        productsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refreshProductsTable() {
        productsTableView.setItems(ProductRepository.findAll());

        observableList = productsTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(productsTableView.comparatorProperty());
        productsTableView.setItems(sortedList);

        // Update pagination
        int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(pageCount);

        productsTableView.refresh();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }

    private void displayErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Une erreur est survenue lors de l'opération.");
        alert.showAndWait();
    }

    private void displayErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredList.size());

        List<Product> subbedList = filteredList.subList(fromIndex, toIndex);
        productsTableView.setItems(FXCollections.observableArrayList(subbedList));
        return productsTableView;
    }

    public TableView<Product> getProductsTable() {
        return productsTableView;
    }
}
