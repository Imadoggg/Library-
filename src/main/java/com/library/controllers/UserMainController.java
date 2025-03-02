package com.library.controllers;

import com.library.LibraryDataManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class UserMainController {
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

        // แสดงหน้าหลักเมื่อเริ่มต้น
        showHomepage();
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
    private void showHomepage() {
        loadContent("/com/views/UserHomepageView.fxml");
    }

    @FXML
    private void showBookSearch() {
        loadContent("/com/views/UserBookSearchView.fxml");
    }

    @FXML
    private void showMyBorrows() {
        loadContent("/com/views/UserMyBorrowsView.fxml");
    }

    @FXML
    private void showBorrowHistory() {
        loadContent("/com/views/UserBorrowHistoryView.fxml");
    }

    @FXML
    private void showProfile() {
        loadContent("/com/views/UserProfileView.fxml");
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