package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.User;
import com.library.services.UserService;
import com.library.utils.DialogUtils;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private User user;
    private UserService userService;

    public void setUser(User user) {
        this.user = user;
        this.userService = new UserService();
    }

    @FXML
    private void handleSave() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // ตรวจสอบความถูกต้อง
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            DialogUtils.showErrorDialog("ข้อผิดพลาด", "กรุณากรอกข้อมูลให้ครบทุกช่อง");
            return;
        }

        // ตรวจสอบรหัสผ่านปัจจุบัน
        if (!userService.validateCredentials(user.getUsername(), currentPassword)) {
            DialogUtils.showErrorDialog("ข้อผิดพลาด", "รหัสผ่านปัจจุบันไม่ถูกต้อง");
            return;
        }

        // ตรวจสอบรหัสผ่านใหม่
        if (!newPassword.equals(confirmPassword)) {
            DialogUtils.showErrorDialog("ข้อผิดพลาด", "รหัสผ่านใหม่ไม่ตรงกัน");
            return;
        }

        // ตรวจสอบความยาวรหัสผ่าน
        if (newPassword.length() < 6) {
            DialogUtils.showErrorDialog("ข้อผิดพลาด", "รหัสผ่านต้องมีความยาวอย่างน้อย 6 ตัวอักษร");
            return;
        }

        // เปลี่ยนรหัสผ่าน
        boolean changed = userService.changePassword(user.getUsername(), currentPassword, newPassword);

        if (changed) {
            DialogUtils.showInfoDialog("สำเร็จ", "เปลี่ยนรหัสผ่านเรียบร้อย");

            // ปิด Stage
            Stage stage = (Stage) currentPasswordField.getScene().getWindow();
            stage.close();
        } else {
            DialogUtils.showErrorDialog("ข้อผิดพลาด", "ไม่สามารถเปลี่ยนรหัสผ่านได้");
        }
    }

    @FXML
    private void handleCancel() {
        // ปิด Stage
        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
        stage.close();
    }
}