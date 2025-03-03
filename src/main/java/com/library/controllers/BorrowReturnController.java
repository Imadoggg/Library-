package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import com.library.models.Member;
import com.library.models.BorrowRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.time.format.DateTimeFormatter;


public class BorrowReturnController {
    @FXML private ComboBox<Member> memberComboBox;
    @FXML private ComboBox<Book> bookComboBox;
    @FXML private Button borrowButton; // added this variable
    @FXML private TableView<BorrowRecord> borrowedBooksTable;
    @FXML private TableColumn<BorrowRecord, String> borrowIdColumn;
    @FXML private TableColumn<BorrowRecord, String> memberNameColumn;
    @FXML private TableColumn<BorrowRecord, String> bookTitleColumn;
    @FXML private TableColumn<BorrowRecord, String> borrowDateColumn;
    @FXML private TableColumn<BorrowRecord, Void> actionColumn;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        try {
            System.out.println("BorrowReturnController.initialize() started");
            dataManager = LibraryDataManager.getInstance();

            // Setup ComboBoxes
            setupComboBoxes();

            // Setup TableView
            setupTableColumns();

            // Load borrowed items
            refreshBorrowedBooks();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error initializing borrow-return page: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        // Setup memberComboBox
        if (memberComboBox != null) {
            memberComboBox.setItems(FXCollections.observableArrayList(dataManager.getAllMembers()));
            memberComboBox.setConverter(new StringConverter<Member>() {
                @Override
                public String toString(Member member) {
                    return member == null ? "" : member.getName() + " (" + member.getId() + ")";
                }

                @Override
                public Member fromString(String string) {
                    return null;
                }
            });
        }

        // Setup bookComboBox
        if (bookComboBox != null) {
            // Show only available books
            bookComboBox.setItems(FXCollections.observableArrayList(
                    dataManager.getAllBooks().stream()
                            .filter(Book::isAvailable)
                            .toList()
            ));
            bookComboBox.setConverter(new StringConverter<Book>() {
                @Override
                public String toString(Book book) {
                    return book == null ? "" : book.getTitle() + " (" + book.getId() + ")";
                }

                @Override
                public Book fromString(String string) {
                    return null;
                }
            });
        }
    }

    private void setupTableColumns() {
        if (borrowedBooksTable != null) {
            // Setup columns
            borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            memberNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getMember().getName()));
            bookTitleColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
            borrowDateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFormattedBorrowDate()));

            // Setup return button in actionColumn
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button returnButton = new Button("Return");

                {
                    returnButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                    returnButton.setOnAction(event -> {
                        BorrowRecord record = getTableView().getItems().get(getIndex());
                        handleReturnBook(record);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        BorrowRecord record = getTableView().getItems().get(getIndex());
                        returnButton.setDisable(record.isReturned());
                        setGraphic(returnButton);
                    }
                }
            });
        }
    }

    private void refreshBorrowedBooks() {
        if (borrowedBooksTable != null) {
            // Show only non-returned items
            borrowedBooksTable.setItems(FXCollections.observableArrayList(
                    dataManager.getAllBorrowRecords().stream()
                            .filter(record -> !record.isReturned())
                            .toList()
            ));
        }
    }

    @FXML
    private void handleBorrowBook() {
        Member selectedMember = memberComboBox.getValue();
        Book selectedBook = bookComboBox.getValue();

        if (selectedMember == null || selectedBook == null) {
            showError("Please select both member and book");
            return;
        }

        BorrowRecord newRecord = dataManager.createBorrowRecord(
                selectedBook.getId(),
                selectedMember.getId()
        );

        if (newRecord != null) {
            showInfo("Book borrowed successfully");
            refreshBorrowedBooks();
            refreshBooks(); // Refresh available books list

            // Clear selection
            memberComboBox.setValue(null);
            bookComboBox.setValue(null);
        } else {
            showError("Could not borrow the book");
        }
    }

    private void handleReturnBook(BorrowRecord record) {
        if (dataManager.returnBook(record.getId())) {
            showInfo("Book returned successfully");
            refreshBorrowedBooks();
            refreshBooks(); // Refresh available books list
        } else {
            showError("Could not return the book");
        }
    }

    private void refreshBooks() {
        bookComboBox.setItems(FXCollections.observableArrayList(
                dataManager.getAllBooks().stream()
                        .filter(Book::isAvailable)
                        .toList()
        ));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void validateBorrowInputs() {
        // Check if both member and book are selected
        boolean isValid = memberComboBox.getValue() != null && bookComboBox.getValue() != null;

        // Enable/disable the borrow button based on validation
        if (borrowButton != null) { // Added check to prevent NullPointerException
            borrowButton.setDisable(!isValid);
        }
    }
}