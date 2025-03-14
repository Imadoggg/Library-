package com.library.controllers;

import com.library.LibraryDataManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AdminMainController {
    @FXML private Label usernameLabel;
    @FXML private StackPane contentArea;


    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        System.out.println("AdminMainController initializing...");
        dataManager = LibraryDataManager.getInstance();

        if (dataManager.getCurrentUser() != null) {
            usernameLabel.setText(dataManager.getCurrentUser().getName());
            System.out.println("Set username label to: " + dataManager.getCurrentUser().getName());
        } else {
            System.out.println("Current user is null");
        }

        showDashboard();
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
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
    public void showDashboard() {
        System.out.println("Showing dashboard...");
        loadContent("/com/views/AdminDashboardView.fxml");
    }

    @FXML
    public void showBookManagement() {
        System.out.println("Showing book management...");
        loadContent("com/views/BookManagementView.fxml");
    }

    @FXML
    public void showMemberManagement() {
        System.out.println("Showing member management...");
        loadContent("/com/views/MemberManagementView.fxml");
    }

    @FXML
    public void showBorrowReturn() {
        System.out.println("Showing borrow-return...");
        loadContent("/com/views/BorrowReturnView.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            System.out.println("Trying to load: " + fxmlPath);


            URL resource = null;

            resource = getClass().getResource(fxmlPath);
            if (resource != null) {
                System.out.println("Found resource using getClass().getResource()");
            } else {
                String relativePath = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                resource = getClass().getClassLoader().getResource(relativePath);
                if (resource != null) {
                    System.out.println("Found resource using ClassLoader: " + relativePath);
                } else {
                    String altPath = "/views" + fxmlPath.substring(fxmlPath.lastIndexOf('/'));
                    resource = getClass().getResource(altPath);
                    if (resource != null) {
                        System.out.println("Found resource using alternative path: " + altPath);
                    }
                }
            }

            if (resource == null) {
                throw new IOException("ไม่พบไฟล์ FXML: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            if (contentArea == null) {
                System.err.println("contentArea is null!");
                return;
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ไม่สามารถโหลด " + fxmlPath + ": " + e.getMessage());

            Label errorLabel = new Label("ไม่พบไฟล์ FXML: " + fxmlPath);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(errorLabel);
            } else {
                System.err.println("contentArea is null, cannot display error message");
            }
        }
    }
}