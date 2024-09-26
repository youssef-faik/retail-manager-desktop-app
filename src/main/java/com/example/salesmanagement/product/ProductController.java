package com.example.salesmanagement.product;

import com.example.salesmanagement.category.Category;
import com.example.salesmanagement.category.CategoryRepository;
import com.example.salesmanagement.document.CreditInvoice;
import com.example.salesmanagement.document.DeliveryNote;
import com.example.salesmanagement.document.Invoice;
import com.example.salesmanagement.document.PurchaseDeliveryNote;
import com.example.salesmanagement.stockmouvement.*;
import com.example.salesmanagement.taxrate.TaxRate;
import com.example.salesmanagement.taxrate.TaxRateRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

public class ProductController implements Initializable {
    private final Pair<String, Category> EMPTY_CATEGORY = new Pair<>(null, null);
    private final Pair<String, TaxRate> EMPTY_TAX_RATE = new Pair<>(null, null);
    @FXML
    public Tab stockMouvementsTab;
    @FXML
    public TableView<StockMovement> stockMouvementsTable;
    public VBox parentVBox;
    TabPane tabPane;
    private Long id;
    private ListProductsController listProductsController;
    @FXML
    private Button saveButton, cancelButton;
    @FXML
    private TextField nameTextField, purchasePriceExcludingTaxTextField, sellingPriceExcludingTaxTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private ComboBox<Pair<String, TaxRate>> taxRateComboBox;
    @FXML
    private ComboBox<Pair<String, Category>> categoryComboBox;

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

