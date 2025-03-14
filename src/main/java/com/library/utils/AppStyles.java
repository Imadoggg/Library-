package com.library.utils;

public class AppStyles {
    public static final String ADMIN_PRIMARY_COLOR = "#3F51B5";
    public static final String USER_PRIMARY_COLOR = "#2196F3";
    public static final String SUCCESS_COLOR = "#4CAF50";
    public static final String WARNING_COLOR = "#FFC107";
    public static final String DANGER_COLOR = "#F44336";
    public static final String LIGHT_GRAY = "#F5F5F5";


    //  สไตล์ปุ่ม
    public static final String PRIMARY_BUTTON = "-fx-background-color: " + ADMIN_PRIMARY_COLOR + "; -fx-text-fill: white; -fx-cursor: hand;";
    public static final String USER_BUTTON = "-fx-background-color: " + USER_PRIMARY_COLOR + "; -fx-text-fill: white; -fx-cursor: hand;";
    public static final String SUCCESS_BUTTON = "-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-cursor: hand;";
    public static final String DANGER_BUTTON = "-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; -fx-cursor: hand;";

    // (VBox)
    public static final String CARD_STYLE = "-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 15;";

    // สไตล์ข้อความ
    public static final String HEADER_TEXT = "-fx-font-size: 24; -fx-font-weight: bold;";
    public static final String SUBHEADER_TEXT = "-fx-font-size: 18; -fx-font-weight: bold;";
    public static final String NORMAL_TEXT = "-fx-font-size: 14;";
    public static final String SMALL_TEXT = "-fx-font-size: 12; -fx-fill: #757575;";

    // TableView
    public static final String TABLE_HEADER = "-fx-background-color: " + LIGHT_GRAY + "; -fx-font-weight: bold;";

    //  สไตล์สำหรับส่วนหัว
    public static final String ADMIN_HEADER = "-fx-background-color: " + ADMIN_PRIMARY_COLOR + "; -fx-padding: 10;";
    public static final String USER_HEADER = "-fx-background-color: " + USER_PRIMARY_COLOR + "; -fx-padding: 10;";

    //  สไตล์สำหรับการแสดงสถานะ
    public static final String AVAILABLE_STATUS = "-fx-text-fill: " + SUCCESS_COLOR + ";";
    public static final String BORROWED_STATUS = "-fx-text-fill: " + DANGER_COLOR + ";";

    //  Sidebar
    public static final String SIDEBAR_STYLE = "-fx-background-color: white; -fx-padding: 10;";
}