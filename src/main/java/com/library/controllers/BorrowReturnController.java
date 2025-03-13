package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import com.library.models.Member;
import com.library.models.BorrowRecord;
import com.library.models.User;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.time.format.DateTimeFormatter;


public class BorrowReturnController {
    // Modified: removed memberComboBox since users can only borrow for themselves
    @FXML private ComboBox<Book> bookComboBox;
    @FXML private Button borrowButton;
    @FXML private TableView<BorrowRecord> borrowedBooksTable;
    @FXML private TableColumn<BorrowRecord, String> borrowIdColumn;
    @FXML private TableColumn<BorrowRecord, String> memberNameColumn;
    @FXML private TableColumn<BorrowRecord, String> bookTitleColumn;
    @FXML private TableColumn<BorrowRecord, String> borrowDateColumn;
    @FXML private TableColumn<BorrowRecord, Void> actionColumn;

    // Added labels for user info and borrow list title
    @FXML private Label currentUserLabel;
    @FXML private Label borrowedBooksLabel;
    @FXML private Label adminNoteLabel;

    private LibraryDataManager dataManager;
    private Member currentMember; // To store the current user's associated member

    @FXML
    public void initialize() {
        try {
            System.out.println("BorrowReturnController.initialize() started");
            dataManager = LibraryDataManager.getInstance();

            // Get current user and show in UI
            User currentUser = dataManager.getCurrentUser();
            if (currentUser != null) {
                boolean isAdmin = dataManager.isCurrentUserAdmin();

                if (isAdmin) {
                    // Admin view setup
                    currentUserLabel.setText("Administrator View - Manage All Borrowed Books");
                    borrowedBooksLabel.setText("All Active Borrowed Books");
                    adminNoteLabel.setVisible(true);  // Show admin note
                    borrowButton.setDisable(true);    // Disable borrow button for admins
                    currentMember = null; // Admins don't borrow books themselves
                } else {
                    // Regular user view setup
                    currentUserLabel.setText("Borrowing as: " + currentUser.getName());
                    // Find or create a member for the current user
                    currentMember = findOrCreateMemberForUser(currentUser);
                }
            }

            // Setup book ComboBox
            setupBookComboBox();

            // Setup TableView
            setupTableColumns();

            // Load borrowed items
            refreshBorrowedBooks();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error initializing borrow-return page: " + e.getMessage());
        }
    }

    private Member findOrCreateMemberForUser(User user) {
        // Skip this process for admin users
        if (user.getRole().name().equals("ADMIN")) {
            return null;
        }

        // Try to find a member with the same name as the user
        for (Member member : dataManager.getAllMembers()) {
            if (member.getName().equals(user.getName())) {
                return member;
            }
        }

        // If no member is found, create one with the name and username as contact
        // Note: We store the username in 'contact' field as per the database schema
        Member newMember = new Member(null, user.getName(), user.getUsername());
        if (dataManager.addMember(newMember)) {
            return dataManager.getAllMembers().stream()
                    .filter(m -> m.getName().equals(user.getName()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    private void setupBookComboBox() {
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

            // Setup return button in actionColumn - only visible for admins
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
                        // Only show the return button for admins
                        boolean isAdmin = dataManager.isCurrentUserAdmin();
                        if (isAdmin && !record.isReturned()) {
                            setGraphic(returnButton);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }
    }

    private void refreshBorrowedBooks() {
        if (borrowedBooksTable != null) {
            boolean isAdmin = dataManager.isCurrentUserAdmin();

            if (isAdmin) {
                // Admin view: show all active borrows for all users
                borrowedBooksTable.setItems(FXCollections.observableArrayList(
                        dataManager.getAllBorrowRecords().stream()
                                .filter(record -> !record.isReturned())
                                .toList()
                ));
            } else if (currentMember != null) {
                // Regular user view: show only their borrows
                ObservableList<BorrowRecord> userBorrows = FXCollections.observableArrayList(
                        dataManager.getAllBorrowRecords().stream()
                                .filter(record -> !record.isReturned() &&
                                        record.getMember().getId().equals(currentMember.getId()))
                                .toList()
                );
                borrowedBooksTable.setItems(userBorrows);
            } else {
                // If somehow there's no current member for a non-admin user
                borrowedBooksTable.setItems(FXCollections.observableArrayList());
            }
        }
    }

    @FXML
    private void handleBorrowBook() {
        Book selectedBook = bookComboBox.getValue();
        boolean isAdmin = dataManager.isCurrentUserAdmin();

        if (isAdmin) {
            showError("Administrators cannot borrow books. Please use a regular user account.");
            return;
        }

        if (currentMember == null) {
            showError("User account is not properly linked to a member. Please contact an administrator.");
            return;
        }

        if (selectedBook == null) {
            showError("Please select a book to borrow");
            return;
        }

        BorrowRecord newRecord = dataManager.createBorrowRecord(
                selectedBook.getId(),
                currentMember.getId()
        );

        if (newRecord != null) {
            showInfo("Book borrowed successfully");
            refreshBorrowedBooks();
            refreshBooks(); // Refresh available books list

            // Clear selection
            bookComboBox.setValue(null);
        } else {
            showError("Could not borrow the book");
        }
    }

    private void handleReturnBook(BorrowRecord record) {
        // Only admins can return books
        if (!dataManager.isCurrentUserAdmin()) {
            showError("Only administrators can process book returns");
            return;
        }

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
        // Check if book is selected
        boolean isValid = bookComboBox.getValue() != null;

        // Enable/disable the borrow button based on validation
        if (borrowButton != null) {
            borrowButton.setDisable(!isValid);
        }
    }
}