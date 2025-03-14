package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.BorrowRecord;
import com.library.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class UserBorrowHistoryController {
    @FXML private ComboBox<String> periodComboBox;
    @FXML private TableView<BorrowRecord> historyTableView;
    @FXML private TableColumn<BorrowRecord, String> borrowIdColumn;
    @FXML private TableColumn<BorrowRecord, String> bookTitleColumn;
    @FXML private TableColumn<BorrowRecord, String> borrowDateColumn;
    @FXML private TableColumn<BorrowRecord, String> returnDateColumn;
    @FXML private TableColumn<BorrowRecord, String> statusColumn;

    private LibraryDataManager dataManager;
    private ObservableList<BorrowRecord> borrowHistory;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();
        borrowHistory = FXCollections.observableArrayList();

        // เพิ่มตัวเลือกใน ComboBox ในโค้ดแทนที่จะระบุใน FXML
        periodComboBox.getItems().addAll(
                "ทั้งหมด",
                "เดือนนี้",
                "3 เดือนที่ผ่านมา",
                "6 เดือนที่ผ่านมา",
                "ปีนี้"
        );

        periodComboBox.getSelectionModel().selectFirst();

        setupTableColumns();
        loadBorrowHistory();
    }

    private void setupTableColumns() {
        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        bookTitleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getTitle()));

        borrowDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBorrowDate().format(dateFormatter)));

        returnDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime returnDate = cellData.getValue().getReturnDate();
            return new SimpleStringProperty(returnDate != null ?
                    returnDate.format(dateFormatter) : "-");
        });

        statusColumn.setCellValueFactory(cellData -> {
            boolean isReturned = cellData.getValue().isReturned();
            return new SimpleStringProperty(isReturned ? "คืนแล้ว" : "ยังไม่คืน");
        });
    }

    @FXML
    private void handleSearch() {
        loadBorrowHistory();
    }

    private void loadBorrowHistory() {
        User currentUser = dataManager.getCurrentUser();
        if (currentUser != null) {
            String period = periodComboBox.getValue();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime effectiveStartDate = null;

            if ("เดือนนี้".equals(period)) {
                effectiveStartDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            } else if ("3 เดือนที่ผ่านมา".equals(period)) {
                effectiveStartDate = now.minusMonths(3);
            } else if ("6 เดือนที่ผ่านมา".equals(period)) {
                effectiveStartDate = now.minusMonths(6);
            } else if ("ปีนี้".equals(period)) {
                effectiveStartDate = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            }

            borrowHistory.clear();

            if (effectiveStartDate != null) {
                //lambda!!!!
                final LocalDateTime finalStartDate = effectiveStartDate;
                borrowHistory.addAll(
                        dataManager.getAllBorrowRecords().stream()
                                .filter(record -> record.getBorrowDate().isAfter(finalStartDate))
                                .toList()
                );
            } else {
                borrowHistory.addAll(dataManager.getAllBorrowRecords());
            }

            historyTableView.setItems(borrowHistory);
        }
    }
}