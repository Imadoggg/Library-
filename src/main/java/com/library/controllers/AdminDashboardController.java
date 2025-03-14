package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import com.library.models.BorrowRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminDashboardController {

    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label currentBorrowsLabel;
    @FXML private Label overdueBorrowsLabel;

    @FXML private TableView<BorrowRecord> recentActivitiesTable;
    @FXML private TableColumn<BorrowRecord, String> dateTimeColumn;
    @FXML private TableColumn<BorrowRecord, String> activityColumn;
    @FXML private TableColumn<BorrowRecord, String> detailsColumn;
    @FXML private TableColumn<BorrowRecord, String> byColumn;

    @FXML private ListView<String> popularBooksListView;

    private LibraryDataManager dataManager;
    private ObservableList<BorrowRecord> activityList;
    private ObservableList<String> popularBooksList;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        dataManager = LibraryDataManager.getInstance();
        activityList = FXCollections.observableArrayList();
        popularBooksList = FXCollections.observableArrayList();


        setupTableColumns();

        dataManager.addBookUpdateListener(this::refreshDashboard);
        dataManager.addMemberUpdateListener(this::refreshDashboard);

        dataManager.addBorrowUpdateListener(this::refreshDashboard);

        refreshDashboard();
    }

    private void setupTableColumns() {
        try {
            if (dateTimeColumn != null) {
                dateTimeColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getBorrowDate().format(formatter)));
            }

            if (activityColumn != null) {
                activityColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().isReturned() ? "Return" : "Borrow"));
            }

            if (detailsColumn != null) {
                detailsColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getBook().getTitle() +
                                " by " + cellData.getValue().getMember().getName()));
            }

            if (byColumn != null) {
                byColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty("Librarian"));
            }
        } catch (Exception e) {
            System.err.println("Error setting up table columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshDashboard() {
        System.out.println("AdminDashboardController.refreshDashboard() called at: " + java.time.LocalDateTime.now());
        try {
            updateBookStats();
            updateMemberStats();
            updateBorrowStats();
            updateRecentActivities();
            updatePopularBooks();
        } catch (Exception e) {
            System.err.println("Error in refreshDashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBookStats() {
        try {
            List<Book> allBooks = dataManager.getAllBooks();
            int totalBooks = allBooks.size();
            int availableBooks = 0;

            for (Book book : allBooks) {
                if (book.isAvailable()) {
                    availableBooks++;
                }
            }

            totalBooksLabel.setText(String.valueOf(totalBooks));
            availableBooksLabel.setText(availableBooks + " available");
        } catch (Exception e) {
            System.err.println("Error updating book stats: " + e.getMessage());
        }
    }

    private void updateMemberStats() {
        try {
            int totalMembers = dataManager.getAllMembers().size();
            totalMembersLabel.setText(String.valueOf(totalMembers));
        } catch (Exception e) {
            System.err.println("Error updating member stats: " + e.getMessage());
        }
    }

    private void updateBorrowStats() {
        try {
            List<BorrowRecord> activeRecords = dataManager.getActiveBorrowRecords();
            List<BorrowRecord> overdueRecords = dataManager.getOverdueBorrowRecords();

            System.out.println("Active borrow records: " + activeRecords.size());
            System.out.println("Overdue records: " + overdueRecords.size());

            javafx.application.Platform.runLater(() -> {
                currentBorrowsLabel.setText(String.valueOf(activeRecords.size()));
                overdueBorrowsLabel.setText(overdueRecords.size() + " overdue");
                System.out.println("UI updated with borrow stats");
            });
        } catch (Exception e) {
            System.err.println("Error updating borrow stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRecentActivities() {
        try {
            activityList.clear();

            List<BorrowRecord> allRecords = dataManager.getAllBorrowRecords();
            System.out.println("Dashboard - All borrow records: " +
                    (allRecords != null ? allRecords.size() : "null"));

            if (allRecords == null || allRecords.isEmpty()) {
                return;
            }

            allRecords.sort((r1, r2) -> {
                var date1 = r1.isReturned() && r1.getReturnDate() != null ?
                        r1.getReturnDate() : r1.getBorrowDate();
                var date2 = r2.isReturned() && r2.getReturnDate() != null ?
                        r2.getReturnDate() : r2.getBorrowDate();
                return date2.compareTo(date1);
            });

          int count = 0;
            for (BorrowRecord record : allRecords) {
                if (record != null) {
                    activityList.add(record);
                    count++;
                    if (count >= 10) break;
                }
            }

            recentActivitiesTable.setItems(activityList);
            System.out.println("Dashboard - Recent activities updated: " + activityList.size() + " items");
        } catch (Exception e) {
            System.err.println("Error updating activities: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void updatePopularBooks() {
        try {
            popularBooksList.clear();

            Map<String, Integer> bookCounts = new HashMap<>();
            Map<String, String> bookTitles = new HashMap<>();

            for (BorrowRecord record : dataManager.getAllBorrowRecords()) {
                if (record != null && record.getBook() != null) {
                    String bookId = record.getBook().getId();
                    bookCounts.put(bookId, bookCounts.getOrDefault(bookId, 0) + 1);
                    bookTitles.put(bookId, record.getBook().getTitle());
                }
            }

            List<Map.Entry<String, Integer>> sortedBooks = new ArrayList<>(bookCounts.entrySet());
            sortedBooks.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedBooks) {
                String bookTitle = bookTitles.get(entry.getKey());
                popularBooksList.add(bookTitle + " (" + entry.getValue() + " borrows)");
                count++;
                if (count >= 5) break;
            }

            popularBooksListView.setItems(popularBooksList);
        } catch (Exception e) {
            System.err.println("Error updating popular books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void refreshData() {
        refreshDashboard();
    }


}