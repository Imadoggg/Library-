package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class AdminDashboardController {
    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label currentBorrowsLabel;
    @FXML private Label overdueBorrowsLabel;
    @FXML private Label totalBorrowsLabel;
    @FXML private ListView<Book> popularBooksListView;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();

        // Summary data
        int totalBooks = dataManager.getAllBooks().size();
        int availableBooks = (int) dataManager.getAllBooks().stream()
                .filter(Book::isAvailable)
                .count();
        int totalMembers = dataManager.getAllMembers().size();
        int currentBorrows = dataManager.getAllBorrowRecords().size();
        int totalBorrows = dataManager.getAllBorrowRecords().size();

        totalBooksLabel.setText(String.valueOf(totalBooks));
        availableBooksLabel.setText(availableBooks + " available");
        totalMembersLabel.setText(String.valueOf(totalMembers));
        currentBorrowsLabel.setText(String.valueOf(currentBorrows));
        overdueBorrowsLabel.setText("0 overdue items");
        if (totalBorrowsLabel != null) {
            totalBorrowsLabel.setText(String.valueOf(totalBorrows));
        }

        // Popular books list
        updatePopularBooks();
    }

    private void updatePopularBooks() {
        // In a real system, would count number of borrows, but for now show all books
        ObservableList<Book> books = FXCollections.observableArrayList(dataManager.getAllBooks());
        popularBooksListView.setItems(books);

        popularBooksListView.setCellFactory(param -> new ListCell<Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);

                if (empty || book == null) {
                    setText(null);
                } else {
                    setText(book.getTitle() + " - " + book.getAuthor());
                }
            }
        });
    }
}