package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UserHomepageController {
    @FXML private TextField searchField;
    @FXML private HBox featuredBooksBox;
    @FXML private TableView<Book> availableBooksTable;
    @FXML private TableColumn<Book, String> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Void> actionColumn;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();

        // ตั้งค่าคอลัมน์ตาราง
        setupTableColumns();

        // โหลดหนังสือแนะนำ
        loadFeaturedBooks();

        // โหลดหนังสือที่พร้อมให้ยืม
        loadAvailableBooks();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // คอลัมน์ปุ่มดำเนินการ
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailButton = new Button("รายละเอียด");

            {
                detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                detailButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    showBookDetail(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailButton);
                }
            }
        });
    }

    private void loadFeaturedBooks() {
        List<Book> books = dataManager.getAllBooks();

        // แสดงเพียง 5 เล่มแรกเป็นหนังสือแนะนำ
        books.stream().limit(5).forEach(book -> {
            VBox bookCard = createBookCard(book);
            featuredBooksBox.getChildren().add(bookCard);
        });
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5; -fx-pref-width: 150;");

        // สร้างพื้นที่สำหรับรูปปก (จำลองด้วยกล่องสี)
        VBox cover = new VBox();
        cover.setStyle("-fx-background-color: #BBDEFB; -fx-min-height: 160; -fx-min-width: 120;");

        Text title = new Text(book.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-wrapping-width: 130;");

        Text author = new Text(book.getAuthor());
        author.setStyle("-fx-font-size: 12; -fx-fill: #757575;");

        Button detailButton = new Button("รายละเอียด");
        detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        detailButton.setMaxWidth(Double.MAX_VALUE);
        detailButton.setOnAction(event -> showBookDetail(book));

        card.getChildren().addAll(cover, title, author, detailButton);
        return card;
    }

    private void loadAvailableBooks() {
        List<Book> availableBooks = dataManager.getAllBooks().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());

        availableBooksTable.setItems(FXCollections.observableArrayList(availableBooks));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            loadAvailableBooks();
            return;
        }

        List<Book> searchResults = dataManager.getAllBooks().stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(query) ||
                                book.getAuthor().toLowerCase().contains(query) ||
                                book.getCategory().toLowerCase().contains(query))
                .collect(Collectors.toList());

        availableBooksTable.setItems(FXCollections.observableArrayList(searchResults));
    }

    private void showBookDetail(Book book) {
        // ในที่นี้แค่แสดง alert สำหรับตัวอย่าง
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("รายละเอียดหนังสือ");
        alert.setHeaderText(book.getTitle());
        alert.setContentText(
                "รหัส: " + book.getId() + "\n" +
                        "ผู้แต่ง: " + book.getAuthor() + "\n" +
                        "หมวดหมู่: " + book.getCategory() + "\n" +
                        "สถานะ: " + (book.isAvailable() ? "พร้อมให้ยืม" : "ถูกยืมแล้ว")
        );
        alert.showAndWait();


    }
    @FXML
    private void openBorrowPage() {
        try {
            // ตรวจสอบว่ามี mainBorderPane หรือไม่
            BorderPane mainBorderPane = (BorderPane) searchField.getScene().getRoot();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/BorrowReturnView.fxml"));
            Parent borrowView = loader.load();

            // วางหน้ายืมหนังสือตรงส่วนกลางของ BorderPane
            mainBorderPane.setCenter(borrowView);
        } catch (IOException e) {
            e.printStackTrace();

            // แสดง Alert หากโหลดหน้าไม่สำเร็จ
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ข้อผิดพลาด");
            alert.setHeaderText("ไม่สามารถโหลดหน้ายืมหนังสือได้");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void handleLogout() {
        try {
            // โหลดหน้า Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/LoginView.fxml"));
            Parent loginView = loader.load();

            // ดึง Stage ปัจจุบัน
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(loginView, 600, 400));
            stage.setTitle("ระบบจัดการห้องสมุด - เข้าสู่ระบบ");

            // ล้างข้อมูลผู้ใช้ปัจจุบัน
            LibraryDataManager.getInstance().logout();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ข้อผิดพลาด");
            alert.setHeaderText("ไม่สามารถออกจากระบบได้");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}