package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController {
    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label currentBorrowsLabel;
    @FXML private Label overdueBorrowsLabel;
    @FXML private Label totalBorrowsLabel;
    @FXML private PieChart bookCategoryChart;
    @FXML private ListView<Book> popularBooksListView;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();

        // ข้อมูลสรุป
        int totalBooks = dataManager.getAllBooks().size();
        int availableBooks = (int) dataManager.getAllBooks().stream()
                .filter(Book::isAvailable)
                .count();
        int totalMembers = dataManager.getAllMembers().size();
        int currentBorrows = dataManager.getAllBorrowRecords().size();
        int totalBorrows = dataManager.getAllBorrowRecords().size();

        totalBooksLabel.setText(String.valueOf(totalBooks));
        availableBooksLabel.setText(availableBooks + " เล่มพร้อมให้ยืม");
        totalMembersLabel.setText(String.valueOf(totalMembers));
        currentBorrowsLabel.setText(String.valueOf(currentBorrows));
        overdueBorrowsLabel.setText("เกินกำหนด 0 รายการ");
        totalBorrowsLabel.setText(String.valueOf(totalBorrows));

        // กราฟวงกลมหมวดหมู่หนังสือ
        updateCategoryChart();

        // รายการหนังสือยอดนิยม
        updatePopularBooks();
    }

    private void updateCategoryChart() {
        Map<String, Long> categoryCounts = dataManager.getAllBooks().stream()
                .collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        categoryCounts.forEach((category, count) ->
                pieChartData.add(new PieChart.Data(category + " (" + count + ")", count)));

        bookCategoryChart.setData(pieChartData);
    }

    private void updatePopularBooks() {
        // ในระบบจริงอาจมีการนับจำนวนการยืม แต่ตอนนี้แสดงหนังสือทั้งหมด
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