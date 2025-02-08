package com.library.controllers;

import com.LibraryDataManager;
import com.library.controllers.models.Book;
import com.library.controllers.models.Member;
import com.library.controllers.models.BorrowRecord;
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
    @FXML private TableView<BorrowRecord> borrowedBooksTable;
    @FXML private TableColumn<BorrowRecord, String> borrowIdColumn;
    @FXML private TableColumn<BorrowRecord, String> memberNameColumn;
    @FXML private TableColumn<BorrowRecord, String> bookTitleColumn;
    @FXML private TableColumn<BorrowRecord, String> borrowDateColumn;
    @FXML private TableColumn<BorrowRecord, Void> actionColumn;

    private LibraryDataManager dataManager;
    private ObservableList<BorrowRecord> borrowRecordList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();
        borrowRecordList = FXCollections.observableArrayList();

        setupComboBoxes();
        setupTableColumns();
        refreshBorrowedBooks();
    }

    private void setupComboBoxes() {
        // Setup member combo box
        memberComboBox.setItems(FXCollections.observableArrayList(dataManager.getAllMembers()));
        memberComboBox.setConverter(new StringConverter<Member>() {
            @Override
            public String toString(Member member) {
                return member == null ? "" : member.getName() + " (" + member.getId() + ")";
            }

            @Override
            public Member fromString(String string) {
                return null;
            }
        });

        // Setup book combo box
        ObservableList<Book> availableBooks = FXCollections.observableArrayList(
                dataManager.getAllBooks().stream()
                        .filter(Book::isAvailable)
                        .toList()
        );

        bookComboBox.setItems(availableBooks);
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

    private void setupTableColumns() {
        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        memberNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMember().getName()));

        bookTitleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBook().getTitle()));

        borrowDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getBorrowDate().format(dateFormatter)
                ));

        // Setup return book button in action column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button returnButton = new Button("คืนหนังสือ");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    BorrowRecord record = getTableView().getItems().get(getIndex());
                    returnButton.setDisable(record.isReturned());
                    returnButton.setOnAction(event -> handleReturnBook(record));
                    setGraphic(returnButton);
                }
            }
        });
    }

    @FXML
    private void handleBorrowBook() {
        Member selectedMember = memberComboBox.getValue();
        Book selectedBook = bookComboBox.getValue();

        if (selectedMember == null || selectedBook == null) {
            showAlert("กรุณาเลือกสมาชิกและหนังสือ", Alert.AlertType.WARNING);
            return;
        }

        BorrowRecord newRecord = dataManager.createBorrowRecord(
                selectedBook.getId(),
                selectedMember.getId()
        );

        if (newRecord != null) {
            showAlert("ยืมหนังสือสำเร็จ", Alert.AlertType.INFORMATION);
            refreshBorrowedBooks();
            setupComboBoxes(); // Refresh available books

            // Clear selections
            memberComboBox.setValue(null);
            bookComboBox.setValue(null);
        } else {
            showAlert("ไม่สามารถยืมหนังสือได้", Alert.AlertType.ERROR);
        }
    }

    private void handleReturnBook(BorrowRecord record) {
        if (dataManager.returnBook(record.getId())) {
            showAlert("คืนหนังสือสำเร็จ", Alert.AlertType.INFORMATION);
            refreshBorrowedBooks();
            setupComboBoxes(); // Refresh available books
        } else {
            showAlert("ไม่สามารถคืนหนังสือได้", Alert.AlertType.ERROR);
        }
    }

    private void refreshBorrowedBooks() {
        borrowRecordList.clear();
        // แสดงเฉพาะรายการที่ยังไม่ได้คืน
        borrowRecordList.addAll(
                dataManager.getAllBorrowRecords().stream()
                        .filter(record -> !record.isReturned())
                        .toList()
        );
        borrowedBooksTable.setItems(borrowRecordList);
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("แจ้งเตือน");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}