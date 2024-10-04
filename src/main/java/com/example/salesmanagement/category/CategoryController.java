package com.example.salesmanagement.category;

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

public class CategoryController implements Initializable {
    private final Category category = new Category();
    @FXML
    public TextField nameTextField;
    @FXML
    public TextArea descriptionTextArea;
    @FXML
    public Button saveButton, cancelButton;
    private ListCategoriesController listCategoriesController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.setOnAction(e -> saveCategory());
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.close();
        });

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

    }

    public void saveCategory() {
        Category category = mapCategory();

        if (category == null) {
            return;
        }

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

        if (category == null) {
            return;
        }

        Optional<Category> optionalCategory = CategoryRepository.update(category);

        if (optionalCategory.isPresent()) {
            if (listCategoriesController != null) {
                int index = listCategoriesController.getCategoriesTableView().getItems().indexOf(category);

                if (index != -1) {
                    Category oldCategory = listCategoriesController.getCategoriesTableView().getItems().get(index);

                    listCategoriesController.getCategoriesTableView().getItems().remove(oldCategory);
                    listCategoriesController.getCategoriesTableView().getItems().add(optionalCategory.get());
                    listCategoriesController.categoriesTableView.refresh();
                }
            }

            this.category.setName(nameTextField.getText().trim());
            displaySuccessAlert();
        } else {
            displayErrorAlert();
        }
    }

    private Category mapCategory() {
        String name = nameTextField.getText().trim();

        if (name.isBlank()) {
            displayErrorAlert("Nom de la catégorie est obligatoire");
            return null;
        }

        Optional<Category> byName = CategoryRepository.findByName(name.toLowerCase());

        if (category.getId() == null) {
            if (byName.isPresent()) {
                displayErrorAlert("Une catégorie avec le même nom existe déjà");
                return null;
            }
        } else {
            if (!name.equalsIgnoreCase(category.getName()) && byName.isPresent()) {
                displayErrorAlert("Une catégorie avec le même nom existe déjà");
                return null;
            }
        }

        Category category = new Category();

        category.setId(this.category.getId());
        category.setName(name);
        category.setDescription(descriptionTextArea.getText() == null ? "" : descriptionTextArea.getText().trim());

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

    public void setListCategoriesController(ListCategoriesController listCategoriesController) {
        this.listCategoriesController = listCategoriesController;
    }

    public void initCategoryUpdate(Category category) {
        this.category.setId(category.getId());
        this.category.setName(category.getName());
        this.category.setDescription(category.getDescription() == null ? "" : category.getDescription());

        this.nameTextField.setText(this.category.getName());
        this.descriptionTextArea.setText(this.category.getDescription());

        saveButton.setOnAction(e -> updateCategory());
    }
}
