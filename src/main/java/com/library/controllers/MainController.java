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
    private String currentRole = null;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();

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
        if ("admin".equals(role)) {
            mainTabPane.getTabs().forEach(tab -> tab.setDisable(false));
        } else if ("user".equals(role)) {
            loginTab.setDisable(false);
            bookTab.setDisable(false);
            borrowReturnTab.setDisable(false);
            memberTab.setDisable(true);
        }

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