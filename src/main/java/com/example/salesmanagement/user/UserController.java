package com.example.salesmanagement.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    private final User user = new User();
    private ListUsersController listUsersController;

    @FXML
    private Button saveButton, cancelButton;
    @FXML
    private TextField usernameTextField, firstnameTextField, phoneNumberTextField, lastnameTextField;
    @FXML
    private TextArea addressTextArea;

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

        cancelButton.setEffect(dropShadow);
        saveButton.setEffect(dropShadow);
        saveButton.setTextFill(Color.color(1, 1, 1));
        saveButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));

        saveButton.setOnAction(e -> saveUser());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    public void saveUser() {
        User user = mapUser();

        if (user == null) {
            return;
        }

        // encrypt password and save it
        user.setPassword(Encryptor.encryptPassword(user.getUsername().trim()));

        if (UserRepository.save(user)) {
            clearTextFields();
            if (listUsersController != null) {
                listUsersController.getUsersObservableList().add(user);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateClient() {
        User user = mapUser();

        if (user == null) {
            return;
        }

        Optional<User> optionalUser = UserRepository.update(user);

        if (optionalUser.isPresent()) {
            if (listUsersController != null) {
                int index = listUsersController.getUsersObservableList().indexOf(user);

                if (index != -1) {
                    User oldUser = listUsersController.getUsersObservableList().get(index);

                    listUsersController.getUsersObservableList().remove(oldUser);
                    listUsersController.getUsersObservableList().add(index, optionalUser.get());
                    listUsersController.usersTableView.refresh();
                }
            }

            initUserUpdate(optionalUser.get());
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private User mapUser() {
        String username = usernameTextField.getText().trim();

        if (username.isBlank()) {
            displayErrorAlert("Identifiant est obligatoire");
            return null;
        }

        Optional<User> byUsername = UserRepository.findByUsername(username.toLowerCase());

        if (user.getId() == null) {
            if (byUsername.isPresent()) {
                displayErrorAlert("Un utilisateur avec le identifiant existe déjà");
                return null;
            }
        } else {
            if (!username.equalsIgnoreCase(user.getUsername()) && byUsername.isPresent()) {
                displayErrorAlert("Un utilisateur avec le identifiant existe déjà");
                return null;
            }
        }

        User user = new User();
        user.setId(this.user.getId());
        user.setUsername(username);
        user.setFirstName(firstnameTextField.getText().trim());
        user.setLastName(lastnameTextField.getText().trim());
        user.setPhoneNumber(phoneNumberTextField.getText().trim());
        user.setAddress(addressTextArea.getText().trim());

        return user;
    }

    public void initUserUpdate(User user) {
        this.user.setId(user.getId());
        this.user.setFirstName(user.getFirstName() == null ? "" : user.getFirstName());
        this.user.setLastName(user.getLastName() == null ? "" : user.getLastName());
        this.user.setUsername(user.getUsername());
        this.user.setRole(user.getRole());
        this.user.setPhoneNumber(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        this.user.setAddress(user.getAddress() == null ? "" : user.getAddress());

        this.firstnameTextField.setText(this.user.getFirstName());
        this.lastnameTextField.setText(this.user.getLastName());
        this.usernameTextField.setText(this.user.getUsername());
        this.phoneNumberTextField.setText(this.user.getPhoneNumber());
        this.addressTextArea.setText(this.user.getAddress());

        saveButton.setOnAction(e -> updateClient());
    }

    private void clearTextFields() {
        firstnameTextField.clear();
        lastnameTextField.clear();
        usernameTextField.clear();
        phoneNumberTextField.clear();
        addressTextArea.clear();
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

    public void setListUsersController(ListUsersController listUsersController) {
        this.listUsersController = listUsersController;
    }

}
