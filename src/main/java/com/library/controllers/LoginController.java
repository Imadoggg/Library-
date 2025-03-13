package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.utils.AppStyles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
    @FXML private Hyperlink registerLink; // Add this line

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

        // Clear error message
        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            errorLabel.setVisible(true);
            return;
        }

        if (dataManager.login(username, password)) {
            try {
                // Check if admin or user
                if (dataManager.isCurrentUserAdmin()) {
                    // Open Admin Dashboard
                    Parent root = FXMLLoader.load(getClass().getResource("/com/views/AdminMainView.fxml"));
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Admin Dashboard - Library Management System");
                    stage.show();
                } else {
                    // Open User Main
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

    @FXML
    private void handleRegisterLink() {
        try {
            // Load the registration form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/RegisterView.fxml"));
            Parent root = loader.load();

            // Create a new popup window (Stage)
            Stage popupStage = new Stage();
            popupStage.setTitle("Library Management System - Register");
            popupStage.setScene(new Scene(root, 500, 600));

            // Make it modal (user must close it before returning to login)
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Set the owner to the login window
            popupStage.initOwner(loginButton.getScene().getWindow());

            // Configure the controller
            RegisterController controller = loader.getController();
            controller.setStage(popupStage);

            // Show the popup
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading registration page");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}