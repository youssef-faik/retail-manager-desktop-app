package com.example.gestioncommercial.payment;

import com.example.gestioncommercial.invoice.InvoiceController;
import com.example.gestioncommercial.invoice.InvoicePaymentEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {
    @FXML
    private Button addPaymentButton;

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

    private TableView<InvoicePaymentEntry> paymentsTableView;
    private Payment selectedPayment;
    private InvoiceController invoiceController;

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
        if (invoiceController != null) {
            paymentAmountTextField.setText(invoiceController.getRemainingAmount());
        }

        addPaymentButton.setOnAction(e -> {
            switch (paymentMethodComboBox.getSelectionModel().getSelectedItem()) {
                case CASH:
                    Cash cash = new Cash(
                            BigDecimal.valueOf(Double.parseDouble(paymentAmountTextField.getText())),
                            paymentDateDatePicker.getValue(),
                            PaymentMethod.CASH,
                            CashFlowType.REVENUE
                    );
                    paymentsTableView.getItems().add(new InvoicePaymentEntry(cash));
                    break;
                case BANK_TRANSFER:
                    BankTransfer bankTransfer = new BankTransfer(
                            BigDecimal.valueOf(Double.parseDouble(paymentAmountTextField.getText())),
                            paymentDateDatePicker.getValue(),
                            "",
                            paymentReferenceTextField.getText(),
                            PaymentMethod.BANK_TRANSFER,
                            bankNameTextField.getText()
                    );
                    paymentsTableView.getItems().add(new InvoicePaymentEntry(bankTransfer));
                    break;
                case CHECK:
                    Check check = new Check(
                            BigDecimal.valueOf(Double.parseDouble(paymentAmountTextField.getText())),
                            paymentDateDatePicker.getValue(),
                            payeeNameTextField.getText(),
                            senderAccountTextField.getText(),
                            checkNumberTextField.getText(),
                            PaymentMethod.CHECK,
                            checkDueDateDatePicker.getValue(),
                            bankNameTextField.getText(),
                            checkStatusComboBox.getSelectionModel().getSelectedItem()
                    );
                    paymentsTableView.getItems().add(new InvoicePaymentEntry(check));
                    break;
            }

            resetForm();
            displaySuccessAlert();
        });
    }

    private void initPaymentMethodComboBox() {
        ObservableList<PaymentMethod> paymentMethods = FXCollections.observableArrayList();
        paymentMethods.addAll(PaymentMethod.CASH, PaymentMethod.CHECK, PaymentMethod.BANK_TRANSFER);
        paymentMethodComboBox.setItems(paymentMethods);

        paymentMethodComboBox.setOnAction(e -> {
            if (paymentMethodComboBox.getSelectionModel().getSelectedItem() != null) {
                addPaymentButton.setDisable(false);
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

                if (invoiceController != null) {
                    paymentAmountTextField.setText(invoiceController.getRemainingAmount());
                }
            }
        });

        paymentMethodComboBox.getSelectionModel().selectFirst();
    }

    public void setPaymentsTableView(TableView<InvoicePaymentEntry> paymentsTableView) {
        this.paymentsTableView = paymentsTableView;
    }

    private void resetForm() {
        paymentMethodComboBox.getSelectionModel().clearSelection();
        paymentMethodComboBox.getSelectionModel().selectFirst();

        paymentDateDatePicker.setValue(LocalDate.now());
        if (invoiceController != null) {
            paymentAmountTextField.setText(invoiceController.getRemainingAmount());
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
        addPaymentButton.setDisable(false);

        addPaymentButton.setText("Modifier");
        addPaymentButton.setOnAction(e -> {
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
            invoiceController.refreshPaidAndRemainingAmounts();

            displaySuccessAlert();
        });
    }

    public void setInvoiceController(InvoiceController invoiceController) {
        this.invoiceController = invoiceController;
    }

    public void refreshPaymentAmount() {
        if (invoiceController != null) {
            paymentAmountTextField.setText(invoiceController.getRemainingAmount());
        }
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }

}
