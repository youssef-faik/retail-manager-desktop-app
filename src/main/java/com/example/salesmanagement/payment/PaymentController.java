package com.example.salesmanagement.payment;

import com.example.salesmanagement.salesdocument.PaymentFormEntry;
import com.example.salesmanagement.salesdocument.SalesDocumentController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {
    @FXML
    private Button addButton, cancelButton;

    @FXML
    private HBox bankHBox, checkDueDateHBox, checkStatusHBox, referenceHBox, checkNumberHBox, senderAccountHBox, payeeNameHBox, paymentMethodHBox;

    @FXML
    private TextField bankNameTextField, paymentAmountTextField, paymentReferenceTextField, checkNumberTextField, payeeNameTextField, senderAccountTextField;

    @FXML
    private DatePicker checkDueDateDatePicker, paymentDateDatePicker;

    @FXML
    private ComboBox<CheckStatus> checkStatusComboBox;

    @FXML
    private ComboBox<PaymentMethod> paymentMethodComboBox;

    private TableView<PaymentFormEntry> paymentsTableView;
    private Payment selectedPayment;
    private SalesDocumentController salesDocumentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initPaymentMethodComboBox();
        checkStatusComboBox.getItems().addAll(CheckStatus.values());

        bankHBox.setVisible(false);
        checkDueDateHBox.setVisible(false);
        checkStatusHBox.setVisible(false);
        payeeNameHBox.setVisible(false);
        checkNumberHBox.setVisible(false);
        senderAccountHBox.setVisible(false);
        referenceHBox.setVisible(false);

        paymentDateDatePicker.setValue(LocalDate.now());
        if (salesDocumentController != null) {
            paymentAmountTextField.setText(salesDocumentController.getRemainingAmount());
        }

        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });

        addButton.setOnAction(e -> {
            BigDecimal paymentAmount = BigDecimal.valueOf(Double.parseDouble(paymentAmountTextField.getText()));
            BigDecimal remainingAmount = BigDecimal.valueOf(Double.parseDouble(salesDocumentController.getRemainingAmount()));

            if (salesDocumentController != null
                    && paymentAmount.compareTo(remainingAmount) > 0 || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                displayErrorAlert("Le montant saisie est invalide");
                return;
            }

            switch (paymentMethodComboBox.getSelectionModel().getSelectedItem()) {
                case CASH:
                    Cash cash = new Cash(
                            paymentAmount,
                            paymentDateDatePicker.getValue(),
                            PaymentMethod.CASH,
                            CashFlowType.REVENUE
                    );
                    paymentsTableView.getItems().add(new PaymentFormEntry(cash));
                    break;
                case BANK_TRANSFER:
                    BankTransfer bankTransfer = new BankTransfer(
                            paymentAmount,
                            paymentDateDatePicker.getValue(),
                            "",
                            paymentReferenceTextField.getText(),
                            PaymentMethod.BANK_TRANSFER,
                            bankNameTextField.getText()
                    );
                    paymentsTableView.getItems().add(new PaymentFormEntry(bankTransfer));
                    break;
                case CHECK:
                    Check check = new Check(
                            paymentAmount,
                            paymentDateDatePicker.getValue(),
                            payeeNameTextField.getText(),
                            senderAccountTextField.getText(),
                            checkNumberTextField.getText(),
                            PaymentMethod.CHECK,
                            checkDueDateDatePicker.getValue(),
                            bankNameTextField.getText(),
                            checkStatusComboBox.getSelectionModel().getSelectedItem()
                    );
                    paymentsTableView.getItems().add(new PaymentFormEntry(check));
                    break;
            }

            remainingAmount = BigDecimal.valueOf(Double.parseDouble(salesDocumentController.getRemainingAmount()));
            resetForm();
            displaySuccessAlert();

            if (salesDocumentController != null && remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.close();
            }
        });
    }

    private void initPaymentMethodComboBox() {
        ObservableList<PaymentMethod> paymentMethods = FXCollections.observableArrayList();
        paymentMethods.addAll(PaymentMethod.CASH, PaymentMethod.CHECK, PaymentMethod.BANK_TRANSFER);
        paymentMethodComboBox.setItems(paymentMethods);

        paymentMethodComboBox.setOnAction(e -> {
            if (paymentMethodComboBox.getSelectionModel().getSelectedItem() != null) {
                addButton.setDisable(false);
                VBox parentVBox = (VBox) paymentMethodHBox.getParent();

                switch (paymentMethodComboBox.getSelectionModel().getSelectedItem()) {
                    case CASH:
                        bankHBox.setVisible(false);
                        checkDueDateHBox.setVisible(false);
                        checkStatusHBox.setVisible(false);
                        payeeNameHBox.setVisible(false);
                        checkNumberHBox.setVisible(false);
                        senderAccountHBox.setVisible(false);
                        referenceHBox.setVisible(false);
                        break;
                    case BANK_TRANSFER:
                        bankHBox.setVisible(true);
                        referenceHBox.setVisible(true);

                        if (!parentVBox.getChildren().contains(referenceHBox)) {
                            parentVBox.getChildren().add(4, referenceHBox);
                        }

                        checkDueDateHBox.setVisible(false);
                        checkStatusHBox.setVisible(false);
                        payeeNameHBox.setVisible(false);
                        checkNumberHBox.setVisible(false);
                        senderAccountHBox.setVisible(false);
                        break;
                    case CHECK:
                        bankHBox.setVisible(true);
                        checkDueDateHBox.setVisible(true);
                        checkStatusHBox.setVisible(true);
                        payeeNameHBox.setVisible(true);
                        checkNumberHBox.setVisible(true);
                        senderAccountHBox.setVisible(true);
                        referenceHBox.setVisible(false);

                        parentVBox.getChildren().remove(referenceHBox);

                        break;
                }

                if (salesDocumentController != null) {
                    paymentAmountTextField.setText(salesDocumentController.getRemainingAmount());
                }
            }
        });

        paymentMethodComboBox.getSelectionModel().selectFirst();
    }

    public void setPaymentsTableView(TableView<PaymentFormEntry> paymentsTableView) {
        this.paymentsTableView = paymentsTableView;
    }

    private void resetForm() {
        paymentMethodComboBox.getSelectionModel().clearSelection();
        paymentMethodComboBox.getSelectionModel().selectFirst();

        paymentDateDatePicker.setValue(LocalDate.now());

        if (salesDocumentController != null) {
            paymentAmountTextField.setText(salesDocumentController.getRemainingAmount());
        } else {
            paymentAmountTextField.clear();
        }

        paymentReferenceTextField.clear();
        checkDueDateDatePicker.setValue(LocalDate.now());
        checkStatusComboBox.getSelectionModel().clearSelection();
        bankNameTextField.clear();
        payeeNameTextField.clear();
        senderAccountTextField.clear();
        checkNumberTextField.clear();
    }

    public void initUpdate(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;

        paymentMethodComboBox.setValue(PaymentMethod.CASH);
        paymentAmountTextField.setText(String.valueOf(selectedPayment.getAmount()));
        paymentDateDatePicker.setValue(selectedPayment.getPaymentDate());

        if (selectedPayment instanceof BankTransfer bankTransfer) {
            bankNameTextField.setText(bankTransfer.getBankName());
            paymentReferenceTextField.setText(bankTransfer.getTransactionId());
            paymentMethodComboBox.setValue(PaymentMethod.BANK_TRANSFER);
            paymentMethodComboBox.fireEvent(new javafx.event.ActionEvent());
        } else if (selectedPayment instanceof Check check) {
            bankNameTextField.setText(check.getBankName());
            checkNumberTextField.setText(check.getCheckNumber());
            payeeNameTextField.setText(check.getPayeeName());
            senderAccountTextField.setText(check.getSenderAccount());
            checkDueDateDatePicker.setValue(check.getDueDate());
            checkStatusComboBox.setValue(check.getCheckStatus());
            paymentMethodComboBox.setValue(PaymentMethod.CHECK);
            paymentMethodComboBox.fireEvent(new javafx.event.ActionEvent());
        }

        paymentMethodComboBox.setDisable(true);
        addButton.setDisable(false);

        addButton.setText("Modifier");
        addButton.setOnAction(e -> {
            this.selectedPayment.setPaymentDate(paymentDateDatePicker.getValue());
            this.selectedPayment.setAmount(BigDecimal.valueOf(Double.parseDouble(paymentAmountTextField.getText())));

            switch (paymentMethodComboBox.getSelectionModel().getSelectedItem()) {
                case BANK_TRANSFER:
                    assert selectedPayment instanceof BankTransfer;
                    BankTransfer bankTransfer = (BankTransfer) selectedPayment;
                    bankTransfer.setBankName(bankNameTextField.getText());
                    bankTransfer.setTransactionId(paymentReferenceTextField.getText());
                    break;
                case CHECK:
                    assert selectedPayment instanceof Check;
                    Check check = (Check) selectedPayment;
                    check.setBankName(bankNameTextField.getText());
                    check.setCheckNumber(checkNumberTextField.getText());
                    check.setPayeeName(payeeNameTextField.getText());
                    check.setSenderAccount(senderAccountTextField.getText());
                    check.setDueDate(checkDueDateDatePicker.getValue());
                    check.setCheckStatus(checkStatusComboBox.getSelectionModel().getSelectedItem());
                    break;
            }

            paymentsTableView.refresh();
            salesDocumentController.updatePaidAndRemainingAmounts();

            displaySuccessAlert();
        });
    }

    public void setSalesDocumentController(SalesDocumentController salesDocumentController) {
        this.salesDocumentController = salesDocumentController;
    }

    public void refreshPaymentAmount() {
        if (salesDocumentController != null) {
            paymentAmountTextField.setText(salesDocumentController.getRemainingAmount());
        }
    }


    private void displayErrorAlert() {
        displayErrorAlert("Une erreur est survenue lors de l'opération.");
    }

    private void displayErrorAlert(String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }


    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }

}
