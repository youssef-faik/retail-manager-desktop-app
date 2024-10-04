package com.example.salesmanagement.configuration;

import com.example.salesmanagement.AuthenticationService;
import com.example.salesmanagement.user.Encryptor;
import com.example.salesmanagement.user.User;
import com.example.salesmanagement.user.UserRepository;
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
        DropShadow dropShadow = new DropShadow(
                BlurType.ONE_PASS_BOX,
                Color.color(0.6392, 0.6392, 0.6392, 1.0),
                10.0,
                0,
                0,
                0
        );

        saveButton.setEffect(dropShadow);
        saveButton.setTextFill(Color.color(1, 1, 1));
        saveButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

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
        String oldPasswordText = oldpasswordField.getText();
        if (oldPasswordText.isBlank()) {
            displayErrorAlert("Ancien mot de passe est obligatoire");
            return;
        }

        User user = AuthenticationService.getCurrentAuthenticatedUser();
        if (!user.getPassword().equals(Encryptor.encryptPassword(oldPasswordText))) {
            displayErrorAlert("Ancien mot de passe erroné");
            System.out.println("Ancien mot de passe erroné");
            return;
        }

        String newPasswordText = newPasswordField.getText();
        if (newPasswordText.isBlank()) {
            displayErrorAlert("Le nouveau mot de passe est obligatoire");
            return;
        }

        String confirmNewPasswordText = confirmNewPasswordField.getText();
        if (confirmNewPasswordText.isBlank()) {
            displayErrorAlert("La confirmation du nouveau mot de passe est requise");
            return;
        }

        if (!newPasswordText.equals(confirmNewPasswordText)) {
            displayErrorAlert("Le nouveau mot de passe ne correspond pas au mot de passe confirmé");
            System.out.println("Le nouveau mot de passe ne correspond pas au mot de passe confirmé");
            return;
        }

        String encryptedNewPassword = Encryptor.encryptPassword(newPasswordText);

        if (encryptedNewPassword.equals(user.getPassword())) {
            displayErrorAlert("Le nouveau mot de passe doit être différent de l'ancien mot de passe");
            System.out.println("Le nouveau mot de passe doit être différent de l'ancien mot de passe");
            return;
        }

        boolean passwordWasUpdated = UserRepository.updatePassword(user.getId(), encryptedNewPassword);

        if (passwordWasUpdated) {
            user.setPassword(encryptedNewPassword);
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
}
