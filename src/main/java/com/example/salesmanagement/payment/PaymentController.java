package com.example.salesmanagement.payment;

import com.example.salesmanagement.document.DocumentController;
import com.example.salesmanagement.document.PaymentFormEntry;
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
import java.util.regex.Pattern;

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
    private DocumentController documentController;

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
        if (documentController != null) {
            paymentAmountTextField.setText(documentController.getRemainingAmount());
        }

        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });

        addButton.setOnAction(e -> {
            if (paymentDateDatePicker.getValue() == null) {
                displayErrorAlert("La date de règlement saisie est invalide");
                return;
            }

            String paymentAmountText = paymentAmountTextField.getText().trim();
            if (paymentAmountText.isBlank()) {
                displayErrorAlert("Montant est obligatoire");
                return;
            }

            BigDecimal paymentAmount;
            try {
                paymentAmount = new BigDecimal(paymentAmountText);
            } catch (Exception exception) {
                displayErrorAlert("le montant n'est pas valide");
                return;
            }

            BigDecimal remainingAmount = BigDecimal.valueOf(Double.parseDouble(documentController.getRemainingAmount()));
            if (!(paymentAmount.compareTo(BigDecimal.ZERO) > 0
                    && paymentAmount.compareTo(remainingAmount) <= 0)
            ) {
                displayErrorAlert("le montant doit être comprise entre 0 et " + remainingAmount.toPlainString());
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
                    String paymentReferenceText = paymentReferenceTextField.getText().trim();

                    if (paymentReferenceText.isBlank()) {
                        displayErrorAlert("Reference est obligatoire");
                        return;
                    }

                    if (!Pattern.matches("^\\d+$", paymentReferenceText)) {
                        displayErrorAlert("Reference ne doit contenir que des chiffres");
                        return;
                    }

                    String bankNameText = bankNameTextField.getText().trim();

                    if (bankNameText.isBlank()) {
                        displayErrorAlert("Banque est obligatoire");
                        return;
                    }

                    BankTransfer bankTransfer = new BankTransfer(
                            paymentAmount,
                            paymentDateDatePicker.getValue(),
                            "",
                            paymentReferenceText,
                            PaymentMethod.BANK_TRANSFER,
                            bankNameText
                    );

                    paymentsTableView.getItems().add(new PaymentFormEntry(bankTransfer));
                    break;
                case CHECK:
                    String bankText = bankNameTextField.getText().trim();

                    if (bankText.isBlank()) {
                        displayErrorAlert("Banque est obligatoire");
                        return;
                    }


                    String checkNumberText = checkNumberTextField.getText().trim();

                    if (checkNumberText.isBlank()) {
                        displayErrorAlert("Numéro de chéque est obligatoire");
                        return;
                    }

                    if (!Pattern.matches("^\\d+$", checkNumberText)) {
                        displayErrorAlert("Le numéro de chéque ne doit contenir que des chiffres");
                        return;
                    }


                    String payeeNameText = payeeNameTextField.getText().trim();

                    if (payeeNameText.isBlank()) {
                        displayErrorAlert("Nom du bénéficiaire est obligatoire");
                        return;
                    }


                    String senderAccountText = senderAccountTextField.getText().trim();

                    if (senderAccountText.isBlank()) {
                        displayErrorAlert("Compte expéditeur est obligatoire");
                        return;
                    }

                    if (!Pattern.matches("^\\d+$", senderAccountText)) {
                        displayErrorAlert("Le compte expéditeur ne doit contenir que des chiffres");
                        return;
                    }


                    CheckStatus checkStatus = checkStatusComboBox.getSelectionModel().getSelectedItem();
                    if (checkStatus == null) {
                        displayErrorAlert("Statut est obligatoire");
                        return;
                    }

                    if (checkDueDateDatePicker.getValue() == null) {
                        displayErrorAlert("La date d'échéance saisie est invalide");
                        return;
                    }

                    Check check = new Check(
                            paymentAmount,
                            paymentDateDatePicker.getValue(),
                            payeeNameText,
                            senderAccountText,
                            senderAccountText,
                            PaymentMethod.CHECK,
                            checkDueDateDatePicker.getValue(),
                            bankText,
                            checkStatus
                    );

                    paymentsTableView.getItems().add(new PaymentFormEntry(check));
                    break;
            }

            remainingAmount = BigDecimal.valueOf(Double.parseDouble(documentController.getRemainingAmount()));
            resetForm();
            displaySuccessAlert();

            if (documentController != null && remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
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

                if (documentController != null) {
                    paymentAmountTextField.setText(documentController.getRemainingAmount());
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

        if (documentController != null) {
            paymentAmountTextField.setText(documentController.getRemainingAmount());
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
            documentController.updatePaidAndRemainingAmounts();

            displaySuccessAlert();
        });
    }

    public void setSalesDocumentController(DocumentController documentController) {
        this.documentController = documentController;
    }

    public void refreshPaymentAmount() {
        if (documentController != null) {
            paymentAmountTextField.setText(documentController.getRemainingAmount());
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
