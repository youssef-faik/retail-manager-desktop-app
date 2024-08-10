package com.example.gestioncommercial.category;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {
    @FXML
    public TextField nameTextField;
    @FXML
    public TextArea descriptionTextArea;
    @FXML
    public Button saveButton, cancelButton;

    private Long id;
    private ListCategoriesController listCategoriesController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.setOnAction(e -> saveCategory());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    public void saveCategory() {
        Category category = mapCategory();
        if (CategoryRepository.save(category)) {
            clearTextFields();

            if (listCategoriesController != null) {
                listCategoriesController.getCategoriesTableView().getItems().add(category);
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    public void updateCategory() {
        Category category = mapCategory();
        Optional<Category> optionalCategory = CategoryRepository.update(category);

        if (optionalCategory.isPresent()) {
            if (listCategoriesController != null) {
                int index = listCategoriesController.getCategoriesTableView().getItems().indexOf(category);

                if (index != -1) {
                    Category oldCategory = listCategoriesController.getCategoriesTableView().getItems().get(index);

                    listCategoriesController.getCategoriesTableView().getItems().remove(oldCategory);
                    listCategoriesController.getCategoriesTableView().getItems().add(optionalCategory.get());
                }
            }

            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Category mapCategory() {
        Category category = new Category();

        category.setId(id);
        category.setName(nameTextField.getText());
        category.setDescription(descriptionTextArea.getText());

        return category;
    }

    private void clearTextFields() {
        nameTextField.clear();
        descriptionTextArea.clear();
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

    public void setListCategoriesController(ListCategoriesController listCategoriesController) {
        this.listCategoriesController = listCategoriesController;
    }

    public void initCategoryUpdate(Category category) {
        this.id = category.getId();
        this.nameTextField.setText(category.getName());
        this.descriptionTextArea.setText(category.getDescription());

        saveButton.setOnAction(e -> updateCategory());
    }
}
