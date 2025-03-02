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
    @FXML private Button borrowButton; // เพิ่มการประกาศตัวแปรนี้
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
            dataManager = LibraryDataManager.getInstance();

            // ตั้งค่า ComboBox
            setupComboBoxes();

            // ตั้งค่า TableView
            setupTableColumns();

            // โหลดข้อมูลการยืม
            refreshBorrowedBooks();
        } catch (Exception e) {
            e.printStackTrace();
            showError("เกิดข้อผิดพลาดในการเริ่มต้นหน้ายืม-คืน: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        // ตั้งค่า memberComboBox
        if (memberComboBox != null) {
            memberComboBox.setItems(FXCollections.observableArrayList(dataManager.getAllMembers()));
            memberComboBox.setConverter(new javafx.util.StringConverter<Member>() {
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

        // ตั้งค่า bookComboBox
        if (bookComboBox != null) {
            // แสดงเฉพาะหนังสือที่พร้อมให้ยืม
            bookComboBox.setItems(FXCollections.observableArrayList(
                    dataManager.getAllBooks().stream()
                            .filter(Book::isAvailable)
                            .toList()
            ));
            bookComboBox.setConverter(new javafx.util.StringConverter<Book>() {
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
            // ตั้งค่าคอลัมน์
            borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            memberNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getMember().getName()));
            bookTitleColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
            borrowDateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFormattedBorrowDate()));

            // ตั้งค่าปุ่มคืนหนังสือในคอลัมน์ actionColumn
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button returnButton = new Button("คืนหนังสือ");

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
            // แสดงเฉพาะรายการที่ยังไม่คืน
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
            showError("กรุณาเลือกสมาชิกและหนังสือ");
            return;
        }

        BorrowRecord newRecord = dataManager.createBorrowRecord(
                selectedBook.getId(),
                selectedMember.getId()
        );

        if (newRecord != null) {
            showInfo("ยืมหนังสือสำเร็จ");
            refreshBorrowedBooks();
            refreshBooks(); // รีเฟรชรายการหนังสือที่พร้อมให้ยืม

            // ล้างค่า
            memberComboBox.setValue(null);
            bookComboBox.setValue(null);
        } else {
            showError("ไม่สามารถยืมหนังสือได้");
        }
    }

    private void handleReturnBook(BorrowRecord record) {
        if (dataManager.returnBook(record.getId())) {
            showInfo("คืนหนังสือสำเร็จ");
            refreshBorrowedBooks();
            refreshBooks(); // รีเฟรชรายการหนังสือที่พร้อมให้ยืม
        } else {
            showError("ไม่สามารถคืนหนังสือได้");
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
        alert.setTitle("ข้อผิดพลาด");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("แจ้งเตือน");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // เพิ่ม inner class ที่จำเป็น
    private static class StringConverter<T> extends javafx.util.StringConverter<T> {
        @Override
        public String toString(T object) {
            return object == null ? "" : object.toString();
        }

        @Override
        public T fromString(String string) {
            return null;
        }
    }

    @FXML
    public void validateBorrowInputs() {
        // ตรวจสอบว่าทั้งสมาชิกและหนังสือถูกเลือกแล้วหรือไม่
        boolean isValid = memberComboBox.getValue() != null && bookComboBox.getValue() != null;

        // ถ้าหากทั้งสมาชิกและหนังสือถูกเลือกแล้ว ให้เปิดใช้งานปุ่มยืม
        if (borrowButton != null) { // เพิ่มการตรวจสอบเพื่อป้องกัน NullPointerException
            borrowButton.setDisable(!isValid);
        }
    }
}