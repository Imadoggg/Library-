package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.utils.AppStyles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private VBox loginFormContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    private LibraryDataManager dataManager;

    @FXML
    public void initialize() {
        System.out.println("กำลังเริ่มต้น LoginController...");
        dataManager = LibraryDataManager.getInstance();

        // ตั้งค่าสไตล์
        if (loginFormContainer != null) {
            loginFormContainer.setStyle(AppStyles.CARD_STYLE);
        }
        if (titleLabel != null) {
            titleLabel.setStyle(AppStyles.HEADER_TEXT);
        }
        if (subtitleLabel != null) {
            subtitleLabel.setStyle(AppStyles.SMALL_TEXT);
        }
        if (loginButton != null) {
            loginButton.setStyle(AppStyles.PRIMARY_BUTTON);
        }
        if (errorLabel != null) {
            errorLabel.setStyle("-fx-text-fill: " + AppStyles.DANGER_COLOR + ";");
        }

        // เพิ่มสไตล์เมื่อ hover โดยใช้ Event Handlers
        if (loginButton != null) {
            loginButton.setOnMouseEntered(e ->
                    loginButton.setStyle(AppStyles.PRIMARY_BUTTON + "-fx-opacity: 0.9;")
            );

            loginButton.setOnMouseExited(e ->
                    loginButton.setStyle(AppStyles.PRIMARY_BUTTON)
            );
        }
    }

    @FXML
    private void handleLogin() {
        System.out.println("เริ่มกระบวนการเข้าสู่ระบบ...");

        String username = "";
        String password = "";

        if (usernameField != null) {
            username = usernameField.getText().trim();
        } else {
            System.err.println("usernameField เป็น null");
        }

        if (passwordField != null) {
            password = passwordField.getText().trim();
        } else {
            System.err.println("passwordField เป็น null");
        }

        if (username.isEmpty() || password.isEmpty()) {
            showError("กรุณากรอกชื่อผู้ใช้และรหัสผ่าน");
            return;
        }

        if (dataManager.login(username, password)) {
            System.out.println("เข้าสู่ระบบสำเร็จ สำหรับ: " + username);

            try {
                // โหลดหน้าจอที่เหมาะสมตามบทบาทของผู้ใช้
                String viewPath = dataManager.isCurrentUserAdmin()
                        ? "/com/views/AdminMainView.fxml"
                        : "/com/views/UserMainView.fxml";

                System.out.println("กำลังพยายามโหลด: " + viewPath);
                Parent root = loadFXML(viewPath);

                if (root != null) {
                    Stage stage = (Stage) getStage();
                    stage.setScene(new Scene(root, 800, 600));
                    stage.setTitle("ระบบจัดการห้องสมุด - " +
                            (dataManager.isCurrentUserAdmin() ? "ผู้ดูแลระบบ" : "ผู้ใช้งาน"));
                    stage.centerOnScreen();
                } else {
                    // ถ้าไม่สามารถโหลด FXML ได้ ให้สร้าง UI อย่างง่ายแทน
                    createSimpleMainUI(dataManager.isCurrentUserAdmin());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("เกิดข้อผิดพลาดในการโหลดหน้าจอ: " + e.getMessage());
            }
        } else {
            showError("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        }
    }

    private Stage getStage() {
        if (usernameField != null && usernameField.getScene() != null) {
            return (Stage) usernameField.getScene().getWindow();
        } else if (loginFormContainer != null && loginFormContainer.getScene() != null) {
            return (Stage) loginFormContainer.getScene().getWindow();
        } else {
            System.err.println("ไม่สามารถค้นหา Stage ได้");
            return null;
        }
    }

    private Parent loadFXML(String fxmlPath) {
        System.out.println("กำลังโหลด FXML: " + fxmlPath);
        Parent root = null;
        Exception lastException = null;

        // วิธีที่ 1: ใช้ getResource
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            root = loader.load();
            System.out.println("โหลด FXML สำเร็จด้วยวิธีที่ 1");
            return root;
        } catch (Exception e1) {
            lastException = e1;
            System.err.println("ไม่สามารถโหลด FXML ด้วยวิธีที่ 1: " + e1);

            // วิธีที่ 2: ใช้ ClassLoader
            try {
                String relativePath = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                URL resource = getClass().getClassLoader().getResource(relativePath);

                if (resource != null) {
                    FXMLLoader loader = new FXMLLoader(resource);
                    root = loader.load();
                    System.out.println("โหลด FXML สำเร็จด้วยวิธีที่ 2");
                    return root;
                } else {
                    System.err.println("ไม่พบไฟล์ FXML สำหรับวิธีที่ 2: " + relativePath);
                }
            } catch (Exception e2) {
                lastException = e2;
                System.err.println("ไม่สามารถโหลด FXML ด้วยวิธีที่ 2: " + e2);
            }
        }

        // ถ้าไม่สามารถโหลดได้ทั้งสองวิธี
        if (lastException != null) {
            lastException.printStackTrace();
        }

        return null;
    }

    private void createSimpleMainUI(boolean isAdmin) {
        System.out.println("กำลังสร้าง UI อย่างง่ายสำหรับ " + (isAdmin ? "ผู้ดูแลระบบ" : "ผู้ใช้งาน"));

        BorderPane root = new BorderPane();

        // ส่วนหัว
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: " + (isAdmin ? "#3F51B5" : "#2196F3") + "; -fx-padding: 15;");

        Label titleLabel = new Label("ระบบจัดการห้องสมุด - " + (isAdmin ? "ผู้ดูแลระบบ" : "ผู้ใช้งาน"));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");

        header.getChildren().add(titleLabel);

        // ส่วนกลาง
        VBox center = new VBox(20);
        center.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label welcomeLabel = new Label("ยินดีต้อนรับเข้าสู่ระบบจัดการห้องสมุด");
        welcomeLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label infoLabel = new Label("ไม่สามารถโหลดส่วนประกอบหลักของแอพพลิเคชันได้");
        infoLabel.setStyle("-fx-text-fill: red;");

        Button logoutButton = new Button("ออกจากระบบ");
        logoutButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            dataManager.logout();
            try {
                Parent loginView = loadFXML("/com/views/LoginView.fxml");
                if (loginView != null) {
                    Stage stage = getStage();
                    stage.setScene(new Scene(loginView, 600, 400));
                    stage.setTitle("ระบบจัดการห้องสมุด - เข้าสู่ระบบ");
                } else {
                    showAlert("ไม่สามารถโหลดหน้าเข้าสู่ระบบได้");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("เกิดข้อผิดพลาดในการกลับไปยังหน้าเข้าสู่ระบบ: " + ex.getMessage());
            }
        });

        center.getChildren().addAll(welcomeLabel, infoLabel, logoutButton);

        // รวมส่วนต่างๆ
        root.setTop(header);
        root.setCenter(center);

        // แสดงหน้าจอ
        Stage stage = getStage();
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("ระบบจัดการห้องสมุด - " + (isAdmin ? "ผู้ดูแลระบบ" : "ผู้ใช้งาน"));
        stage.centerOnScreen();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            System.err.println("errorLabel เป็น null, แสดงข้อความผิดพลาด: " + message);
            showAlert(message);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ข้อผิดพลาด");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}