package com.library.controllers;

import com.library.dao.UserDAO;
import com.library.models.User;
import com.library.models.UserRole;
import com.library.utils.AppStyles;
import com.library.utils.DatabaseConnection;
import com.library.utils.DialogUtils;

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
// No need for MessageDigest and NoSuchAlgorithmException since we use SQL's SHA2
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    // Phone field removed as it's not in the current database structure
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private Button cancelButton;
    @FXML private VBox registerFormContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    private UserDAO userDAO;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    @FXML
    public void initialize() {
        userDAO = new UserDAO();

        // Setup styles
        registerFormContainer.setStyle(AppStyles.CARD_STYLE);
        titleLabel.setStyle(AppStyles.HEADER_TEXT);
        subtitleLabel.setStyle(AppStyles.SMALL_TEXT);
        registerButton.setStyle(AppStyles.PRIMARY_BUTTON);
        cancelButton.setStyle(AppStyles.DANGER_BUTTON);
        errorLabel.setStyle("-fx-text-fill: " + AppStyles.DANGER_COLOR + ";");

        // Add hover styles
        registerButton.setOnMouseEntered(e ->
                registerButton.setStyle(AppStyles.PRIMARY_BUTTON + "-fx-opacity: 0.9;")
        );

        registerButton.setOnMouseExited(e ->
                registerButton.setStyle(AppStyles.PRIMARY_BUTTON)
        );

        cancelButton.setOnMouseEntered(e ->
                cancelButton.setStyle(AppStyles.DANGER_BUTTON + "-fx-opacity: 0.9;")
        );

        cancelButton.setOnMouseExited(e ->
                cancelButton.setStyle(AppStyles.DANGER_BUTTON)
        );
    }

    @FXML
    private void handleRegister() {
        // Clear previous error
        errorLabel.setVisible(false);

        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        // Get phone information (using username as contact information by default)

        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                fullName.isEmpty() || email.isEmpty()) {
            showError("Please fill in all required fields");
            return;
        }

        // Validate username (only letters, numbers, and underscores, at least 3 characters)
        if (!username.matches("^[a-zA-Z0-9_]{3,}$")) {
            showError("Username must be at least 3 characters and contain only letters, numbers, and underscores");
            return;
        }

        // Check if username already exists
        if (userExists(username)) {
            showError("Username already exists. Please choose another one");
            return;
        }

        // Validate password (at least 6 characters)
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }

        // Confirm password
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        // If all validations pass, register the user
        boolean success = registerUser(username, password, fullName, email, null);

        if (success) {
            DialogUtils.showInfoDialog("Registration Successful",
                    "Your account has been created successfully. You can now login.");

            // Close the registration popup
            if (stage != null) {
                stage.close();
            }
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    private boolean userExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean registerUser(String username, String password, String fullName, String email, String phone) {
        String query = "INSERT INTO users (username, password, name, email, role) VALUES (?, SHA2(?, 256), ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setString(5, UserRole.USER.name());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void handleLoginLink() {
        // Just close the popup window
        if (stage != null) {
            stage.close();
        }
    }


    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}