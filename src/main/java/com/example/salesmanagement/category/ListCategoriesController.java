package com.example.salesmanagement.category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListCategoriesController implements Initializable {
    private static final int ROWS_PER_PAGE = 13;
    @FXML
    public Button newButton, updateButton, deleteButton;
    @FXML
    public TableView<Category> categoriesTableView;
    FilteredList<Category> filteredList;
    SortedList<Category> sortedList;
    ObservableList<Category> observableList;
    @FXML
    private TextField searchTextField;
    @FXML
    private Pagination pagination;


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
        newButton.setBackground(new Background(new BackgroundFill(Color.color(0.4, 0.44, 1, 1.0), new CornerRadii(3.0), null)));
        ((Text) newButton.getGraphic()).setFill(Color.WHITE);

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        initCategoriesTableView();
        refreshCategoriesTable();

        int dataSize = (categoriesTableView.getItems().size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(dataSize);
        pagination.setPageFactory(this::createPage);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(category -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return category.getName().toLowerCase().contains(lowerCaseFilter)
                        || (category.getDescription() != null && category.getDescription().toLowerCase().contains(lowerCaseFilter));
            });

            // Update pagination after filtering
            int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
            pagination.setPageCount(pageCount);
            // Reset to first page after filter change
            pagination.setCurrentPageIndex(0);
            // Update the table view with the new first page
            createPage(0);
        });
    }

    public void addCategory(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-category.fxml"));
        Parent root = fxmlLoader.load();

        CategoryController categoryController = fxmlLoader.getController();
        categoryController.setListCategoriesController(this);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        categoriesTableView.getSelectionModel().clearSelection();
    }

    public void updateCategory(ActionEvent actionEvent) throws IOException {
        Category selectedCategory = categoriesTableView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-category.fxml"));
            Parent root = fxmlLoader.load();

            CategoryController categoryController = fxmlLoader.getController();
            categoryController.setListCategoriesController(this);
            categoryController.initCategoryUpdate(selectedCategory);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            categoriesTableView.getSelectionModel().clearSelection();
        }
    }

    public void deleteCategory(ActionEvent actionEvent) {
        Category selectedCategory = categoriesTableView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cette catégorie?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (CategoryRepository.deleteById(selectedCategory.getId())) {
                    categoriesTableView.getSelectionModel().clearSelection();
                    categoriesTableView.getItems().remove(selectedCategory);
                    displaySuccessAlert();
                } else {
                    displayErrorAlert("Cet enregistrement ne peut pas être supprimé, car il est référencé par d'autres enregistrements.");
                }

            }

            updateButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    private void initCategoriesTableView() {
        TableColumn<Category, String> nameColumn = new TableColumn<>("Nom");
        TableColumn<Category, String> descriptionColumn = new TableColumn<>("Description");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        categoriesTableView.getColumns().addAll(nameColumn, descriptionColumn);

        categoriesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        categoriesTableView.setOnMouseClicked(e -> {
            if (categoriesTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    public void refreshCategoriesTable() {
        categoriesTableView.setItems(CategoryRepository.findAll());

        observableList = categoriesTableView.getItems();

        filteredList = new FilteredList<>(observableList);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(categoriesTableView.comparatorProperty());
        categoriesTableView.setItems(sortedList);

        // Update pagination
        int pageCount = (filteredList.size() / ROWS_PER_PAGE) + 1;
        pagination.setPageCount(pageCount);

        categoriesTableView.refresh();
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

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredList.size());

        List<Category> subbedList = filteredList.subList(fromIndex, toIndex);
        categoriesTableView.setItems(FXCollections.observableArrayList(subbedList));
        return categoriesTableView;
    }

    public TableView<Category> getCategoriesTableView() {
        return categoriesTableView;
    }
}
