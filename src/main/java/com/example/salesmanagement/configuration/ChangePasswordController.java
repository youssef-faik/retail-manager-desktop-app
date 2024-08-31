package com.example.salesmanagement.configuration;

import com.example.salesmanagement.AuthenticationService;
import com.example.salesmanagement.user.Encryptor;
import com.example.salesmanagement.user.User;
import com.example.salesmanagement.user.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangePasswordController implements Initializable {
    @FXML
    private PasswordField confirmNewPasswordField;

    @FXML
    private TextField confirmNewPasswordTextField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private TextField newPasswordTextField;

    @FXML
    private PasswordField oldpasswordField;

    @FXML
    private TextField oldpasswordTextField;

    @FXML
    private Button saveButton;

    @FXML
    private CheckBox showPasswordCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.setOnAction(e -> changePassword());

        showPasswordCheckBox.setOnAction(event -> {
            oldpasswordField.setVisible(!showPasswordCheckBox.isSelected());
            oldpasswordTextField.setVisible(showPasswordCheckBox.isSelected());

            newPasswordField.setVisible(!showPasswordCheckBox.isSelected());
            newPasswordTextField.setVisible(showPasswordCheckBox.isSelected());

            confirmNewPasswordField.setVisible(!showPasswordCheckBox.isSelected());
            confirmNewPasswordTextField.setVisible(showPasswordCheckBox.isSelected());
        });

        oldpasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            oldpasswordTextField.setText(newValue);
        });

        oldpasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            oldpasswordField.setText(newValue);
        });

        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            newPasswordTextField.setText(newValue);
        });

        newPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            newPasswordField.setText(newValue);
        });

        confirmNewPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmNewPasswordTextField.setText(newValue);
        });

        confirmNewPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmNewPasswordField.setText(newValue);
        });


    }

    private void changePassword() {
        User user = AuthenticationService.getCurrentAuthenticatedUser();


        if (!user.getPassword().equals(Encryptor.encryptPassword(oldpasswordField.getText()))) {
            System.out.println("Wrong old password");
            return;
        }

        if (!newPasswordField.getText().equals(confirmNewPasswordField.getText())) {
            System.out.println("New password doesn't match confirm password");
            return;
        }

        boolean passwordWasUpdated = UserRepository.updatePassword(user.getId(), Encryptor.encryptPassword(newPasswordField.getText()));

        if (passwordWasUpdated) {
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
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
}
