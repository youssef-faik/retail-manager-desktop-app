package com.example.salesmanagement;

import com.example.salesmanagement.document.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    public HBox logoHBox;
    @FXML
    public Button dashboardNavBarButton, clientsNavBarButton, suppliersNavBarButton, productsNavBarButton, stockNavBarButton, PurchaseDeliveryNoteNavBarButton, invoiceNavBarButton, usersNavBarButton, settingsNavBarButton, signoutNavBarButton;
    public Text homeLabel, managementLabel, configLabel;
    @FXML
    private BorderPane borderPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initButtonHoverEffect();
    }

    public void displayDashboard() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("dashboard.fxml")));
        borderPane.setCenter(pane);
    }

    public void addSupplier() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("supplier/form-supplier.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listSuppliers() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("supplier/list-suppliers.fxml")));
        borderPane.setCenter(pane);
    }

    public void addClient() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client/form-client.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listClients() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client/list-clients.fxml")));
        borderPane.setCenter(pane);
    }

    public void addProduct() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("product/form-product.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listProducts() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("product/list-products.fxml")));
        borderPane.setCenter(pane);
    }

    public void addCategory() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("category/form-category.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listCategories() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("category/list-categories.fxml")));
        borderPane.setCenter(pane);
    }

    public void addTaxRate() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("taxrate/form-taxrate.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listTaxRates() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("taxrate/list-taxrates.fxml")));
        borderPane.setCenter(pane);
    }

    public void addPurchaseOrder() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(PurchaseOrder.class);

        borderPane.setCenter(pane);
    }

    public void listPurchaseOrders() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(PurchaseOrder.class);

        borderPane.setCenter(pane);
    }

    public void addPurchaseDeliveryNote() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(PurchaseDeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void listPurchaseDeliveryNotes() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(PurchaseDeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void listQuotations() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(Quotation.class);

        borderPane.setCenter(pane);
    }

    public void addQuotation() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(Quotation.class);

        borderPane.setCenter(pane);
    }

    public void listDeliveryNotes() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(DeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void addDeliveryNote() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(DeliveryNote.class);

        borderPane.setCenter(pane);
    }

    public void addInvoice() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(Invoice.class);

        borderPane.setCenter(pane);
    }

    public void listInvoices() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(Invoice.class);

        borderPane.setCenter(pane);
    }

    public void listCreditInvoices() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/documents.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentsController documentsController = fxmlLoader.getController();
        documentsController.setFormClass(CreditInvoice.class);

        borderPane.setCenter(pane);
    }

    public void addCreditInvoice() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("document/form-document.fxml"));
        VBox pane = fxmlLoader.load();

        DocumentController documentController = fxmlLoader.getController();
        documentController.setDocumentType(CreditInvoice.class);

        borderPane.setCenter(pane);
    }

    public void editConfig() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("configuration/form-config.fxml")));
        borderPane.setCenter(pane);
    }

    public void listStockMouvements() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("stockmouvement/stock_mouvements.fxml")));
        borderPane.setCenter(pane);
    }

    public void addUser() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("user/form-user.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void listUsers() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("user/list-users.fxml")));
        borderPane.setCenter(pane);
    }

    public void changePassword() throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("configuration/form-change-password.fxml")));
        borderPane.setCenter(pane);
    }

    public void signout(ActionEvent actionEvent) {
        Stage stage = (Stage) borderPane.getScene().getWindow();

        try {
            VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("form-login.fxml")));
            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.sizeToScene();


            final Rectangle2D bounds = Screen.getPrimary().getBounds();

            stage.setX(bounds.getMinX() + bounds.getWidth() / 2 - scene.getWidth() / 2);
            stage.setY(bounds.getMinY() + bounds.getHeight() / 2 - scene.getHeight() / 2 - 60);
        } catch (IOException ex) {
            ex.printStackTrace();
            // Display error dialog or handle gracefully
        }

        AuthenticationService.setCurrentAuthenticatedUser(null);
        stage.show();
    }

    private void initButtonHoverEffect() {
        Button[] buttons = {dashboardNavBarButton, clientsNavBarButton, suppliersNavBarButton, productsNavBarButton, stockNavBarButton, PurchaseDeliveryNoteNavBarButton, invoiceNavBarButton, usersNavBarButton, settingsNavBarButton, signoutNavBarButton};

        for (Button button : buttons) {
            button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.4)"));
            button.setOnMouseExited(event -> button.setStyle("-fx-background-color: rgba(0, 0, 0, 0)"));
        }
    }
}

