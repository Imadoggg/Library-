package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
        System.out.println("BookManagementController.initialize() started");
        dataManager = LibraryDataManager.getInstance();
        bookList = FXCollections.observableArrayList();

        try {
            // Set up columns (if defined)
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

                // Custom cell factory for status column
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

            // Setup category ComboBox (if defined)
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

            // Setup search field Enter key handler
            if (searchField != null) {
                searchField.setOnKeyPressed(this::handleSearchKeyPress);
            }

            // Load initial data
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
    public void handleAddBook() {
        System.out.println("Adding new book...");
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add New Book");
            dialog.setHeaderText("Please enter book information");
            dialog.setContentText("Book Title:");

            dialog.showAndWait().ifPresent(title -> {
                if (!title.isEmpty()) {
                    String id = "B" + String.format("%03d", dataManager.getAllBooks().size() + 1);
                    String category = categoryComboBox != null ? categoryComboBox.getValue() : "Programming";
                    if (category.equals("All")) category = "Programming";

                    Book newBook = new Book(id, title, "New Author", category);
                    dataManager.addBook(newBook);
                    refreshBookList();
                }
            });

        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
            e.printStackTrace();

            // Show alert in case of error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not add book");
            alert.setContentText("Error: " + e.getMessage());
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