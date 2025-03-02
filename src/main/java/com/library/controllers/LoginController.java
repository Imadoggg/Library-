package com.library.controllers;

import com.library.LibraryApplication;
import com.library.LibraryDataManager;
import com.library.models.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("กรุณากรอกชื่อผู้ใช้และรหัสผ่าน");
            return;
        }

        if (dataManager.login(username, password)) {
            try {
                // โหลดหน้าจอที่เหมาะสมตามบทบาทของผู้ใช้
                String viewPath = dataManager.isCurrentUserAdmin()
                        ? "/com/views/AdminMainView.fxml"
                        : "/com/views/UserMainView.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
                Parent root = loader.load();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root, 800, 600));
                stage.setTitle("ระบบจัดการห้องสมุด - " +
                        (dataManager.isCurrentUserAdmin() ? "ผู้ดูแลระบบ" : "ผู้ใช้งาน"));
                stage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
                showError("เกิดข้อผิดพลาดในการโหลดหน้าจอ: " + e.getMessage());
            }
        } else {
            showError("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}