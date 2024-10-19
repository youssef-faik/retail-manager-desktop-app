package com.example.salesmanagement.configuration;

import com.example.salesmanagement.document.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ConfigurationController implements Initializable {
    Map<ConfigKey, String> allConfigurations;
    @FXML
    private TextField companyNameTextField;
    @FXML
    private TextField commonCompanyIdentifierTextField;
    @FXML
    private TextField taxIdentificationNumberTextField;
    @FXML
    private TextField patentNumberTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private TextField fixedPhoneNumberTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private TextField purchaseOrderNumberTextField;
    @FXML
    private TextField purchaseDeliveryNoteNumberTextField;
    @FXML
    private TextField quotationNumberTextField;
    @FXML
    private TextField deliveryNoteNumberTextField;
    @FXML
    private TextField invoiceNumberTextField;
    @FXML
    private TextField creditInvoiceNumberTextField;
    @FXML
    private TextField commercialRegistrationNumberTextField;
    @FXML
    private Button saveCompanyInfosButton;
    @FXML
    private Button saveDocumentReferencesButton;
    @FXML
    private Button savePrintingOptionsButton;
    @FXML
    private CheckBox printHeaderCheckBox, printDeliveryNoteUnitPriceCheckBox;

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

        saveCompanyInfosButton.setEffect(dropShadow);
        saveCompanyInfosButton.setTextFill(Color.color(1, 1, 1));
        saveCompanyInfosButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

        saveDocumentReferencesButton.setEffect(dropShadow);
        saveDocumentReferencesButton.setTextFill(Color.color(1, 1, 1));
        saveDocumentReferencesButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

        savePrintingOptionsButton.setEffect(dropShadow);
        savePrintingOptionsButton.setTextFill(Color.color(1, 1, 1));
        savePrintingOptionsButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

        AppConfiguration configuration = AppConfiguration.getInstance();

        Thread thread = new Thread(() -> {
            allConfigurations = configuration.getAllConfigurations();

            companyNameTextField.setText(allConfigurations.get(ConfigKey.COMPANY_NAME));
            commonCompanyIdentifierTextField.setText(allConfigurations.get(ConfigKey.COMMON_IDENTIFIER_NUMBER));
            taxIdentificationNumberTextField.setText(allConfigurations.get(ConfigKey.TAX_IDENTIFIER_NUMBER));
            commercialRegistrationNumberTextField.setText(allConfigurations.get(ConfigKey.COMMERCIAL_REGISTRATION_NUMBER));
            patentNumberTextField.setText(allConfigurations.get(ConfigKey.COMPANY_PATENT_NUMBER));
            phoneNumberTextField.setText(allConfigurations.get(ConfigKey.COMPANY_PHONE_NUMBER));
            fixedPhoneNumberTextField.setText(allConfigurations.get(ConfigKey.COMPANY_FIXED_PHONE_NUMBER));
            addressTextArea.setText(allConfigurations.get(ConfigKey.BUSINESS_ADDRESS));
            emailTextField.setText(allConfigurations.get(ConfigKey.COMPANY_EMAIL_ADDRESS));

            purchaseOrderNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_PURCHASE_ORDER_NUMBER));
            purchaseDeliveryNoteNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_PURCHASE_DELIVERY_NOTE_NUMBER));
            quotationNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_QUOTATION_NUMBER));
            deliveryNoteNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_DELIVERY_NOTE_NUMBER));
            invoiceNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_INVOICE_NUMBER));
            creditInvoiceNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_CREDIT_INVOICE_NUMBER));

            printHeaderCheckBox.setSelected(Boolean.parseBoolean(allConfigurations.get(ConfigKey.PRINT_SALES_DOCUMENT_HEADING)));
            printDeliveryNoteUnitPriceCheckBox.setSelected(Boolean.parseBoolean(allConfigurations.get(ConfigKey.PRINT_DELIVERY_NOTE_UNIT_PRICE)));
        });

        thread.start();

        saveCompanyInfosButton.setOnAction(event -> {
            String companyNameText = companyNameTextField.getText().trim();
            if (companyNameText.isBlank()) {
                displayErrorAlert("Nom de l'entreprise est obligatoire");
                return;
            }

            allConfigurations.put(ConfigKey.COMPANY_NAME, companyNameText);

            if (setNumericCompanyInfo(commonCompanyIdentifierTextField, ConfigKey.COMMON_IDENTIFIER_NUMBER, "L'ICE ne doit contenir que des chiffres")) {
                return;
            }

            if (setNumericCompanyInfo(taxIdentificationNumberTextField, ConfigKey.TAX_IDENTIFIER_NUMBER, "Le numéro d'identification fiscale ne doit contenir que des chiffres")) {
                return;
            }

            if (setNumericCompanyInfo(commercialRegistrationNumberTextField, ConfigKey.COMMERCIAL_REGISTRATION_NUMBER, "Le numéro du registre de commerce ne doit contenir que des chiffres")) {
                return;
            }

            if (setNumericCompanyInfo(patentNumberTextField, ConfigKey.COMPANY_PATENT_NUMBER, "Le numéro de patente ne doit contenir que des chiffres")) {
                return;
            }

            allConfigurations.put(ConfigKey.COMPANY_PHONE_NUMBER, phoneNumberTextField.getText().trim());
            allConfigurations.put(ConfigKey.COMPANY_FIXED_PHONE_NUMBER, fixedPhoneNumberTextField.getText().trim());
            allConfigurations.put(ConfigKey.COMPANY_EMAIL_ADDRESS, emailTextField.getText().trim());

            String addressText = addressTextArea.getText().trim();
            if (addressText.isBlank()) {
                displayErrorAlert("Adresse est obligatoire");
                return;
            }
            allConfigurations.put(ConfigKey.BUSINESS_ADDRESS, addressText);

            configuration.setConfigurationValues(allConfigurations);
            displaySuccessAlert();
        });

        saveDocumentReferencesButton.setOnAction(event -> {
            if (setDocumentReference(
                    PurchaseOrder.class,
                    purchaseOrderNumberTextField,
                    "Le numéro initiale de bon de commande est obligatoire",
                    "la valeur du numéro initiale de bon de commande n'est pas valide",
                    "la valeur du numéro initiale de bon de commande doit être comprise entre 1 et 9,999,999",
                    "Le prochain numéro de bon de commande doit être égal ou supérieur à ",
                    ConfigKey.NEXT_PURCHASE_ORDER_NUMBER)) {
                return;
            }

            if (setDocumentReference(
                    PurchaseDeliveryNote.class,
                    purchaseDeliveryNoteNumberTextField,
                    "Le numéro initiale de bon de réception est obligatoire",
                    "la valeur du numéro initiale de bon de réception n'est pas valide",
                    "la valeur du numéro initiale de bon de réception doit être comprise entre 1 et 9,999,999",
                    "Le prochain numéro de bon de réception doit être égal ou supérieur à ",
                    ConfigKey.NEXT_PURCHASE_DELIVERY_NOTE_NUMBER)) {
                return;
            }

            if (setDocumentReference(
                    Quotation.class,
                    quotationNumberTextField,
                    "Le numéro initiale de devis est obligatoire",
                    "la valeur du numéro initiale de devis n'est pas valide",
                    "la valeur du numéro initiale de devis doit être comprise entre 1 et 9,999,999",
                    "Le prochain numéro de devis doit être égal ou supérieur à ",
                    ConfigKey.NEXT_QUOTATION_NUMBER)) {
                return;
            }

            if (setDocumentReference(
                    DeliveryNote.class,
                    deliveryNoteNumberTextField,
                    "Le numéro initiale de bon de livraison est obligatoire",
                    "la valeur du numéro initiale de bon de livraison n'est pas valide",
                    "la valeur du numéro initiale de bon de livraison doit être comprise entre 1 et 9,999,999",
                    "Le prochain numéro de bon de livraison doit être égal ou supérieur à ",
                    ConfigKey.NEXT_DELIVERY_NOTE_NUMBER)) {
                return;
            }

            if (setDocumentReference(
                    Invoice.class,
                    invoiceNumberTextField,
                    "Le numéro initiale de facture doit est obligatoire",
                    "la valeur du numéro initiale de facture doit n'est pas valide",
                    "la valeur du numéro initiale de facture doit doit être comprise entre 1 et 9,999,999",
                    "Le prochain numéro de facture doit doit être égal ou supérieur à ",
                    ConfigKey.NEXT_INVOICE_NUMBER)) {
                return;
            }

            if (setDocumentReference(
                    CreditInvoice.class,
                    creditInvoiceNumberTextField,
                    "Le numéro initiale de facture avoir est obligatoire",
                    "la valeur du numéro initiale de facture avoir n'est pas valide",
                    "la valeur du numéro initiale de facture avoir doit être comprise entre 1 et 9,999,999",
                    "Le prochain numéro de facture avoir doit être égal ou supérieur à ",
                    ConfigKey.NEXT_CREDIT_INVOICE_NUMBER)) {
                return;
            }

            configuration.setConfigurationValues(allConfigurations);
            displaySuccessAlert();
        });

        savePrintingOptionsButton.setOnAction(event -> {
            allConfigurations.put(ConfigKey.PRINT_SALES_DOCUMENT_HEADING, String.valueOf(printHeaderCheckBox.isSelected()));
            allConfigurations.put(ConfigKey.PRINT_DELIVERY_NOTE_UNIT_PRICE, String.valueOf(printDeliveryNoteUnitPriceCheckBox.isSelected()));

            configuration.setConfigurationValues(allConfigurations);
            displaySuccessAlert();
        });
    }

    private boolean setDocumentReference(Class<? extends Document> DocumentClass, TextField textField, String mandatoryValueMessage, String invalideValueMessage, String valueOutOfRangeMessage, String nextDocumentNumberMessage, ConfigKey configKey) {
        String purchaseOrderNumberText = textField.getText().trim();
        if (purchaseOrderNumberText.isBlank()) {
            displayErrorAlert(mandatoryValueMessage);
            return true;
        }

        long purchaseOrderNumber;
        try {
            purchaseOrderNumber = Long.parseLong(purchaseOrderNumberText);
        } catch (NumberFormatException e) {
            displayErrorAlert(invalideValueMessage);
            return true;
        }

        if (!(purchaseOrderNumber >= 1 && purchaseOrderNumber <= 9_999_999)
        ) {
            displayErrorAlert(valueOutOfRangeMessage);
            return true;
        }

        try {
            AppConfiguration.validateNextSalesDocumentReference(purchaseOrderNumber, DocumentClass);
        } catch (InvalideDocumentReferenceException e) {
            displayErrorAlert(nextDocumentNumberMessage + e.getNextValideDocumentReference());
            return true;
        }

        allConfigurations.put(configKey, Long.toString(purchaseOrderNumber));
        return false;
    }

    private boolean setNumericCompanyInfo(TextField textField, ConfigKey configKey, String errorMessage) {
        String text = textField.getText().trim();
        if (text.isBlank()) {
            text = null;
        } else {
            if (!Pattern.matches("^\\d+$", text)) {
                displayErrorAlert(errorMessage);
                return true;
            }
        }

        allConfigurations.put(configKey, text);
        return false;
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

}
