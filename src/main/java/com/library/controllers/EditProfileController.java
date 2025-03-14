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
        roleComboBox.getItems().addAll(UserRole.values());
    }

    public void setUser(User user) {
        this.user = user;

        usernameField.setText(user.getUsername());
        nameField.setText(user.getName());
        roleComboBox.setValue(user.getRole());

        usernameField.setEditable(false);
    }

    @FXML
    private void handleSave() {
        try {
            if (nameField.getText().trim().isEmpty()) {
                DialogUtils.showErrorDialog("ข้อผิดพลาด", "กรุณากรอกชื่อ");
                return;
            }

            if (roleComboBox.getValue() == null) {
                DialogUtils.showErrorDialog("ข้อผิดพลาด", "กรุณาเลือกบทบาท");
                return;
            }

            user.setName(nameField.getText().trim());
            user.setRole(roleComboBox.getValue());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();

            DialogUtils.showInfoDialog("สำเร็จ", "แก้ไขโปรไฟล์เรียบร้อย");
        } catch (Exception e) {
            DialogUtils.showErrorDialog("เกิดข้อผิดพลาด", "ไม่สามารถบันทึกโปรไฟล์ได้");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}