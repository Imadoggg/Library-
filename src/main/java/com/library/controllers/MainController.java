package com.library.controllers;

import com.library.LibraryDataManager;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.*;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab loginTab;
    @FXML private Tab bookTab;
    @FXML private Tab memberTab;
    @FXML private Tab borrowReturnTab;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private LibraryDataManager dataManager;
    private String currentRole = null; // "admin", "user", or null

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();

        // เริ่มต้นแสดงเฉพาะแท็บล็อกอิน
        showLoginTabOnly();
    }

    private void showLoginTabOnly() {
        mainTabPane.getTabs().forEach(tab -> {
            if (tab != loginTab) {
                tab.setDisable(true);
            }
        });
        mainTabPane.getSelectionModel().select(loginTab);
    }

    private void showTabs(String role) {
        // เปิดใช้งานแท็บตามบทบาท
        if ("admin".equals(role)) {
            // Admin เข้าถึงได้ทุกแท็บ
            mainTabPane.getTabs().forEach(tab -> tab.setDisable(false));
        } else if ("user".equals(role)) {
            // User เข้าถึงได้เฉพาะบางแท็บ (ตัวอย่าง - คุณสามารถปรับได้)
            loginTab.setDisable(false);
            bookTab.setDisable(false);
            borrowReturnTab.setDisable(false);
            memberTab.setDisable(true); // User ไม่สามารถจัดการสมาชิกได้
        }

        // เลือกแท็บแรกที่ไม่ใช่แท็บล็อกอิน
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab != loginTab && !tab.isDisable()) {
                mainTabPane.getSelectionModel().select(tab);
                break;
            }
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("กรุณากรอกชื่อผู้ใช้และรหัสผ่าน");
            return;
        }

        // ตรวจสอบการล็อกอินอย่างง่าย
        if ("admin".equals(username) && "admin".equals(password)) {
            currentRole = "admin";
            showTabs(currentRole);
        } else if ("user".equals(username) && "user".equals(password)) {
            currentRole = "user";
            showTabs(currentRole);
        } else {
            showError("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        }
    }

    @FXML
    private void handleExit() {
        // ถ้าล็อกอินแล้ว ให้ถามก่อนออก
        if (currentRole != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("ยืนยันการออกจากระบบ");
            alert.setHeaderText(null);
            alert.setContentText("คุณต้องการออกจากระบบหรือไม่?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    currentRole = null;
                    showLoginTabOnly();
                    clearLoginFields();
                }
            });
        } else {
            // ถ้ายังไม่ได้ล็อกอิน ให้ออกจากโปรแกรมเลย
            Platform.exit();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearLoginFields() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
    }
}