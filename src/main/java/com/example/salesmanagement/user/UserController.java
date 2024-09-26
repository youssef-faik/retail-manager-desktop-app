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
    private Long id;
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
        saveButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));

        saveButton.setOnAction(e -> saveUser());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    public void saveUser() {
        User user = mapUser();

        // encrypt password and save it
        user.setPassword(Encryptor.encryptPassword(user.getUsername().trim()));

        if (UserRepository.save(user)) {
            clearTextFields();
            if (listUsersController != null) {
                listUsersController.getUsersTable().getItems().add(user);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateClient() {
        User user = mapUser();
        Optional<User> optionalClient = UserRepository.update(user);

        if (optionalClient.isPresent()) {
            if (listUsersController != null) {
                int index = listUsersController.getUsersTable().getItems().indexOf(user);

                if (index != -1) {
                    User oldClient = listUsersController.getUsersTable().getItems().get(index);

                    listUsersController.getUsersTable().getItems().remove(oldClient);
                    listUsersController.getUsersTable().getItems().add(optionalClient.get());
                }
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private User mapUser() {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstnameTextField.getText());
        user.setLastName(lastnameTextField.getText());
        user.setUsername(usernameTextField.getText());
        user.setPhoneNumber(phoneNumberTextField.getText());
        user.setAddress(addressTextArea.getText());

        return user;
    }

    public void initUserUpdate(User User) {
        this.id = User.getId();
        this.firstnameTextField.setText(User.getFirstName());
        this.lastnameTextField.setText(User.getLastName());
        this.usernameTextField.setText(User.getUsername());
        this.phoneNumberTextField.setText(User.getPhoneNumber());
        this.addressTextArea.setText(User.getAddress());

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
