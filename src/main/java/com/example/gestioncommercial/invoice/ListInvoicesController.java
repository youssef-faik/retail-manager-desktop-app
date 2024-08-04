package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.report.InvoiceReportManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.sf.jasperreports.engine.JRException;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListInvoicesController implements Initializable {
    @FXML
    private TableView<Invoice> invoicesTableView;
    @FXML
    private Button deleteButton, updateButton;
    @FXML
    private TextField searchInvoiceTextField;

    private InvoiceRepository invoiceRepository;
    ObservableList<Invoice> invoiceObservableList;
    FilteredList<Invoice> filteredInvoiceList;
    SortedList<Invoice> invoiceSortedList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invoiceRepository = new InvoiceRepository();

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initInvoicesTableView();
        refreshInvoicesTable();

        filteredInvoiceList = new FilteredList<>(invoiceObservableList);
        searchInvoiceTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredInvoiceList.setPredicate(invoice -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                try {
                    Long value = Long.valueOf(searchInvoiceTextField.getText());

                    if (Objects.equals(invoice.getReference(), value)) {
                        return true;
                    }
                }
                catch (Exception ignored) {
                }

                if (invoice.getClient().getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;

            });
        });

        invoiceSortedList = new SortedList<>(filteredInvoiceList);
        invoiceSortedList.comparatorProperty().bind(invoicesTableView.comparatorProperty());
        invoicesTableView.setItems(invoiceSortedList);
    }

    public void addInvoice(ActionEvent actionEvent) throws IOException {
        VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("form-invoice.fxml")));
        Button button = (Button) actionEvent.getSource();
        BorderPane borderPane = (BorderPane) button.getScene().getRoot();
        borderPane.setCenter(pane);
    }

    public void updateInvoice(ActionEvent actionEvent) throws IOException {
        Invoice selectedInvoice = invoicesTableView.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-invoice.fxml"));
            Parent root = fxmlLoader.load();

            InvoiceController invoiceController = fxmlLoader.getController();

            Invoice invoice = invoiceRepository.findById(selectedInvoice.getId());
            invoiceController.initInvoiceUpdate(invoice);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            refreshInvoicesTable();
        }
    }

    public void deleteInvoice(ActionEvent actionEvent) throws SQLException {
        Invoice selectedInvoice = invoicesTableView.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cette facture?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                invoiceRepository.deleteById(selectedInvoice.getId());
                refreshInvoicesTable();
            }
        }
    }

    private void initInvoicesTableView() {
        TableColumn<Invoice, String> idColumn = new TableColumn<>("ID");
        TableColumn<Invoice, String> referenceColumn = new TableColumn<>("Référence");
        TableColumn<Invoice, String> clientColumn = new TableColumn<>("Client");
        TableColumn<Invoice, String> issueDateColumn = new TableColumn<>("Date d'émission");
        TableColumn<Invoice, String> statusColumn = new TableColumn<>("Status");
        TableColumn<Invoice, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");
        TableColumn<Invoice, String> paidAmountColumn = new TableColumn<>("Montant payé");
        TableColumn<Invoice, String> remainingAmountTaxesColumn = new TableColumn<>("Montant restant");
        TableColumn<Invoice, String> dueDateColumn = new TableColumn<>("Date d'échéance");
        TableColumn<Invoice, String> actionColumn = new TableColumn<>("Actions");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient().getName()));
        remainingAmountTaxesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalIncludingTaxes().subtract(cellData.getValue().getPaidAmount()).toPlainString()));
        totalIncludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalIncludingTaxes"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paidAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));

        dueDateColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getDueDate() == null ? "N/A" : cellData.getValue().getDueDate().toString());
        });

        referenceColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getReference() == 0L ? "N/A" : cellData.getValue().getReference().toString());
        });

        idColumn.setVisible(false);

        referenceColumn.setMinWidth(70);
        referenceColumn.setMaxWidth(70);
        referenceColumn.setReorderable(false);

        actionColumn.setMinWidth(90);
        actionColumn.setMaxWidth(90);
        actionColumn.setResizable(false);
        actionColumn.setReorderable(false);

        invoicesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        invoicesTableView.getColumns().addAll(
                idColumn,
                referenceColumn,
                clientColumn,
                issueDateColumn,
                dueDateColumn,
                statusColumn,
                totalIncludingTaxesColumn,
                paidAmountColumn,
                remainingAmountTaxesColumn,
                actionColumn
        );

        Callback<TableColumn<Invoice, String>, TableCell<Invoice, String>> cellFactory =
                (TableColumn<Invoice, String> param) -> {
                    // make cell containing button

                    return new TableCell<>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            //that cell created only on non-empty rows
                            if (empty) {
                                setGraphic(null);
                                setText(null);

                            } else {
                                Button printButton = new Button("Imprimer");
                                Text icon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PRINT);
                                printButton.setGraphic(icon);


                                printButton.setOnMouseClicked((MouseEvent event) -> {
                                    try {
                                        TableRow tableRow = (TableRow) printButton.getParent().getParent().getParent();
                                        Invoice selectedInvoice = (Invoice) tableRow.getItem();

                                        Invoice invoice = invoiceRepository.findById(selectedInvoice.getId());

                                        try {
                                            InvoiceReportManager invoiceReportManager = new InvoiceReportManager();
                                            invoiceReportManager.displayInvoiceReport(invoice);
                                        } catch (JRException e) {
                                            throw new RuntimeException(e);
                                        }

                                    } catch (Exception ex) {
                                        Logger.getLogger(InvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });

                                HBox actionsHBox = new HBox(printButton);
                                actionsHBox.setStyle("-fx-alignment:center");
                                setGraphic(actionsHBox);

                                setText(null);
                            }
                        }

                    };
                };

        actionColumn.setCellFactory(cellFactory);

        invoicesTableView.setOnMouseClicked(e -> {
            if (invoicesTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    private void refreshInvoicesTable() {
        invoiceObservableList = invoiceRepository.findAllJoinClient();

        filteredInvoiceList = new FilteredList<>(invoiceObservableList);
        invoiceSortedList = new SortedList<>(filteredInvoiceList);
        invoiceSortedList.comparatorProperty().bind(invoicesTableView.comparatorProperty());
        invoicesTableView.setItems(invoiceSortedList);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
}
