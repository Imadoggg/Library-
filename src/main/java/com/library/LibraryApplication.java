package com;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LibraryApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // ใช้ Class.getResource แทน
            FXMLLoader loader = new FXMLLoader(LibraryApplication.class.getResource("/views/MainView.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("ระบบจัดการห้องสมุด");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}