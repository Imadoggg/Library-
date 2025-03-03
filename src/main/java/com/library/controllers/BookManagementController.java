package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
        System.out.println("BookManagementController.initialize() เริ่มทำงาน");
        dataManager = LibraryDataManager.getInstance();
        bookList = FXCollections.observableArrayList();

        try {
            // ตั้งค่าคอลัมน์ต่างๆ (ถ้ามีการกำหนดไว้)
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
                            setText(item ? "พร้อมให้ยืม" : "ถูกยืมแล้ว");
                        }
                    }
                });
            }

            // ตั้งค่า ComboBox ตัวเลือกหมวดหมู่ (ถ้ามีการกำหนดไว้)
            if (categoryComboBox != null) {
                System.out.println("กำลังตั้งค่า categoryComboBox");
                categoryComboBox.getItems().addAll(
                        "ทั้งหมด",
                        "Programming",
                        "Database",
                        "Network",
                        "AI",
                        "Other"
                );
                categoryComboBox.setValue("ทั้งหมด");
                categoryComboBox.setOnAction(e -> handleSearchBook());
            } else {
                System.out.println("ไม่พบ categoryComboBox ในไฟล์ FXML");
            }

            // โหลดข้อมูลเริ่มต้น
            refreshBookList();

        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดใน initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearchBook() {
        System.out.println("กำลังค้นหาหนังสือ...");
        try {
            String category = categoryComboBox != null ? categoryComboBox.getValue() : "ทั้งหมด";
            String searchText = searchField != null ? searchField.getText().trim() : "";

            System.out.println("หมวดหมู่: " + category + ", คำค้นหา: " + searchText);

            bookList.clear();
            if (category.equals("ทั้งหมด")) {
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
            System.err.println("เกิดข้อผิดพลาดในการค้นหา: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddBook() {
        System.out.println("กำลังเพิ่มหนังสือใหม่...");
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("เพิ่มหนังสือใหม่");
            dialog.setHeaderText("กรุณากรอกข้อมูลหนังสือ");
            dialog.setContentText("ชื่อหนังสือ:");

            dialog.showAndWait().ifPresent(title -> {
                if (!title.isEmpty()) {
                    String id = "B" + String.format("%03d", dataManager.getAllBooks().size() + 1);
                    String category = categoryComboBox != null ? categoryComboBox.getValue() : "Programming";
                    if (category.equals("ทั้งหมด")) category = "Programming";

                    Book newBook = new Book(id, title, "ผู้แต่งใหม่", category);
                    dataManager.addBook(newBook);
                    refreshBookList();
                }
            });

        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการเพิ่มหนังสือ: " + e.getMessage());
            e.printStackTrace();

            // แสดง Alert กรณีเกิดข้อผิดพลาด
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ข้อผิดพลาด");
            alert.setHeaderText("ไม่สามารถเพิ่มหนังสือได้");
            alert.setContentText("เกิดข้อผิดพลาด: " + e.getMessage());
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
            System.err.println("เกิดข้อผิดพลาดในการโหลดรายการหนังสือ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}