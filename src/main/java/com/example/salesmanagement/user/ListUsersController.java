package com.example.salesmanagement.user;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListUsersController implements Initializable {
    @FXML
    private TableView<User> usersTableView;

    @FXML
    private Button deleteButton, updateButton, newButton;

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

        updateButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        newButton.setEffect(dropShadow);
        newButton.setTextFill(Color.color(1, 1, 1));
        newButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initUsersTableView();
        refreshUsersTable();
    }

    public void addUser() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-user.fxml"));
        Parent root = fxmlLoader.load();

        UserController userController = fxmlLoader.getController();
        userController.setListUsersController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        usersTableView.getSelectionModel().clearSelection();
    }

    public void updateUser() throws IOException {
        User selecteduser = usersTableView.getSelectionModel().getSelectedItem();
        if (selecteduser != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-user.fxml"));
            Parent root = fxmlLoader.load();

            UserController userController = fxmlLoader.getController();
            userController.initUserUpdate(selecteduser);
            userController.setListUsersController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            usersTableView.getSelectionModel().clearSelection();
        }
    }

    public void deleteUser() {
        User selectedItem = usersTableView.getSelectionModel().getSelectedItem();

        if (selectedItem.getRole() == Role.ADMIN) {
            displayErrorAlert("Impossible de supprimer l'utilisateur administrateur");
            return;
        }

        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cet utilisateur?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (UserRepository.deleteById(selectedItem.getId())) {
                    usersTableView.getSelectionModel().clearSelection();
                    usersTableView.getItems().remove(selectedItem);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert();
                }
            }

            updateButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }


    private void initUsersTableView() {
        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<User, String> lastnameColumn = new TableColumn<>("Nom");
        TableColumn<User, String> firstnameColumn = new TableColumn<>("Prénom");
        TableColumn<User, String> usernameColumn = new TableColumn<>("Identifiant");
        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        TableColumn<User, String> addressColumn = new TableColumn<>("Adresse");
        TableColumn<User, String> phoneNumberColumn = new TableColumn<>("Telephone");
        TableColumn<User, String> actionColumn = new TableColumn<>("Actions");

        actionColumn.setReorderable(false);

        usersTableView.getColumns().addAll(
                idColumn,
                firstnameColumn,
                lastnameColumn,
                usernameColumn,
                roleColumn,
                phoneNumberColumn,
                addressColumn,
                actionColumn
        );

        usersTableView.setOnMouseClicked(e -> {
            if (usersTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setVisible(false);
        firstnameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        roleColumn.setCellValueFactory(param ->
                new SimpleStringProperty(
                        switch (param.getValue().getRole()) {
                            case ADMIN -> "Admin";
                            case USER -> "Caissier";
                        }
                ));

        usersTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Callback<TableColumn<User, String>, TableCell<User, String>> cellFactory =
                (TableColumn<User, String> param) -> {
                    // make cell containing button

                    return new TableCell<>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            //that cell created only on non-empty rows
                            if (empty) {
                                setGraphic(null);
                                setText(null);

                            } else {
                                Button resetPasswordButton = new Button("Réinitialiser mot de passe");
                                Text icon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.REPEAT);
                                resetPasswordButton.setGraphic(icon);


                                resetPasswordButton.setOnMouseClicked((MouseEvent event) -> {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Confirmation de réinitialisation du mot de passe");
                                    alert.setHeaderText("Confirmation de réinitialisation le mot de passe");
                                    alert.setContentText("Voulez vous réinitialisez le mot de passe de cet utilisateur?");
                                    Optional<ButtonType> result = alert.showAndWait();

                                    if (result.isPresent() && result.get() == ButtonType.OK) {

                                        try {
                                            @SuppressWarnings("unchecked")
                                            TableRow<User> tableRow = (TableRow<User>) resetPasswordButton.getParent().getParent().getParent();
                                            User user = tableRow.getItem();

                                            // update user password
                                            boolean passwordWasUpdated = UserRepository.resetPassword(user.getId(), Encryptor.encryptPassword(user.getUsername()));

                                            if (passwordWasUpdated) {
                                                displaySuccessAlert();
                                                refreshUsersTable();
                                            }

                                        } catch (Exception ex) {
                                            Logger.getLogger(ListUsersController.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                });

                                HBox actionsHBox = new HBox(resetPasswordButton);
                                actionsHBox.setStyle("-fx-alignment:center");
                                setGraphic(actionsHBox);

                                setText(null);
                            }
                        }

                    };
                };

        actionColumn.setCellFactory(cellFactory);
    }

    public void refreshUsersTable() {
        usersTableView.setItems(UserRepository.findAll());
        usersTableView.refresh();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
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

    private void displayErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public TableView<User> getUsersTable() {
        return usersTableView;
    }

}
