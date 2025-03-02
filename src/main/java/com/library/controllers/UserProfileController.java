    package com.library.controllers;

    import com.library.LibraryDataManager;
    import com.library.models.User;
    import javafx.fxml.FXML;
    import javafx.scene.control.Label;
    import javafx.scene.control.PasswordField;
    import javafx.scene.control.TextField;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Alert.AlertType;

    public class UserProfileController {
        @FXML private Label usernameLabel;
        @FXML private TextField nameField;
        @FXML private PasswordField currentPasswordField;
        @FXML private PasswordField newPasswordField;
        @FXML private PasswordField confirmPasswordField;
        @FXML private Label errorLabel;

        private LibraryDataManager dataManager;
        private User currentUser;

        @FXML
        public void initialize() {
            dataManager = LibraryDataManager.getInstance();
            currentUser = dataManager.getCurrentUser();

            if (currentUser != null) {
                // แสดงข้อมูลผู้ใช้ปัจจุบัน
                usernameLabel.setText(currentUser.getUsername());
                nameField.setText(currentUser.getName());
            }
        }

        @FXML
        private void handleSaveProfile() {
            if (currentUser == null) {
                showError("ไม่พบข้อมูลผู้ใช้");
                return;
            }

            String name = nameField.getText().trim();
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // ตรวจสอบข้อมูลที่กรอก
            if (name.isEmpty()) {
                showError("กรุณากรอกชื่อ-นามสกุล");
                return;
            }

            // หากต้องการเปลี่ยนรหัสผ่าน
            if (!currentPassword.isEmpty()) {
                // ตรวจสอบรหัสผ่านปัจจุบัน
                if (!currentPassword.equals(currentUser.getPassword())) {
                    showError("รหัสผ่านปัจจุบันไม่ถูกต้อง");
                    return;
                }

                // ตรวจสอบรหัสผ่านใหม่
                if (newPassword.isEmpty()) {
                    showError("กรุณากรอกรหัสผ่านใหม่");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    showError("รหัสผ่านใหม่และการยืนยันไม่ตรงกัน");
                    return;
                }
            }

            // บันทึกการเปลี่ยนแปลง
            currentUser.setName(name);

            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                currentUser.setPassword(newPassword);
            }

            // เรียกใช้เมธอด updateUser ใน LibraryDataManager
            dataManager.updateUser(currentUser);

            // แสดงการแจ้งเตือนว่าบันทึกข้อมูลเรียบร้อย
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("บันทึกข้อมูล");
            alert.setHeaderText(null);
            alert.setContentText("บันทึกข้อมูลเรียบร้อยแล้ว");
            alert.showAndWait();

            // รีเซ็ตช่องกรอกรหัสผ่าน
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            errorLabel.setVisible(false);
        }

        private void showError(String message) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }