package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.report.InvoiceReportManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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

    private InvoiceRepository invoiceRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invoiceRepository = new InvoiceRepository();

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initInvoicesTableView();
        refreshInvoicesTable();
    }

    public void addInvoice(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("form-invoice.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        refreshInvoicesTable();
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
        TableColumn<Invoice, String> issueDateColumn = new TableColumn<>("Date d'émission");
        TableColumn<Invoice, String> clientColumn = new TableColumn<>("Client");
        TableColumn<Invoice, String> totalExcludingTaxesColumn = new TableColumn<>("Total (HT)");
        TableColumn<Invoice, String> totalTaxesColumn = new TableColumn<>("Taxes");
        TableColumn<Invoice, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");
        TableColumn<Invoice, String> actionColumn = new TableColumn<>("Actions");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient().getName()));
        totalExcludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalExcludingTaxes"));
        totalTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalTaxes"));
        totalIncludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalIncludingTaxes"));

        actionColumn.setMinWidth(90);
        actionColumn.setMaxWidth(90);
        actionColumn.setResizable(false);
        actionColumn.setReorderable(false);

        invoicesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        invoicesTableView.getColumns().addAll(
                idColumn,
                clientColumn,
                issueDateColumn,
                totalExcludingTaxesColumn,
                totalTaxesColumn,
                totalIncludingTaxesColumn,
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
        invoicesTableView.setItems(invoiceRepository.findAllJoinClient());

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
}
