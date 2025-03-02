package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.BorrowRecord;
import com.library.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class UserMyBorrowsController {
    @FXML private TableView<BorrowRecord> borrowedBooksTable;
    @FXML private TableColumn<BorrowRecord, String> borrowIdColumn;
    @FXML private TableColumn<BorrowRecord, String> bookTitleColumn;
    @FXML private TableColumn<BorrowRecord, String> borrowDateColumn;
    @FXML private TableColumn<BorrowRecord, String> dueDateColumn;
    @FXML private TableColumn<BorrowRecord, String> statusColumn;

    private LibraryDataManager dataManager;
    private ObservableList<BorrowRecord> currentBorrows;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();
        currentBorrows = FXCollections.observableArrayList();

        setupTableColumns();
        loadCurrentBorrows();
    }

    private void setupTableColumns() {
        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        bookTitleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getTitle()));

        borrowDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBorrowDate().format(dateFormatter)));

        dueDateColumn.setCellValueFactory(cellData -> {
            // สมมติให้กำหนดคืน 7 วันหลังจากวันที่ยืม
            LocalDateTime dueDate = cellData.getValue().getBorrowDate().plusDays(7);
            return new SimpleStringProperty(dueDate.format(dateFormatter));
        });

        statusColumn.setCellValueFactory(cellData -> {
            boolean isReturned = cellData.getValue().isReturned();
            return new SimpleStringProperty(isReturned ? "คืนแล้ว" : "ยังไม่คืน");
        });
    }

    private void loadCurrentBorrows() {
        User currentUser = dataManager.getCurrentUser();
        if (currentUser != null) {
            currentBorrows.clear();
            currentBorrows.addAll(
                    dataManager.getAllBorrowRecords().stream()
                            .filter(record -> !record.isReturned())
                            .toList()
            );
            borrowedBooksTable.setItems(currentBorrows);
        }
    }
}