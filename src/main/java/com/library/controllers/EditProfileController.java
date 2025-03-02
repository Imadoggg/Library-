package com.library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import com.library.models.User;
import com.library.models.UserRole;
import com.library.services.UserService;
import com.library.utils.DialogUtils;

public class EditProfileController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<UserRole> roleComboBox;

    private User user;
    private UserService userService;

    public void initialize() {
        // Populate role combo box
        roleComboBox.getItems().addAll(UserRole.values());
    }

    public void setUser(User user) {
        this.user = user;

        // Populate fields with current user data
        usernameField.setText(user.getUsername());
        nameField.setText(user.getName());
        roleComboBox.setValue(user.getRole());

        // Disable username field if needed
        usernameField.setEditable(false);
    }

    @FXML
    private void handleSave() {
        try {
            // Validate inputs
            if (nameField.getText().trim().isEmpty()) {
                DialogUtils.showErrorDialog("ข้อผิดพลาด", "กรุณากรอกชื่อ");
                return;
            }

            if (roleComboBox.getValue() == null) {
                DialogUtils.showErrorDialog("ข้อผิดพลาด", "กรุณาเลือกบทบาท");
                return;
            }

            // Update user details
            user.setName(nameField.getText().trim());
            user.setRole(roleComboBox.getValue());

            // Close dialog
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();

            // Optional: Show success message
            DialogUtils.showInfoDialog("สำเร็จ", "แก้ไขโปรไฟล์เรียบร้อย");
        } catch (Exception e) {
            DialogUtils.showErrorDialog("เกิดข้อผิดพลาด", "ไม่สามารถบันทึกโปรไฟล์ได้");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        // Close the dialog
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}