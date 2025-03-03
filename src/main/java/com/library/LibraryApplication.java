package com.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LibraryApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("กำลังเริ่มแอพพลิเคชัน...");

            // พยายามโหลด LoginView โดยใช้หลายวิธี
            Parent root = null;
            Exception lastException = null;

            try {
                // วิธีที่ 1: ใช้ getResource ปกติ
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/LoginView.fxml"));
                root = loader.load();
                System.out.println("โหลด LoginView.fxml สำเร็จด้วยวิธีที่ 1");
            } catch (Exception e) {
                lastException = e;
                System.err.println("ไม่สามารถโหลด LoginView.fxml ด้วยวิธีที่ 1: " + e.getMessage());

                try {
                    // วิธีที่ 2: ใช้ ClassLoader
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("com/views/LoginView.fxml"));
                    root = loader.load();
                    System.out.println("โหลด LoginView.fxml สำเร็จด้วยวิธีที่ 2");
                } catch (Exception e2) {
                    lastException = e2;
                    System.err.println("ไม่สามารถโหลด LoginView.fxml ด้วยวิธีที่ 2: " + e2.getMessage());
                }
            }

            // ถ้าไม่สามารถโหลดได้ ให้สร้าง UI อย่างง่ายแทน
            if (root == null) {
                root = createSimpleLoginUI();
                System.out.println("สร้าง UI อย่างง่ายแทนเนื่องจากไม่สามารถโหลด FXML ได้");
            }

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("ระบบจัดการห้องสมุด");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการเริ่มแอพพลิเคชัน:");
            e.printStackTrace();

            // แสดง Alert เพื่อแจ้งข้อผิดพลาด
            showErrorAlert("เกิดข้อผิดพลาดในการเริ่มแอพพลิเคชัน", e.getMessage());
        }
    }

    private Parent createSimpleLoginUI() {
        VBox loginBox = new VBox(10);
        loginBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("ระบบจัดการห้องสมุด");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label errorLabel = new Label("ไม่สามารถโหลดหน้าเข้าสู่ระบบได้");
        errorLabel.setStyle("-fx-text-fill: red;");

        javafx.scene.control.TextField usernameField = new javafx.scene.control.TextField();
        usernameField.setPromptText("ชื่อผู้ใช้");
        usernameField.setMaxWidth(300);

        javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
        passwordField.setPromptText("รหัสผ่าน");
        passwordField.setMaxWidth(300);

        javafx.scene.control.Button loginButton = new javafx.scene.control.Button("เข้าสู่ระบบ");
        loginButton.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white;");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if ("admin".equals(username) && "admin".equals(password)) {
                showInfoAlert("เข้าสู่ระบบสำเร็จ", "ยินดีต้อนรับ ผู้ดูแลระบบ");
            } else if ("user".equals(username) && "user".equals(password)) {
                showInfoAlert("เข้าสู่ระบบสำเร็จ", "ยินดีต้อนรับ ผู้ใช้ทั่วไป");
            } else {
                errorLabel.setText("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
                errorLabel.setVisible(true);
            }
        });

        Label noteLabel = new Label("หมายเหตุ: ใช้ admin/admin หรือ user/user");
        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        loginBox.getChildren().addAll(
                titleLabel,
                errorLabel,
                new javafx.scene.control.Label("ชื่อผู้ใช้"),
                usernameField,
                new javafx.scene.control.Label("รหัสผ่าน"),
                passwordField,
                loginButton,
                noteLabel
        );

        errorLabel.setVisible(false);

        return loginBox;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}