        cancelButton.setEffect(dropShadow);
        saveButton.setEffect(dropShadow);
        saveButton.setTextFill(Color.color(1, 1, 1));
        saveButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));

        saveButton.setOnAction(e -> saveProduct());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });

        tabPane = stockMouvementsTab.getTabPane();
        tabPane.getTabs().remove(stockMouvementsTab);

        initCategoryComboBox();
        initTaxRatesComboBox();
    }

    public void saveProduct() {
        Product product = mapProduct();
        if (ProductRepository.save(product)) {
            resetForm();

            if (listProductsController != null) {
                listProductsController.getProductsTable().getItems().add(product);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateProduct() {
        Product product = mapProduct();
        Optional<Product> optionalProduct = ProductRepository.update(product);

        if (optionalProduct.isPresent()) {
            if (listProductsController != null) {
                int index = listProductsController.getProductsTable().getItems().indexOf(product);

                if (index != -1) {
                    Product oldProduct = listProductsController.getProductsTable().getItems().get(index);

                    listProductsController.getProductsTable().getItems().remove(oldProduct);
                    listProductsController.getProductsTable().getItems().add(optionalProduct.get());

                    listProductsController.getProductsTable().refresh();
                }
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Product mapProduct() {
        Product product = new Product();

        product.setId(id);
        product.setName(nameTextField.getText());
        product.setDescription(descriptionTextArea.getText());
        product.setSellingPriceExcludingTax(new BigDecimal(sellingPriceExcludingTaxTextField.getText()));
        product.setPurchasePriceExcludingTax(new BigDecimal(purchasePriceExcludingTaxTextField.getText()));
        product.setTaxRate(taxRateComboBox.getValue().getValue());
        product.setCategory(categoryComboBox.getValue().getValue());

        return product;
    }

    public void initProductUpdate(Product product) {
        this.id = product.getId();
        this.nameTextField.setText(product.getName());
        this.descriptionTextArea.setText(product.getDescription());
        this.purchasePriceExcludingTaxTextField.setText(String.valueOf(product.getPurchasePriceExcludingTax()));
        this.sellingPriceExcludingTaxTextField.setText(String.valueOf(product.getSellingPriceExcludingTax()));

        tabPane.getTabs().add(stockMouvementsTab);
        initStockMovementsTableView(product);
        parentVBox.setPrefWidth(550);

        if (product.getCategory() != null) {
            categoryComboBox.getItems()
                    .stream()
                    .filter(pair -> Objects.equals(pair.getValue(), product.getCategory()))
                    .findFirst()
                    .ifPresent(value -> categoryComboBox.setValue(value));
        } else {
            categoryComboBox.setValue(EMPTY_CATEGORY);
        }

        if (product.getTaxRate() != null) {
            taxRateComboBox.getItems()
                    .stream()
                    .filter(pair -> Objects.equals(pair.getValue(), product.getTaxRate()))
                    .findFirst()
                    .ifPresent(value -> taxRateComboBox.setValue(value));
        } else {
            taxRateComboBox.setValue(EMPTY_TAX_RATE);
        }

        saveButton.setOnAction(e -> updateProduct());
    }

    private void resetForm() {
        nameTextField.clear();
        descriptionTextArea.clear();
        sellingPriceExcludingTaxTextField.clear();
        purchasePriceExcludingTaxTextField.clear();

        categoryComboBox.getSelectionModel().selectFirst();
        taxRateComboBox.getSelectionModel().selectFirst();
    }

    private void initCategoryComboBox() {
        categoryComboBox.setEditable(false);

        categoryComboBox.setCellFactory(x -> new CategoryComboCell());
        categoryComboBox.setButtonCell(new CategoryComboCell());

        ObservableList<Category> categories = CategoryRepository.findAll();
        Function<Category, Pair<String, Category>> categoryObjectFunction = category -> new Pair<>(category.getName(), category);

        categoryComboBox.getItems().add(EMPTY_CATEGORY);
        categoryComboBox.getItems().addAll(categories.stream().map(categoryObjectFunction).toList());
        categoryComboBox.getSelectionModel().selectFirst();
    }

    private void initTaxRatesComboBox() {
        taxRateComboBox.setEditable(false);

        taxRateComboBox.setCellFactory(x -> new TaxRateComboCell());
        taxRateComboBox.setButtonCell(new TaxRateComboCell());

        ObservableList<TaxRate> taxRates = TaxRateRepository.findAll();
        Function<TaxRate, Pair<String, TaxRate>> taxRateObjectFunction = taxRate -> new Pair<>(taxRate.getLabel(), taxRate);

        taxRateComboBox.getItems().add(EMPTY_TAX_RATE);
        taxRateComboBox.getItems().addAll(taxRates.stream().map(taxRateObjectFunction).toList());
        taxRateComboBox.getSelectionModel().selectFirst();
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

    public void setListProductsController(ListProductsController listProductsController) {
        this.listProductsController = listProductsController;
    }


    private void initStockMovementsTableView(Product product) {
        TableColumn<StockMovement, String> movementTypeColumn = new TableColumn<>("Type movement");
        TableColumn<StockMovement, String> quantityColumn = new TableColumn<>("Quantité");
        TableColumn<StockMovement, String> dateTimeColumn = new TableColumn<>("Date");
        TableColumn<StockMovement, String> mouvementSourceColumn = new TableColumn<>("Source movement");

        movementTypeColumn.setMinWidth(100);
        movementTypeColumn.setMaxWidth(100);
        movementTypeColumn.setResizable(false);
        movementTypeColumn.setReorderable(false);

        quantityColumn.setMinWidth(65);
        quantityColumn.setMaxWidth(65);
        quantityColumn.setResizable(false);
        quantityColumn.setReorderable(false);

        dateTimeColumn.setMinWidth(140);
        dateTimeColumn.setMaxWidth(140);
        dateTimeColumn.setResizable(false);
        dateTimeColumn.setReorderable(false);

        mouvementSourceColumn.setReorderable(false);

        stockMouvementsTable.getColumns().addAll(
                movementTypeColumn,
                quantityColumn,
                dateTimeColumn,
                mouvementSourceColumn
        );

        movementTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(switch (cellData.getValue().getMovementType()) {
            case STOCK_ENTRY -> "Entrée de stock";
            case OUT_OF_STOCK -> "Sortie de stock";
        }));

        quantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getQuantity())));
        dateTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateTime().format(DateTimeFormatter.ofPattern("yyyy MMM dd - HH:mm:ss"))));
        mouvementSourceColumn.setCellValueFactory(cellData -> {
            MovementSource movementSource = cellData.getValue().getMovementSource();

            String initialValue = "";
            if (movementSource instanceof DocumentBasedMovementSource documentBasedMouvementSource
                    && documentBasedMouvementSource.getSource() != null) {
                if (documentBasedMouvementSource.getSource() instanceof PurchaseDeliveryNote purchaseDeliveryNote) {
                    initialValue = "Bon de réception ref n° : " + purchaseDeliveryNote.getReference();
                } else if (documentBasedMouvementSource.getSource() instanceof DeliveryNote deliveryNote) {
                    initialValue = "Bon de livraison ref n° : " + deliveryNote.getReference();
                } else if (documentBasedMouvementSource.getSource() instanceof Invoice invoice) {
                    initialValue = "Facture ref n° : " + invoice.getReference();
                } else if (documentBasedMouvementSource.getSource() instanceof CreditInvoice creditInvoice) {
                    initialValue = "Facture d'avoir ref n° : " + creditInvoice.getReference();
                }
            }

            if (movementSource instanceof StockCorrectionBasedMovementSource stockCorrectionBasedMovementSource
                    && stockCorrectionBasedMovementSource.getSource() != null) {
                initialValue = "Correction de stock ref n° : " + stockCorrectionBasedMovementSource.getSource().getId();
            }

            return new SimpleStringProperty(initialValue);
        });

        stockMouvementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        stockMouvementsTable.getItems().addAll(StockMovementRepository.findAllByProduct(product));
    }


    private static class CategoryComboCell extends ListCell<Pair<String, Category>> {
        @Override
        protected void updateItem(Pair<String, Category> pair, boolean bln) {
            super.updateItem(pair, bln);
            setText(pair != null ? (pair.getValue() != null ? pair.getValue().getName() : "Choisissez une catégorie") : null);
        }
    }

    private static class TaxRateComboCell extends ListCell<Pair<String, TaxRate>> {
        @Override
        protected void updateItem(Pair<String, TaxRate> pair, boolean bln) {
            super.updateItem(pair, bln);
            setText(pair != null ? (pair.getValue() != null ? pair.getValue().getValue().intValue() + " %" : "Choisissez un taux de TVA") : null);
        }
    }
}
