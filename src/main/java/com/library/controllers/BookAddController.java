package com.library.controllers;

import com.library.dao.BookDAO;
import com.library.models.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class BookAddController {

    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private CheckBox availableCheckBox;
    @FXML
    private Label errorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button addCategoryButton;
    private Stage stage;
    private BookManagementController parentController;


    private BookDAO bookDAO;
    private ObservableList<String> categories;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setBookManagementController(BookManagementController controller) {
        this.parentController = controller;
    }

    @FXML
    private void initialize() {
        bookDAO = new BookDAO();
        errorLabel.setVisible(false);

        // โหลดรายการหมวดหมู่จากฐานข้อมูล หรือใช้รายการตายตัวในตัวอย่างนี้
        categories = FXCollections.observableArrayList(
                "นิยาย", "นิยายวิทยาศาสตร์", "คอมพิวเตอร์", "การเงิน", "การจัดการ",
                "ประวัติศาสตร์", "จิตวิทยา", "ปรัชญา", "ศาสนา", "ศิลปะ", "เกษตรกรรม", "อื่นๆ"
        );

        categoryComboBox.setItems(categories);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setVisible(false);

        // 1. ตรวจสอบข้อมูล
        if (titleField.getText().isEmpty()) {
            showError("กรุณาระบุชื่อหนังสือ");
            return;
        }

        if (authorField.getText().isEmpty()) {
            showError("กรุณาระบุชื่อผู้แต่ง");
            return;
        }

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            showError("กรุณาเลือกหมวดหมู่");
            return;
        }

        Book newBook = new Book(
                null, // ส่ง null สำหรับ id เพราะจะถูกสร้างโดย auto-increment
                titleField.getText(),
                authorField.getText(),
                categoryComboBox.getValue()
        );

        newBook.setAvailable(availableCheckBox.isSelected());

        // 3. บันทึกลงฐานข้อมูล
        boolean success = bookDAO.addBook(newBook);

        if (success) {
            showAlert("เพิ่มหนังสือสำเร็จ", "เพิ่มหนังสือ \"" + newBook.getTitle() + "\" เข้าระบบเรียบร้อยแล้ว");
            handleCancel(event); // กลับไปหน้าจัดการหนังสือ
        } else {
            showError("เกิดข้อผิดพลาดในการบันทึกข้อมูล กรุณาลองอีกครั้ง");
        }
        if (success) {
            showAlert("เพิ่มหนังสือสำเร็จ", "เพิ่มหนังสือ \"" + newBook.getTitle() + "\" เข้าระบบเรียบร้อยแล้ว");
            // ปิด popup แทนการกลับไปหน้าจัดการหนังสือ
            if (stage != null) {
                stage.close();
            }
        } else {
            showError("เกิดข้อผิดพลาดในการบันทึกข้อมูล กรุณาลองอีกครั้ง");
        }
    }


    @FXML
    private void handleAddCategory(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("เพิ่มหมวดหมู่ใหม่");
        dialog.setHeaderText(null);
        dialog.setContentText("กรุณาระบุชื่อหมวดหมู่ใหม่:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty() && !categories.contains(name.trim())) {
                categories.add(name.trim());
                categoryComboBox.setValue(name.trim());
            }
        });
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // ปิด popup
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}