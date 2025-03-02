package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserBookSearchController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private CheckBox availableOnlyCheckBox;
    @FXML private ToggleButton gridViewButton;
    @FXML private ToggleButton tableViewButton;
    @FXML private ScrollPane gridViewPane;
    @FXML private FlowPane bookCardsPane;
    @FXML private TableView<Book> bookTableView;
    @FXML private TableColumn<Book, String> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Boolean> statusColumn;
    @FXML private TableColumn<Book, Void> actionColumn;
    @FXML private Label resultCountLabel;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();

        // ตั้งค่าการสลับมุมมอง
        setupViewToggle();

        // ตั้งค่าคอลัมน์ตาราง
        setupTableColumns();

        // ตั้งค่า dropdown หมวดหมู่
        setupCategoryComboBox();

        // โหลดหนังสือเริ่มต้น
        handleSearch();
    }

    private void setupViewToggle() {
        gridViewButton.setOnAction(e -> {
            gridViewPane.setVisible(true);
            bookTableView.setVisible(false);
            tableViewButton.setSelected(false);
        });

        tableViewButton.setOnAction(e -> {
            gridViewPane.setVisible(false);
            bookTableView.setVisible(true);
            gridViewButton.setSelected(false);
        });
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // คอลัมน์สถานะ
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        statusColumn.setCellFactory(column -> new TableCell<Book, Boolean>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty || available == null) {
                    setText(null);
                } else {
                    setText(available ? "พร้อมให้ยืม" : "ถูกยืมแล้ว");
                    setStyle(available ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
                }
            }
        });

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

    private void setupCategoryComboBox() {
        // รวบรวมหมวดหมู่ทั้งหมดที่มีในระบบ
        Set<String> categories = new HashSet<>();
        categories.add("ทั้งหมด");

        List<Book> allBooks = dataManager.getAllBooks();
        if (allBooks != null && !allBooks.isEmpty()) {
            for (Book book : allBooks) {
                if (book != null && book.getCategory() != null) {
                    categories.add(book.getCategory());
                }
            }

            System.out.println("พบหนังสือทั้งหมด " + allBooks.size() + " เล่ม");
        } else {
            System.out.println("ไม่พบข้อมูลหนังสือ");
        }

        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setValue("ทั้งหมด");
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String category = categoryComboBox.getValue();
        boolean availableOnly = availableOnlyCheckBox.isSelected();

        List<Book> searchResults = dataManager.getAllBooks().stream()
                .filter(book ->
                        (query.isEmpty() ||
                                book.getTitle().toLowerCase().contains(query) ||
                                book.getAuthor().toLowerCase().contains(query) ||
                                book.getId().toLowerCase().contains(query)) &&
                                (category.equals("ทั้งหมด") || book.getCategory().equals(category)) &&
                                (!availableOnly || book.isAvailable()))
                .collect(Collectors.toList());

        // อัพเดทการแสดงผลทั้งสองมุมมอง
        updateGridView(searchResults);
        updateTableView(searchResults);

        // อัพเดทข้อความแสดงจำนวนผลลัพธ์
        resultCountLabel.setText("พบ " + searchResults.size() + " รายการ");
    }

    private void updateGridView(List<Book> books) {
        bookCardsPane.getChildren().clear();

        for (Book book : books) {
            VBox bookCard = createBookCard(book);
            bookCardsPane.getChildren().add(bookCard);
        }
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setMinWidth(180);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        // พื้นที่สำหรับรูปปก (ใส่สีเทา)
        VBox cover = new VBox();
        cover.setPrefHeight(220);
        cover.setMinHeight(220);
        String coverColor = "#d9d9d9"; // สีเทาสำหรับทุกหนังสือ
        cover.setStyle("-fx-background-color: " + coverColor + "; -fx-background-radius: 5;");

        // เพิ่มตัวอักษรแรกของชื่อหนังสือ
        Label initialLabel = new Label(book.getTitle().substring(0, 1).toUpperCase());
        initialLabel.setStyle("-fx-font-size: 48; -fx-text-fill: white; -fx-font-weight: bold;");
        initialLabel.setAlignment(javafx.geometry.Pos.CENTER);
        initialLabel.setPrefWidth(Double.MAX_VALUE);
        initialLabel.setPrefHeight(Double.MAX_VALUE);
        cover.getChildren().add(initialLabel);
        cover.setAlignment(javafx.geometry.Pos.CENTER);

        Label status = new Label(book.isAvailable() ? "พร้อมให้ยืม" : "ถูกยืมแล้ว");
        status.setStyle("-fx-padding: 5 10; -fx-background-radius: 3; -fx-font-size: 11; " +
                "-fx-background-color: " + (book.isAvailable() ? "#4CAF50" : "#F44336") + ";" +
                "-fx-text-fill: white;");

        Label title = new Label(book.getTitle());
        title.setWrapText(true);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        title.setMinHeight(40);

        Label author = new Label(book.getAuthor());
        author.setWrapText(true);
        author.setStyle("-fx-font-size: 12; -fx-text-fill: #757575;");

        Label category = new Label(book.getCategory());
        category.setStyle("-fx-font-size: 12; -fx-text-fill: #757575; -fx-background-color: #F5F5F5; -fx-padding: 2 8; -fx-background-radius: 10;");

        Button detailButton = new Button("รายละเอียด");
        detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;");
        detailButton.setPrefWidth(Double.MAX_VALUE);
        detailButton.setOnAction(event -> showBookDetail(book));

        card.getChildren().addAll(cover, status, title, author, category, detailButton);
        return card;
    }

    private void updateTableView(List<Book> books) {
        bookTableView.setItems(FXCollections.observableArrayList(books));
    }

    private void showBookDetail(Book book) {
        // แสดง alert สำหรับตัวอย่าง
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
}