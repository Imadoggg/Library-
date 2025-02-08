package com.library.controllers;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.TabPane;

public class MainController {
    @FXML
    private TabPane mainTabPane;

    @FXML
    public void initialize() {
        // เริ่มต้นการทำงาน
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}