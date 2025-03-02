package com.library.controllers;

import com.library.LibraryDataManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminMainController {
    @FXML private Label usernameLabel;
    @FXML private StackPane contentArea;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();

        // ตั้งค่าชื่อผู้ใช้
        if (dataManager.getCurrentUser() != null) {
            usernameLabel.setText(dataManager.getCurrentUser().getName());
        }

        // แสดง dashboard เมื่อเริ่มต้น
        showDashboard();
    }

    @FXML
    private void handleLogout() {
        dataManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("ระบบจัดการห้องสมุด - เข้าสู่ระบบ");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        loadContent("/com/views/AdminDashboardView.fxml");
    }

    @FXML
    private void showBookManagement() {
        loadContent("/com/views/BookManagementView.fxml");
    }

    @FXML
    private void showMemberManagement() {
        loadContent("/com/views/MemberManagementView.fxml");
    }

    @FXML
    private void showBorrowReturn() {
        loadContent("/com/views/BorrowReturnView.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}