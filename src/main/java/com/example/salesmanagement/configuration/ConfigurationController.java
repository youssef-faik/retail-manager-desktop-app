package com.example.salesmanagement.configuration;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

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
    private TextField invoiceNumberTextField;
    @FXML
    private TextField creditInvoiceNumberTextField;
    @FXML
    private TextField commercialRegistrationNumberTextField;
    @FXML
    private Button saveButton;
    @FXML
    private CheckBox printHeaderCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
            invoiceNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_INVOICE_NUMBER));
            creditInvoiceNumberTextField.setText(allConfigurations.get(ConfigKey.NEXT_CREDIT_INVOICE_NUMBER));
            printHeaderCheckBox.setSelected(Boolean.parseBoolean(allConfigurations.get(ConfigKey.PRINT_INVOICE_HEADING)));
        });

        thread.start();

        saveButton.setOnAction(event -> {
            allConfigurations.put(ConfigKey.COMPANY_NAME, companyNameTextField.getText());
            allConfigurations.put(ConfigKey.COMMON_IDENTIFIER_NUMBER, commonCompanyIdentifierTextField.getText());
            allConfigurations.put(ConfigKey.TAX_IDENTIFIER_NUMBER, taxIdentificationNumberTextField.getText());
            allConfigurations.put(ConfigKey.COMMERCIAL_REGISTRATION_NUMBER, commercialRegistrationNumberTextField.getText());
            allConfigurations.put(ConfigKey.COMPANY_PATENT_NUMBER, patentNumberTextField.getText());
            allConfigurations.put(ConfigKey.COMPANY_PHONE_NUMBER, phoneNumberTextField.getText());
            allConfigurations.put(ConfigKey.COMPANY_FIXED_PHONE_NUMBER, fixedPhoneNumberTextField.getText());
            allConfigurations.put(ConfigKey.BUSINESS_ADDRESS, addressTextArea.getText());
            allConfigurations.put(ConfigKey.COMPANY_EMAIL_ADDRESS, emailTextField.getText());
            allConfigurations.put(ConfigKey.NEXT_INVOICE_NUMBER, invoiceNumberTextField.getText());
            allConfigurations.put(ConfigKey.NEXT_CREDIT_INVOICE_NUMBER, creditInvoiceNumberTextField.getText());
            allConfigurations.put(ConfigKey.PRINT_INVOICE_HEADING, String.valueOf(printHeaderCheckBox.isSelected()));
            configuration.setConfigurationValues(allConfigurations);
            displaySuccessAlert();
        });
    }

    private void displaySuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Operation effectué avec success");
        alert.showAndWait();
    }
}
