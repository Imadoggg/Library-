module demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    exports com.library;
    exports com.library.controllers;
    exports com.library.models;
    exports com.library.utils;

    opens com.library to javafx.graphics, javafx.fxml;
    opens com.library.controllers to javafx.fxml;
    opens com.library.models to javafx.base;
    opens com.library.utils to javafx.base;
}