package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UserHomepageController {
    @FXML
    private TextField searchField;
    @FXML
    private HBox featuredBooksBox;
    @FXML
    private TableView<Book> availableBooksTable;
    @FXML
    private TableColumn<Book, String> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> categoryColumn;
    @FXML
    private TableColumn<Book, Void> actionColumn;
    @FXML
    private HBox carouselContainer;
    @FXML
    private HBox indicatorBox;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;


    private LibraryDataManager dataManager;
    private int currentPage = 0;
    private int booksPerPage = 1;
    private List<Book> featuredBooks;


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
        card.setPrefWidth(180);
        card.setMinWidth(180);
        card.setMaxWidth(180);
        card.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        // สร้างพื้นที่สำหรับรูปปก (ใส่สีเทา)
        VBox cover = new VBox();
        cover.setMinHeight(200);
        cover.setMaxHeight(200);
        cover.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 5;");

        Label title = new Label(book.getTitle());
        title.setWrapText(true);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        title.setMinHeight(40);
        title.setPrefHeight(40);

        Label author = new Label(book.getAuthor());
        author.setWrapText(true);
        author.setStyle("-fx-font-size: 12; -fx-text-fill: #757575;");

        Button detailButton = new Button("รายละเอียด");
        detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;");
        detailButton.setPrefWidth(Double.MAX_VALUE);
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

    private void setupCarousel() {
        featuredBooks = dataManager.getAllBooks().stream().limit(5).collect(Collectors.toList());

        // สร้างตัวบ่งชี้ (indicator dots)
        for (int i = 0; i < featuredBooks.size(); i++) {
            Label dot = new Label("•");
            final int index = i;
            dot.setStyle("-fx-font-size: 30; -fx-text-fill: " + (i == 0 ? "#2196F3" : "#E0E0E0") + ";");
            dot.setOnMouseClicked(e -> showPage(index));
            indicatorBox.getChildren().add(dot);
        }

        // ตั้งค่าปุ่มเลื่อน
        prevButton.setOnAction(e -> showPage(currentPage - 1));
        nextButton.setOnAction(e -> showPage(currentPage + 1));

        // แสดงหน้าแรก
        showPage(0);
    }

    private void showPage(int pageIndex) {
        // ตรวจสอบขอบเขต
        if (pageIndex < 0) pageIndex = featuredBooks.size() - 1;
        if (pageIndex >= featuredBooks.size()) pageIndex = 0;

        currentPage = pageIndex;

        // อัพเดต dots
        for (int i = 0; i < indicatorBox.getChildren().size(); i++) {
            Label dot = (Label) indicatorBox.getChildren().get(i);
            dot.setStyle("-fx-font-size: 30; -fx-text-fill: " + (i == currentPage ? "#2196F3" : "#E0E0E0") + ";");
        }

        // สร้างการ์ดหนังสือสำหรับหน้าปัจจุบัน
        carouselContainer.getChildren().clear();

        Book currentBook = featuredBooks.get(currentPage);
        carouselContainer.getChildren().add(createLargeBookCard(currentBook));
    }

    private Pane createLargeBookCard(Book book) {
        // สร้างการ์ดขนาดใหญ่
        HBox card = new HBox(20);
        card.setPrefWidth(700);
        card.setPrefHeight(300);
        card.setStyle("-fx-background-color: linear-gradient(to right, #1a237e, #303F9F); -fx-background-radius: 10; -fx-padding: 20;");

        // ปกหนังสือ
        StackPane coverPane = new StackPane();
        coverPane.setMinWidth(200);
        coverPane.setMaxWidth(200);
        coverPane.setPrefHeight(260);
        coverPane.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");

        Label initialLabel = new Label(book.getTitle().substring(0, 1).toUpperCase());
        initialLabel.setStyle("-fx-font-size: 80; -fx-text-fill: #303F9F; -fx-font-weight: bold;");
        coverPane.getChildren().add(initialLabel);

        // รายละเอียดหนังสือ
        VBox detailsBox = new VBox(15);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPrefHeight(260);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");

        Label authorLabel = new Label("โดย: " + book.getAuthor());
        authorLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #E0E0E0;");

        Label categoryLabel = new Label("หมวดหมู่: " + book.getCategory());
        categoryLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #E0E0E0;");

        // แสดงดาว
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < 5; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-font-size: 20; -fx-text-fill: " + (i < 4 ? "#FFC107" : "#757575") + ";");
            ratingBox.getChildren().add(star);
        }

        Label ratingText = new Label("4.0/5.0 (24 รีวิว)");
        ratingText.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14;");
        ratingBox.getChildren().add(ratingText);

        // คำอธิบายสั้นๆ
        Label descriptionLabel = new Label("หนังสือ " + book.getTitle() + " เป็นหนังสือที่ได้รับความนิยมในหมวด " +
                book.getCategory() + " เขียนโดย " + book.getAuthor() +
                " เหมาะสำหรับผู้ที่สนใจในด้านนี้");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14;");

        // ปุ่มดำเนินการ
        HBox actionBox = new HBox(10);

        Button detailButton = new Button("รายละเอียด");
        detailButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 10 20;");
        detailButton.setOnAction(e -> showBookDetail(book));

        Button borrowButton = new Button(book.isAvailable() ? "ยืมเลย" : "จองคิว");
        borrowButton.setDisable(!book.isAvailable());
        borrowButton.setStyle("-fx-background-color: " + (book.isAvailable() ? "#4CAF50" : "#757575") + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 10 20;");

        actionBox.getChildren().addAll(detailButton, borrowButton);

        detailsBox.getChildren().addAll(titleLabel, authorLabel, categoryLabel, ratingBox, descriptionLabel, actionBox);

        card.getChildren().addAll(coverPane, detailsBox);
        return card;
    }
}