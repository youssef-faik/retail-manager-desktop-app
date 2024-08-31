package com.example.salesmanagement;

import com.example.salesmanagement.user.Encryptor;
import com.example.salesmanagement.user.User;
import com.example.salesmanagement.user.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    VBox parentNode;
    @FXML
    private Button cancelButton;
    @FXML
    private Button loginButton;
    @FXML
    private PasswordField passwordField, confirmNewPasswordField, newPasswordField;
    @FXML
    private TextField passwordTextField, confirmNewPasswordTextField, newPasswordTextField;
    @FXML
    private CheckBox showPasswordCheckBox;
    @FXML
    private TextField usernameTextField;
    @FXML
    private HBox newPasswordHBox, confirmNewPasswordHBox, showPasswordHBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parentNode = (VBox) newPasswordHBox.getParent();
        parentNode.getChildren().remove(newPasswordHBox);
        parentNode.getChildren().remove(confirmNewPasswordHBox);

        showPasswordCheckBox.setOnAction(event -> {
            passwordField.setVisible(!showPasswordCheckBox.isSelected());
            passwordTextField.setVisible(showPasswordCheckBox.isSelected());

            newPasswordField.setVisible(!showPasswordCheckBox.isSelected());
            newPasswordTextField.setVisible(showPasswordCheckBox.isSelected());

            confirmNewPasswordField.setVisible(!showPasswordCheckBox.isSelected());
            confirmNewPasswordTextField.setVisible(showPasswordCheckBox.isSelected());
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordTextField.setText(newValue);
        });

        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordField.setText(newValue);
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

        loginButton.setOnAction(e -> login());
        cancelButton.setOnAction(e -> {
            HibernateUtil.shutdown();

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });

    }

    private void login() {
        String username = usernameTextField.getText();
        Optional<User> optionalUser = UserRepository.findByUsername(username);

        if (optionalUser.isPresent()) {
            User authenticatedUser = optionalUser.get();
            if (authenticatedUser.getPassword().equals(Encryptor.encryptPassword(passwordField.getText()))) {
                System.out.println("Login successful");
                AuthenticationService.setCurrentAuthenticatedUser(authenticatedUser);

                if (!authenticatedUser.isMustChangePassword()) {
                    displayMainStage();
                } else {
                    displayChangePasswordControls();
                }
            } else {
                System.out.println("Login failed: " + authenticatedUser.getUsername());
            }
        } else {
            System.out.println("Login failed: user not found");
        }

    }

    private void displayChangePasswordControls() {
        passwordField.setDisable(true);
        passwordTextField.setDisable(true);
        usernameTextField.setDisable(true);

        int index = parentNode.getChildren().indexOf(showPasswordHBox);
        parentNode.getChildren().add(index, newPasswordHBox);
        parentNode.getChildren().add(index + 1, confirmNewPasswordHBox);

        parentNode.getScene().getWindow().setHeight(400);

        loginButton.setText("Enregistrer");
        loginButton.setOnAction(event -> changePassword());
    }

    private void changePassword() {
        User authenticatedUser = AuthenticationService.getCurrentAuthenticatedUser();
        String encryptedNewPassword = Encryptor.encryptPassword(newPasswordField.getText());

        if (encryptedNewPassword.equals(authenticatedUser.getPassword())) {
            System.out.println("The new password is the same as the old password");
            return;
        }

        if (!newPasswordField.getText().equals(confirmNewPasswordField.getText())) {
            System.out.println("New password doesn't match confirm password");
            return;
        }


        boolean passwordWasUpdated = UserRepository.updatePassword(authenticatedUser.getId(), encryptedNewPassword);

        if (passwordWasUpdated) {
            authenticatedUser.setPassword(encryptedNewPassword);
            displaySuccessAlert();
            displayMainStage();
        } else {
            displayErrorAlert();
        }

    }


    private void displayMainStage() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));

        Scene scene = usernameTextField.getScene();
        Stage stage = (Stage) scene.getWindow();

        stage.setTitle("Gestion Commercial");
        stage.setResizable(false);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        try {
            Parent load = fxmlLoader.load();
            scene.setRoot(load);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
