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
    private final Product product = new Product();
    @FXML
    public TableView<StockMovement> stockMouvementsTable;
    @FXML
    private Tab stockMouvementsTab;
    @FXML
    private VBox parentVBox;

    private TabPane tabPane;

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
        saveButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

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

        if (product == null) {
            return;
        }

        if (product.getPurchasePriceExcludingTax() != null
                && (product.getSellingPriceExcludingTax().compareTo(product.getPurchasePriceExcludingTax()) <= 0)) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "le prix de vente (" + product.getSellingPriceExcludingTax() + ") est inférieur ou égal au prix d'achat(" + product.getPurchasePriceExcludingTax() + "). \nVous souhaitez continuer tel quel ?",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );

            alert.setTitle("Attention");
            alert.setHeaderText("Confirmation des valeurs des prix d'achat et prix de vente");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        if (ProductRepository.save(product)) {
            resetForm();

            if (listProductsController != null) {
                listProductsController.getProductsObservableList().add(product);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateProduct() {
        Product product = mapProduct();

        if (product == null) {
            return;
        }

        if (product.getPurchasePriceExcludingTax() != null
                && (product.getSellingPriceExcludingTax().compareTo(product.getPurchasePriceExcludingTax()) <= 0)) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "le prix de vente (" + product.getSellingPriceExcludingTax() + ") est inférieur ou égal au prix d'achat(" + product.getPurchasePriceExcludingTax() + "). \nVous souhaitez continuer tel quel ?",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );

            alert.setTitle("Attention");
            alert.setHeaderText("Confirmation des valeurs des prix d'achat et prix de vente");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        Optional<Product> optionalProduct = ProductRepository.update(product);

        if (optionalProduct.isPresent()) {
            if (listProductsController != null) {
                int index = listProductsController.getProductsObservableList().indexOf(product);

                if (index != -1) {
                    Product oldProduct = listProductsController.getProductsObservableList().get(index);

                    listProductsController.getProductsObservableList().remove(oldProduct);
                    listProductsController.getProductsObservableList().add(index, optionalProduct.get());

                    listProductsController.productsTableView.refresh();
                }
            }

            initProductUpdate(optionalProduct.get());
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Product mapProduct() {
        String name = nameTextField.getText().trim();

        if (name.isBlank()) {
            displayErrorAlert("Nom du produit est obligatoire");
            return null;
        }

        Optional<Product> byName = ProductRepository.findByName(name.toLowerCase());

        if (this.product.getId() == null) {
            if (byName.isPresent()) {
                displayErrorAlert("Un produit avec le même nom existe déjà");
                return null;
            }
        } else {
            if (!name.equalsIgnoreCase(this.product.getName()) && byName.isPresent()) {
                displayErrorAlert("Un produit avec le même nom existe déjà");
                return null;
            }
        }


        String description = null;
        if (descriptionTextArea.getText() != null) {
            description = descriptionTextArea.getText().trim();
            if (description.isBlank()) {
                description = null;
            }
        }


        String purchasePriceExcludingTaxText;
        BigDecimal purchasePriceExcludingTax = null;
        if (purchasePriceExcludingTaxTextField.getText() != null) {
            purchasePriceExcludingTaxText = purchasePriceExcludingTaxTextField.getText().trim();
            if (purchasePriceExcludingTaxText.isBlank()) {
                purchasePriceExcludingTax = null;
            } else {
                try {
                    purchasePriceExcludingTax = new BigDecimal(purchasePriceExcludingTaxText);
                } catch (Exception e) {
                    displayErrorAlert("la valeur du prix d'achat n'est pas valide");
                    return null;
                }

                if (!(purchasePriceExcludingTax.compareTo(BigDecimal.ZERO) >= 0
                        && purchasePriceExcludingTax.compareTo(BigDecimal.valueOf(9_999_999L)) <= 0)
                ) {
                    displayErrorAlert("la valeur du prix d'achat doit être comprise entre 0 et 9,999,999.00");
                    return null;
                }
            }
        }

        String sellingPriceExcludingTaxText = sellingPriceExcludingTaxTextField.getText().trim();
        if (sellingPriceExcludingTaxText.isBlank()) {
            displayErrorAlert("Prix de vente est obligatoire");
            return null;
        }

        BigDecimal sellingPriceExcludingTax;
        try {
            sellingPriceExcludingTax = new BigDecimal(sellingPriceExcludingTaxText);
        } catch (Exception e) {
            displayErrorAlert("la valeur du prix de vente n'est pas valide");
            return null;
        }

        if (!(sellingPriceExcludingTax.compareTo(BigDecimal.ZERO) >= 0
                && sellingPriceExcludingTax.compareTo(BigDecimal.valueOf(9_999_999L)) <= 0)
        ) {
            displayErrorAlert("la valeur du prix de vente doit être comprise entre 0 et 9,999,999.00");
            return null;
        }

        if (taxRateComboBox.getValue() == EMPTY_TAX_RATE) {
            displayErrorAlert("Taux de TVA est obligatoire");
            return null;
        }

        Product product = new Product();
        product.setId(this.product.getId());
        product.setName(name);
        product.setDescription(description);
        product.setSellingPriceExcludingTax(sellingPriceExcludingTax);
        product.setPurchasePriceExcludingTax(purchasePriceExcludingTax);
        product.setTaxRate(taxRateComboBox.getValue().getValue());
        product.setCategory(categoryComboBox.getValue().getValue());

        return product;
    }

    public void initProductUpdate(Product product) {
        this.nameTextField.setText(product.getName());
        this.descriptionTextArea.setText(product.getDescription() == null ? "" : product.getDescription());
        this.purchasePriceExcludingTaxTextField.setText(String.valueOf(product.getPurchasePriceExcludingTax() == null ? "" : product.getPurchasePriceExcludingTax()));
        this.sellingPriceExcludingTaxTextField.setText(String.valueOf(product.getSellingPriceExcludingTax()));

        this.product.setId(product.getId());
        this.product.setName(product.getName());
        this.product.setDescription(product.getDescription());
        this.product.setPurchasePriceExcludingTax(product.getPurchasePriceExcludingTax());
        this.product.setSellingPriceExcludingTax(product.getSellingPriceExcludingTax());

        if (!tabPane.getTabs().contains(stockMouvementsTab)) {
            tabPane.getTabs().add(stockMouvementsTab);
            initStockMovementsTableView(product);
            parentVBox.setPrefWidth(550);
        }

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

        Function<Category, Pair<String, Category>> categoryObjectFunction = category -> new Pair<>(category.getName(), category);

        categoryComboBox.getItems().add(EMPTY_CATEGORY);
        categoryComboBox.getItems().addAll(CategoryRepository.findAll().stream().map(categoryObjectFunction).toList());
        categoryComboBox.getSelectionModel().selectFirst();
    }

    private void initTaxRatesComboBox() {
        taxRateComboBox.setEditable(false);

        taxRateComboBox.setCellFactory(x -> new TaxRateComboCell());
        taxRateComboBox.setButtonCell(new TaxRateComboCell());

        Function<TaxRate, Pair<String, TaxRate>> taxRateObjectFunction = taxRate -> new Pair<>(taxRate.getLabel(), taxRate);

        taxRateComboBox.getItems().add(EMPTY_TAX_RATE);
        taxRateComboBox.getItems().addAll(TaxRateRepository.findAll().stream().map(taxRateObjectFunction).toList());
        taxRateComboBox.getSelectionModel().selectFirst();
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }

    private void displayErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
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
