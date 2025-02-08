module org.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;

    opens com.library to javafx.fxml, javafx.base;
    opens com.library.controllers to javafx.fxml, javafx.base;
    opens com.library.models to javafx.base;

    exports com.library;
    exports com.library.controllers;
    exports com.library.models;
}