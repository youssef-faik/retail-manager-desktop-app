package com.example.salesmanagement;

import com.example.salesmanagement.user.Encryptor;
import com.example.salesmanagement.user.User;
import com.example.salesmanagement.user.UserRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    VBox parentNode;
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
    private VBox newPasswordVBox, confirmNewPasswordVBox, showPasswordVBox;

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

        loginButton.setEffect(dropShadow);
        loginButton.setTextFill(Color.color(1, 1, 1));
        loginButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));
        ((Text) loginButton.getGraphic()).setFill(Color.WHITE);

        parentNode = (VBox) newPasswordVBox.getParent();
        parentNode.getChildren().remove(newPasswordVBox);
        parentNode.getChildren().remove(confirmNewPasswordVBox);

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
    }

    private void login() {
        String username = usernameTextField.getText();
        String password = passwordField.getText();

        if (username.isBlank()) {
            displayErrorAlert("Identifiant est obligatoire");
            return;
        }

        if (password.isBlank()) {
            displayErrorAlert("Mot de passe est obligatoire");
            return;
        }

        Optional<User> optionalUser = UserRepository.findByUsername(username);

        if (optionalUser.isPresent()) {
            User authenticatedUser = optionalUser.get();
            if (authenticatedUser.getPassword().equals(Encryptor.encryptPassword(password))) {
                System.out.println("Login successful");
                AuthenticationService.setCurrentAuthenticatedUser(authenticatedUser);

                if (!authenticatedUser.isMustChangePassword()) {
                    displayMainStage();
                } else {
                    displayChangePasswordControls();
                }
            } else {
                System.out.println("Login failed: " + authenticatedUser.getUsername());
                displayErrorAlert("Identifiant ou mot de passe est incorrect");
            }
        } else {
            System.out.println("Login failed: user not found");
            displayErrorAlert("Identifiant ou mot de passe est incorrect");
        }

    }

    private void displayChangePasswordControls() {
        passwordField.setDisable(true);
        passwordTextField.setDisable(true);
        usernameTextField.setDisable(true);

        parentNode.getScene().getWindow().setHeight(640);

        int index = parentNode.getChildren().indexOf(showPasswordVBox);
        parentNode.getChildren().add(index, newPasswordVBox);
        parentNode.getChildren().add(index + 1, confirmNewPasswordVBox);

        loginButton.setText("Enregistrer");
        loginButton.setOnAction(event -> changePassword());
    }

    private void changePassword() {
        String newPassword = newPasswordField.getText();
        String confirmNewPassword = confirmNewPasswordField.getText();

        if (newPassword.isBlank()) {
            displayErrorAlert("Nouveau mot de passe est obligatoire");
            return;
        }

        if (newPassword.length() < 8) {
            displayErrorAlert("Nouveau mot de passe doit contenir au minimum 8 character");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            System.out.println("New password doesn't match confirm password");
            displayErrorAlert("Le mot de passe et la confirmation du mot de passe ne correspondent pas");
            return;
        }

        User authenticatedUser = AuthenticationService.getCurrentAuthenticatedUser();
        String encryptedNewPassword = Encryptor.encryptPassword(newPassword);

        if (encryptedNewPassword.equals(authenticatedUser.getPassword())) {
            System.out.println("Le nouveau mot de passe doit être différent de l'ancien mot de passe");
            displayErrorAlert("Le nouveau mot de passe doit être différent de l'ancien mot de passe");
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

        // Load FXML in a background thread
        new Thread(() -> {
            try {
                Parent load = fxmlLoader.load();

                // Switch to the JavaFX Application Thread to update the UI
                Platform.runLater(() -> {
                    scene.setRoot(load);
                    stage.setResizable(true);
                    stage.setMaximized(true);

                    // Now load the dashboard components
                    MainController mainController = fxmlLoader.getController();
                    try {
                        mainController.displayDashboard();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                // Handle the exception on the JavaFX Application Thread
                Platform.runLater(() -> {
                    // You can show an alert or log the error
                    showError("Failed to load the main interface: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showError(String message) {
        // Create and show an error alert dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
