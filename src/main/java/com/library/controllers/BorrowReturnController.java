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
    @FXML
    private ComboBox<Book> bookComboBox;
    @FXML
    private Button borrowButton;
    @FXML
    private TableView<BorrowRecord> borrowedBooksTable;
    @FXML
    private TableColumn<BorrowRecord, String> borrowIdColumn;
    @FXML
    private TableColumn<BorrowRecord, String> memberNameColumn;
    @FXML
    private TableColumn<BorrowRecord, String> bookTitleColumn;
    @FXML
    private TableColumn<BorrowRecord, String> borrowDateColumn;
    @FXML
    private TableColumn<BorrowRecord, Void> actionColumn;

    @FXML
    private Label currentUserLabel;
    @FXML
    private Label borrowedBooksLabel;
    @FXML
    private Label adminNoteLabel;

    private LibraryDataManager dataManager;
    private Member currentMember;

    @FXML
    public void initialize() {
        try {
            System.out.println("BorrowReturnController.initialize() started");
            dataManager = LibraryDataManager.getInstance();

            User currentUser = dataManager.getCurrentUser();
            if (currentUser != null) {
                boolean isAdmin = dataManager.isCurrentUserAdmin();

                if (isAdmin) {
                    currentUserLabel.setText("Administrator View - Manage All Borrowed Books");
                    borrowedBooksLabel.setText("All Active Borrowed Books");
                    adminNoteLabel.setVisible(true);
                    borrowButton.setDisable(true);
                    currentMember = null;
                } else {
                    currentUserLabel.setText("Borrowing as: " + currentUser.getName());
                    currentMember = findOrCreateMemberForUser(currentUser);
                }
            }

            setupBookComboBox();

            setupTableColumns();

            refreshBorrowedBooks();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error initializing borrow-return page: " + e.getMessage());
        }
    }


    private Member findOrCreateMemberForUser(User user) {
        if (user.getRole().name().equals("ADMIN")) {
            return null;
        }

        for (Member member : dataManager.getAllMembers()) {
            if (member.getName().equals(user.getName())) {
                return member;
            }
        }


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
            borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            memberNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getMember().getName()));
            bookTitleColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
            borrowDateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFormattedBorrowDate()));

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
                borrowedBooksTable.setItems(FXCollections.observableArrayList(
                        dataManager.getAllBorrowRecords().stream()
                                .filter(record -> !record.isReturned())
                                .toList()
                ));
            } else if (currentMember != null) {
                ObservableList<BorrowRecord> userBorrows = FXCollections.observableArrayList(
                        dataManager.getAllBorrowRecords().stream()
                                .filter(record -> !record.isReturned() &&
                                        record.getMember().getId().equals(currentMember.getId()))
                                .toList()
                );
                borrowedBooksTable.setItems(userBorrows);
            } else {
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
            refreshBooks();

            bookComboBox.setValue(null);
        } else {
            showError("Could not borrow the book");
        }
    }

    private void handleReturnBook(BorrowRecord record) {
        if (!dataManager.isCurrentUserAdmin()) {
            showError("Only administrators can process book returns");
            return;
        }

        if (dataManager.returnBook(record.getId())) {
            showInfo("Book returned successfully");
            refreshBorrowedBooks();
            refreshBooks();
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
        boolean isValid = bookComboBox.getValue() != null;
        if (borrowButton != null) {
            borrowButton.setDisable(!isValid);
        }
    }
}