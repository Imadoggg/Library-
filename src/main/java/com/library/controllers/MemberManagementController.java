package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Member;
import com.library.models.BorrowRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.Optional;
import java.time.LocalDate;

public class MemberManagementController {
    @FXML private TableView<Member> memberTableView;
    @FXML private TableColumn<Member, String> idColumn;
    @FXML private TableColumn<Member, String> nameColumn;
    @FXML private TableColumn<Member, String> contactColumn;
    @FXML private TextField searchField;

    private LibraryDataManager dataManager;
    private ObservableList<Member> memberList;

    @FXML
    public void initialize() {
        dataManager = LibraryDataManager.getInstance();
        memberList = FXCollections.observableArrayList();

        // Setup columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));

        // Add context menu for viewing borrow history
        memberTableView.setRowFactory(tv -> {
            TableRow<Member> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem viewHistoryItem = new MenuItem("ดูประวัติการยืม");
            viewHistoryItem.setOnAction(event -> showBorrowHistory(row.getItem()));
            contextMenu.getItems().add(viewHistoryItem);

            // Set context menu only if row is not empty
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        refreshMemberList();
    }

    @FXML
    private void handleAddMember() {
        Dialog<Member> dialog = new Dialog<>();
        dialog.setTitle("เพิ่มสมาชิกใหม่");
        dialog.setHeaderText("กรุณากรอกข้อมูลสมาชิก");

        // Set the button types
        ButtonType addButtonType = new ButtonType("เพิ่ม", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("ชื่อ-นามสกุล");
        TextField contactField = new TextField();
        contactField.setPromptText("เบอร์โทรศัพท์");

        grid.add(new Label("ชื่อ-นามสกุล:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("เบอร์โทรศัพท์:"), 0, 1);
        grid.add(contactField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to Member object when button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String id = "M" + String.format("%03d", dataManager.getAllMembers().size() + 1);
                return new Member(id, nameField.getText(), contactField.getText());
            }
            return null;
        });

        Optional<Member> result = dialog.showAndWait();
        result.ifPresent(member -> {
            dataManager.addMember(member);
            refreshMemberList();
        });
    }

    @FXML
    private void handleSearchMember() {
        String searchText = searchField.getText().trim().toLowerCase();
        memberList.clear();

        if (searchText.isEmpty()) {
            memberList.addAll(dataManager.getAllMembers());
        } else {
            memberList.addAll(dataManager.getAllMembers().stream()
                    .filter(member ->
                            member.getName().toLowerCase().contains(searchText) ||
                                    member.getId().toLowerCase().contains(searchText))
                    .toList());
        }
    }

    private void showBorrowHistory(Member member) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("ประวัติการยืม");
        dialog.setHeaderText("ประวัติการยืมของ " + member.getName());

        VBox content = new VBox(10);
        ListView<String> historyList = new ListView<>();

        for (BorrowRecord record : member.getBorrowHistory()) {
            String status = record.isReturned() ? "คืนแล้ว" : "ยังไม่คืน";
            historyList.getItems().add(String.format(
                    "หนังสือ: %s | วันที่ยืม: %s | สถานะ: %s",
                    record.getBook().getTitle(),
                    record.getBorrowDate().toLocalDate(),
                    status
            ));
        }

        content.getChildren().add(historyList);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void refreshMemberList() {
        memberList.clear();
        memberList.addAll(dataManager.getAllMembers());
        memberTableView.setItems(memberList);
    }
}
