package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.utils.AppStyles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

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
        dataManager = LibraryDataManager.getInstance();

        // Setup styles
        loginFormContainer.setStyle(AppStyles.CARD_STYLE);
        titleLabel.setStyle(AppStyles.HEADER_TEXT);
        subtitleLabel.setStyle(AppStyles.SMALL_TEXT);
        loginButton.setStyle(AppStyles.PRIMARY_BUTTON);
        errorLabel.setStyle("-fx-text-fill: " + AppStyles.DANGER_COLOR + ";");

        // Add hover styles using Event Handlers
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle(AppStyles.PRIMARY_BUTTON + "-fx-opacity: 0.9;")
        );

        loginButton.setOnMouseExited(e ->
                loginButton.setStyle(AppStyles.PRIMARY_BUTTON)
        );
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // ล้างข้อความผิดพลาด
        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            errorLabel.setVisible(true);
            return;
        }

        if (dataManager.login(username, password)) {
            try {
                // ตรวจสอบว่าเป็น admin หรือ user
                if (dataManager.isCurrentUserAdmin()) {
                    // เปิดหน้า Admin Dashboard
                    Parent root = FXMLLoader.load(getClass().getResource("/com/views/AdminMainView.fxml"));
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Admin Dashboard - Library Management System");
                    stage.show();
                } else {
                    // เปิดหน้า User Main
                    Parent root = FXMLLoader.load(getClass().getResource("/com/views/UserMainView.fxml"));
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("User Dashboard - Library Management System");
                    stage.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Error loading dashboard");
                errorLabel.setVisible(true);
            }
        } else {
            errorLabel.setText("Invalid username or password");
            errorLabel.setVisible(true);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}