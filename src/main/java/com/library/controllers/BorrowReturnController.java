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

        // เพิ่ม Listener สำหรับการอัพเดทข้อมูล
        dataManager.addMemberUpdateListener(this::refreshMembers);
        dataManager.addBookUpdateListener(this::refreshBooks);
    }

    private void setupComboBoxes() {
        refreshMembers();
        refreshBooks();

        // Setup member combo box converter
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

        // Setup book combo box converter
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
        // Setup basic columns with PropertyValueFactory
        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        memberNameColumn.setCellValueFactory(cellData -> cellData.getValue().getMember().nameProperty());
        bookTitleColumn.setCellValueFactory(cellData -> cellData.getValue().getBook().titleProperty());
        borrowDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedBorrowDate()));

        // Setup return button in action column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button returnButton = new Button("Return");

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


    public void refreshMembers() {
        Member selectedMember = memberComboBox.getValue();
        memberComboBox.setItems(FXCollections.observableArrayList(dataManager.getAllMembers()));
        if (selectedMember != null) {
            memberComboBox.setValue(selectedMember);
        }
    }

    public void refreshBooks() {
        Book selectedBook = bookComboBox.getValue();
        bookComboBox.setItems(FXCollections.observableArrayList(
                dataManager.getAllBooks().stream()
                        .filter(Book::isAvailable)
                        .toList()
        ));
        if (selectedBook != null && selectedBook.isAvailable()) {
            bookComboBox.setValue(selectedBook);
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

    @FXML
    private void handleBorrowBook() {
        Member selectedMember = memberComboBox.getValue();
        Book selectedBook = bookComboBox.getValue();

        if (selectedMember == null || selectedBook == null) {
            showAlert("Please select member and book", Alert.AlertType.WARNING);
            return;
        }

        BorrowRecord newRecord = dataManager.createBorrowRecord(
                selectedBook.getId(),
                selectedMember.getId()
        );

        if (newRecord != null) {
            showAlert("Book borrowed successfully", Alert.AlertType.INFORMATION);
            refreshBorrowedBooks();
            refreshBooks(); // refresh available books

            // Clear selections
            memberComboBox.setValue(null);
            bookComboBox.setValue(null);
        } else {
            showAlert("Failed to borrow book", Alert.AlertType.ERROR);
        }
    }

    private void handleReturnBook(BorrowRecord record) {
        if (dataManager.returnBook(record.getId())) {
            showAlert("Book returned successfully", Alert.AlertType.INFORMATION);
            refreshBorrowedBooks();
            refreshBooks(); // refresh available books
        } else {
            showAlert("Failed to return book", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Library System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}