package com.library.controllers;

import com.library.LibraryDataManager;
import com.library.models.Member;
import com.library.models.BorrowRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.Optional;

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
        System.out.println("MemberManagementController.initialize() started");
        dataManager = LibraryDataManager.getInstance();
        memberList = FXCollections.observableArrayList();

        // Setup columns
        if (idColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        }

        if (nameColumn != null) {
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }

        if (contactColumn != null) {
            contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        }

        // Add context menu for viewing borrow history
        if (memberTableView != null) {
            memberTableView.setRowFactory(tv -> {
                TableRow<Member> row = new TableRow<>();
                ContextMenu contextMenu = new ContextMenu();
                MenuItem viewHistoryItem = new MenuItem("View Borrow History");
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
        }

        refreshMemberList();
    }

    // Removed handleAddMember method since users register themselves

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
                                    member.getId().toLowerCase().contains(searchText) ||
                                    member.getContact().toLowerCase().contains(searchText))
                    .toList());
        }

        if (memberTableView != null) {
            memberTableView.setItems(memberList);
        }
    }

    private void showBorrowHistory(Member member) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Borrow History");
        dialog.setHeaderText("Borrow History for " + member.getName());

        VBox content = new VBox(10);
        ListView<String> historyList = new ListView<>();

        for (BorrowRecord record : member.getBorrowHistory()) {
            String status = record.isReturned() ? "Returned" : "Not Returned";
            historyList.getItems().add(String.format(
                    "Book: %s | Borrow Date: %s | Status: %s",
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

        if (memberTableView != null) {
            memberTableView.setItems(memberList);
        }
    }
}