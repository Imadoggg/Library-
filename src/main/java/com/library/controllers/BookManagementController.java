package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
public class BookManagementController {

    @FXML private TableView<Book> bookTableView;
    @FXML private TableColumn<Book, String> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Boolean> statusColumn;

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField searchField;

    private LibraryDataManager dataManager;
    private ObservableList<Book> bookList;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();
        bookList = FXCollections.observableArrayList();

        // Setup columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Customize status column
        statusColumn.setCellFactory(column -> new TableCell<Book, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "พร้อมให้ยืม" : "ถูกยืมแล้ว");
                }
            }
        });

        categoryComboBox.getItems().addAll(
                "ทั้งหมด",
                "Programming",
                "Database",
                "Network",
                "AI",
                "Other"
        );
        categoryComboBox.setValue("ทั้งหมด");

        // Load initial data
        refreshBookList();
        categoryComboBox.setOnAction(e -> handleSearchBook());
    }

    @FXML
    private void handleSearchBook() {
        String category = categoryComboBox.getValue();
        String searchText = searchField.getText().trim();

        bookList.clear();
        if (category.equals("ทั้งหมด")) {
            if (searchText.isEmpty()) {
                bookList.addAll(dataManager.getAllBooks());
            } else {
                bookList.addAll(dataManager.findBooksByTitle(searchText));
            }
        } else {
            bookList.addAll(dataManager.findBooksByCategory(category));
        }

        bookTableView.setItems(bookList);
    }
    @FXML
    private void handleAddBook() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("เพิ่มหนังสือใหม่");
        dialog.setHeaderText("กรุณากรอกข้อมูลหนังสือ");
        dialog.setContentText("ชื่อหนังสือ:");

        dialog.showAndWait().ifPresent(title -> {
            if (!title.isEmpty()) {
                String id = "B" + String.format("%03d", dataManager.getAllBooks().size() + 1);
                Book newBook = new Book(id, title, "ผู้แต่งใหม่", categoryComboBox.getValue());
                dataManager.addBook(newBook);
                refreshBookList();
            }
        });
    }
    private void refreshBookList() {
        bookList.clear();
        bookList.addAll(dataManager.getAllBooks());
        bookTableView.setItems(bookList);
    }
}




