package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import com.library.models.BorrowRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label currentBorrowsLabel;
    @FXML private Label overdueBorrowsLabel;
    @FXML private TableView<BorrowRecord> recentActivitiesTable;
    @FXML private ListView<String> popularBooksListView;

    private LibraryDataManager dataManager;
    private ObservableList<BorrowRecord> activityList;
    private ObservableList<String> popularBooksList;

    @FXML
    private void initialize() {
        dataManager = LibraryDataManager.getInstance();
        activityList = FXCollections.observableArrayList();
        popularBooksList = FXCollections.observableArrayList();

        // ลงทะเบียน listeners เพื่อรับทราบการเปลี่ยนแปลงข้อมูล
        dataManager.addBookUpdateListener(this::refreshDashboard);
        dataManager.addMemberUpdateListener(this::refreshDashboard);

        // โหลดข้อมูลครั้งแรก
        refreshDashboard();
    }

    /**
     * รีเฟรชข้อมูลทั้งหมดบนแดชบอร์ด
     */
    private void refreshDashboard() {
        updateBookStats();
        updateMemberStats();
        updateBorrowStats();
        updateRecentActivities();
        updatePopularBooks();
    }

    /**
     * อัปเดตสถิติของหนังสือ
     */
    private void updateBookStats() {
        List<Book> allBooks = dataManager.getAllBooks();
        int totalBooks = allBooks.size();
        int availableBooks = 0;

        for (Book book : allBooks) {
            if (book.isAvailable()) {
                availableBooks++;
            }
        }

        totalBooksLabel.setText(String.valueOf(totalBooks));
        availableBooksLabel.setText(availableBooks + " available (" +
                (totalBooks > 0 ? (availableBooks * 100 / totalBooks) : 0) + "%)");
    }

    /**
     * อัปเดตสถิติของสมาชิก
     */
    private void updateMemberStats() {
        int totalMembers = dataManager.getAllMembers().size();
        totalMembersLabel.setText(String.valueOf(totalMembers));
    }

    /**
     * อัปเดตสถิติการยืม-คืน
     */
    private void updateBorrowStats() {
        List<BorrowRecord> activeRecords = dataManager.getActiveBorrowRecords();
        List<BorrowRecord> overdueRecords = dataManager.getOverdueBorrowRecords();

        currentBorrowsLabel.setText(String.valueOf(activeRecords.size()));
        overdueBorrowsLabel.setText(overdueRecords.size() + " overdue");
    }

    /**
     * อัปเดตกิจกรรมล่าสุด
     */
    private void updateRecentActivities() {
        activityList.clear();

        // ในตัวอย่างนี้ เราใช้การยืมล่าสุดเป็นกิจกรรม
        // คุณสามารถปรับเปลี่ยนให้เหมาะสมกับระบบของคุณ
        List<BorrowRecord> allRecords = dataManager.getAllBorrowRecords();

        // เรียงลำดับตามวันที่ล่าสุด
        allRecords.sort((r1, r2) -> r2.getBorrowDate().compareTo(r1.getBorrowDate()));

        // แสดงเฉพาะ 10 รายการล่าสุด
        int count = 0;
        for (BorrowRecord record : allRecords) {
            activityList.add(record);
            count++;
            if (count >= 10) break;
        }

        recentActivitiesTable.setItems(activityList);
    }

    /**
     * อัปเดตหนังสือยอดนิยม
     */
    private void updatePopularBooks() {
        popularBooksList.clear();

        // ตัวอย่างการนับจำนวนการยืมหนังสือแต่ละเล่ม
        Map<String, Integer> bookBorrowCounts = new HashMap<>();
        List<BorrowRecord> allRecords = dataManager.getAllBorrowRecords();

        for (BorrowRecord record : allRecords) {
            String bookId = record.getBook().getId();
            String bookTitle = record.getBook().getTitle();
            String key = bookId + " - " + bookTitle;

            bookBorrowCounts.put(key, bookBorrowCounts.getOrDefault(key, 0) + 1);
        }

        // แปลงเป็น List และเรียงลำดับตามจำนวนการยืม
        List<Map.Entry<String, Integer>> sortedBooks = new ArrayList<>(bookBorrowCounts.entrySet());
        sortedBooks.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // แสดงหนังสือยอดนิยม 5 อันดับ
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedBooks) {
            popularBooksList.add(entry.getKey() + " (" + entry.getValue() + " borrows)");
            count++;
            if (count >= 5) break;
        }

        popularBooksListView.setItems(popularBooksList);
    }
}