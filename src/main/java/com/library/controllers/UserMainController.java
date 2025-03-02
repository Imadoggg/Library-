package com.library.controllers;

import com.library.LibraryDataManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class UserMainController {
    @FXML
    private Label usernameLabel;
    @FXML
    private StackPane contentArea;

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
    private void showBorrowReturn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/BorrowReturnView.fxml"));
            Parent borrowReturnView = loader.load();

            // โหลดและแสดงหน้าการยืมคืนในพื้นที่เนื้อหา
            contentArea.getChildren().clear();
            contentArea.getChildren().add(borrowReturnView);
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ข้อผิดพลาด");
            alert.setHeaderText("ไม่สามารถโหลดหน้ายืมคืนหนังสือ");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void showBorrowHistory() {
        // เพิ่ม log เพื่อตรวจสอบ
        System.out.println("Attempting to load borrow history view");
        loadContent("/com/views/UserBorrowHistoryView.fxml");
    }

    @FXML
    private void showProfile() {
        // เพิ่ม log เพื่อตรวจสอบ
        System.out.println("Attempting to load profile view");
        loadContent("/com/views/UserProfileView.fxml");
    }


    private void loadContent(String fxmlPath) {
        try {
            System.out.println("Trying to load: " + fxmlPath);

            // ลองหาไฟล์ด้วยหลายวิธี
            URL resource = null;

            // วิธีที่ 1: ใช้ getResource ปกติ
            resource = getClass().getResource(fxmlPath);
            if (resource != null) {
                System.out.println("Found resource using getClass().getResource()");
            } else {
                // วิธีที่ 2: ลองใช้ ClassLoader
                String relativePath = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                resource = getClass().getClassLoader().getResource(relativePath);
                if (resource != null) {
                    System.out.println("Found resource using ClassLoader: " + relativePath);
                    FXMLLoader loader = new FXMLLoader(resource);
                    Parent view = loader.load();
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);
                    return;
                } else {
                    // วิธีที่ 3: ลองเส้นทางอื่น
                    String altPath = "/views" + fxmlPath.substring(fxmlPath.lastIndexOf('/'));
                    resource = getClass().getResource(altPath);
                    if (resource != null) {
                        System.out.println("Found resource using alternative path: " + altPath);
                        FXMLLoader loader = new FXMLLoader(resource);
                        Parent view = loader.load();
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(view);
                        return;
                    }
                }
            }

            if (resource == null) {
                throw new IOException("ไม่พบไฟล์ FXML: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ไม่สามารถโหลด " + fxmlPath + ": " + e.getMessage());

            // แสดงข้อความแจ้งเตือนบนหน้าจอ
            Label errorLabel = new Label("ไม่พบไฟล์ FXML: " + fxmlPath);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }
}
