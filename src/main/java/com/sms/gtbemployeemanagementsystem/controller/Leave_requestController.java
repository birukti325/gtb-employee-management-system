package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Leave_request;
import com.sms.gtbemployeemanagementsystem.Repository.Leave_requestRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class Leave_requestController {

    @FXML private TableView<Leave_request> leaveTable;
    @FXML private TableColumn<Leave_request, Long> idColumn;
    @FXML private TableColumn<Leave_request, String> employeeColumn;
    @FXML private TableColumn<Leave_request, String> typeColumn;
    @FXML private TableColumn<Leave_request, String> startDateColumn;
    @FXML private TableColumn<Leave_request, String> endDateColumn;
    @FXML private TableColumn<Leave_request, Number> durationColumn;
    @FXML private TableColumn<Leave_request, String> statusColumn;


    @Autowired
    private Leave_requestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Popup detailsPopup;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        employeeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmployee() != null ? data.getValue().getEmployee().getFullName() : "N/A"));
        setupEmployeeColumn();

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("leaveType"));

        startDateColumn.setCellValueFactory(data -> {
            Leave_request lr = data.getValue();
            return new SimpleStringProperty(lr.getStartDate() != null ? lr.getStartDate().toString() : "-");
        });

        endDateColumn.setCellValueFactory(data -> {
            Leave_request lr = data.getValue();
            if ("PERMANENT".equalsIgnoreCase(lr.getLeaveType())) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(lr.getEndDate() != null ? lr.getEndDate().toString() : "-");
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        durationColumn.setCellValueFactory(data -> {
            Leave_request lr = data.getValue();
            if ("PERMANENT".equalsIgnoreCase(lr.getLeaveType())) {
                return new javafx.beans.property.SimpleLongProperty(0);
            }
            long days = (lr.getStartDate() != null && lr.getEndDate() != null)
                    ? ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1
                    : 0;
            return new javafx.beans.property.SimpleLongProperty(days);
        });

        refreshTable();
    }

    private void setupEmployeeColumn() {
        employeeColumn.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink nameLink = new Hyperlink();

            {
                nameLink.setStyle("-fx-text-fill: #6d28d9; -fx-font-weight: bold; -fx-border-color: transparent; -fx-underline: false; -fx-padding: 0;");
                nameLink.setOnAction(e -> {
                    Leave_request request = getTableView().getItems().get(getIndex());
                    showLeaveDetailsPopup(nameLink, request);
                });
            }

            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setGraphic(null);
                } else {
                    nameLink.setText(name);
                    setGraphic(nameLink);
                }
            }
        });
    }

    private void showLeaveDetailsPopup(Hyperlink anchor, Leave_request request) {
        if (detailsPopup != null) {
            detailsPopup.hide();
        }

        VBox content = new VBox(6.0);
        content.setStyle("-fx-background-color: #D6E2F4; -fx-background-radius: 8px; "
                + "-fx-border-color: #B8CCE8; -fx-border-radius: 8px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(20,33,61,0.25), 12, 0, 0, 4);");
        content.setMaxWidth(280);

        String employeeName = request.getEmployee() != null ? request.getEmployee().getFullName() : "N/A";

        Label nameLabel = new Label(employeeName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");

        Label typeLabel = new Label("Type: " + (request.getLeaveType() != null ? request.getLeaveType() : "-"));
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7c3aed;");

        Label statusLabel = new Label("Status: " + (request.getStatus() != null ? request.getStatus() : "-"));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7c3aed;");

        Label reasonHeader = new Label("Reason:");
        reasonHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4c1d95; -fx-padding: 6 0 0 0;");

        Label reasonLabel = new Label(request.getReason() != null && !request.getReason().isBlank()
                ? request.getReason() : "No reason provided.");
        reasonLabel.setWrapText(true);
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");

        content.getChildren().addAll(nameLabel, typeLabel, statusLabel, reasonHeader, reasonLabel);

        detailsPopup = new Popup();
        detailsPopup.setAutoHide(true);
        detailsPopup.getContent().add(content);

        javafx.geometry.Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        detailsPopup.show(anchor, bounds.getMinX(), bounds.getMaxY() + 5);
    }

    private void refreshTable() {
        List<Leave_request> requests = leaveRequestRepository.findAll();
        leaveTable.setItems(FXCollections.observableArrayList(requests));
    }


    private void updateStatus(Leave_request request, String newStatus) {
        request.setStatus(newStatus);
        leaveRequestRepository.save(request);

        if ("APPROVED".equalsIgnoreCase(newStatus)
                && "PERMANENT".equalsIgnoreCase(request.getLeaveType())
                && request.getEmployee() != null) {
            Employee emp = request.getEmployee();
            emp.setActive(false);
            emp.setLeaveDate(java.time.LocalDate.now());
            employeeRepository.save(emp);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Request Updated");
        alert.setHeaderText(null);
        alert.setContentText(request.getEmployee().getFullName() + "'s leave request has been "
                + (newStatus.equals("APPROVED") ? "accepted." : "declined."));
        alert.showAndWait();

        refreshTable();
    }
}