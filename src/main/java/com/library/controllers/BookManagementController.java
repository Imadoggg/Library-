package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent; // ถูกต้อง - เป็น JavaFX
import java.io.IOException;

public class BookManagementController {

    @FXML private TableView<Book> bookTableView;
    @FXML private TableColumn<Book, String> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Boolean> statusColumn;
    // เพิ่มบรรทัดนี้ที่ส่วนบนของคลาสพร้อมกับตัวแปรอื่นๆ
    @FXML
    private Button addBookButton;

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField searchField;

    private LibraryDataManager dataManager;
    private ObservableList<Book> bookList;

    @FXML
    public void initialize() {
        System.out.println("BookManagementController.initialize() started");
        dataManager = LibraryDataManager.getInstance();
        bookList = FXCollections.observableArrayList();

        try {
            if (idColumn != null) {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            }

            if (titleColumn != null) {
                titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            }

            if (authorColumn != null) {
                authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
            }

            if (categoryColumn != null) {
                categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            }

            if (statusColumn != null) {
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("available"));

                statusColumn.setCellFactory(column -> new TableCell<Book, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item ? "Available" : "Borrowed");
                        }
                    }
                });
            }

            if (categoryComboBox != null) {
                System.out.println("Setting up categoryComboBox");
                categoryComboBox.getItems().addAll(
                        "All",
                        "Programming",
                        "Database",
                        "Network",
                        "AI",
                        "Other"
                );
                categoryComboBox.setValue("All");
                categoryComboBox.setOnAction(e -> handleSearchBook());
            } else {
                System.out.println("categoryComboBox not found in FXML");
            }

            if (searchField != null) {
                searchField.setOnKeyPressed(this::handleSearchKeyPress);
            }

            refreshBookList();

        } catch (Exception e) {
            System.err.println("Error in initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSearchKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSearchBook();
        }
    }

    @FXML
    public void handleSearchBook() {
        System.out.println("Searching for books...");
        try {
            String category = categoryComboBox != null ? categoryComboBox.getValue() : "All";
            String searchText = searchField != null ? searchField.getText().trim() : "";

            System.out.println("Category: " + category + ", Search text: " + searchText);

            bookList.clear();
            if (category.equals("All")) {
                if (searchText.isEmpty()) {
                    bookList.addAll(dataManager.getAllBooks());
                } else {
                    bookList.addAll(dataManager.findBooksByTitle(searchText));
                }
            } else {
                bookList.addAll(dataManager.findBooksByCategory(category));
            }

            if (bookTableView != null) {
                bookTableView.setItems(bookList);
            }

        } catch (Exception e) {
            System.err.println("Error searching: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddBook(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/BookAddView.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("เพิ่มหนังสือใหม่");
            popupStage.setScene(new Scene(root));

            popupStage.initModality(Modality.APPLICATION_MODAL);

            Stage mainStage = (Stage) bookTableView.getScene().getWindow();

            popupStage.initOwner(mainStage);

            BookAddController controller = loader.getController();
            controller.setStage(popupStage);
            controller.setBookManagementController(this);

            popupStage.showAndWait();

            refreshBookList();

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("เกิดข้อผิดพลาดในการโหลดหน้าเพิ่มหนังสือ: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void refreshBookList() {
        try {
            if (bookList != null) {
                bookList.clear();
                bookList.addAll(dataManager.getAllBooks());

                if (bookTableView != null) {
                    bookTableView.setItems(bookList);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading book list: " + e.getMessage());
            e.printStackTrace();
        }
    }
}