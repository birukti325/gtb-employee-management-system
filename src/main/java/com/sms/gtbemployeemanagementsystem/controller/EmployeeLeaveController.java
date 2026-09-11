package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.Leave_request;
import com.sms.gtbemployeemanagementsystem.Entity.UserSession;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.Leave_requestRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class EmployeeLeaveController {

    @FXML private ComboBox<String> leaveTypeInput;
    @FXML private DatePicker startDateInput;
    @FXML private DatePicker endDateInput;
    @FXML private TextArea reasonInput;

    @FXML private TableView<Leave_request> myLeaveTable;
    @FXML private TableColumn<Leave_request, String> typeColumn;
    @FXML private TableColumn<Leave_request, String> startColumn;
    @FXML private TableColumn<Leave_request, String> endColumn;
    @FXML private TableColumn<Leave_request, String> statusColumn;
    @FXML private TableColumn<Leave_request, String> reasonColumn;

    @Autowired
    private Leave_requestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserSession userSession;

    private final ObservableList<Leave_request> myRequests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Updated ComboBox items to include GRIEF instead of PATERNITY
        leaveTypeInput.setItems(FXCollections.observableArrayList(
                "ANNUAL", "SICK", "MATERNITY", "GRIEF", "UNPAID", "PERMANENT"));

        // Column cell value factories with null-safety
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLeaveType() != null ? cellData.getValue().getLeaveType() : ""));

        startColumn.setCellValueFactory(cellData -> {
            LocalDate start = cellData.getValue().getStartDate();
            return new SimpleStringProperty(start != null ? start.toString() : "-");
        });

        endColumn.setCellValueFactory(cellData -> {
            Leave_request lr = cellData.getValue();
            if ("PERMANENT".equalsIgnoreCase(lr.getLeaveType())) {
                return new SimpleStringProperty("-");
            }
            LocalDate end = lr.getEndDate();
            return new SimpleStringProperty(end != null ? end.toString() : "-");
        });

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus() : ""));

        reasonColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReason() != null && !cellData.getValue().getReason().isBlank()
                        ? cellData.getValue().getReason() : "-"));

        reasonColumn.setCellFactory(col -> new TableCell<Leave_request, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String reason, boolean empty) {
                super.updateItem(reason, empty);
                if (empty || reason == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(reason);
                    tooltip.setText(reason);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(320);
                    setTooltip(tooltip);
                }
            }
        });

        // Toggle behavior for permanent leave requests
        leaveTypeInput.setOnAction(e -> {
            boolean isPermanent = "PERMANENT".equals(leaveTypeInput.getValue());
            startDateInput.setDisable(isPermanent);
            endDateInput.setDisable(isPermanent);
            if (isPermanent) {
                startDateInput.setValue(null);
                endDateInput.setValue(null);
            }
        });

        refreshTable();
    }

    @FXML
    private void handleSubmit() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No active session found.");
            return;
        }

        String leaveType = leaveTypeInput.getValue();
        String reason = reasonInput.getText();
        boolean isPermanent = "PERMANENT".equals(leaveType);

        LocalDate startDate;
        LocalDate endDate;

        if (isPermanent) {
            startDate = LocalDate.now();
            endDate = null;
        } else {
            startDate = startDateInput.getValue();
            endDate = endDateInput.getValue();
        }

        if (leaveType == null || (!isPermanent && (startDate == null || endDate == null))) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please select a leave type, start date, and end date.");
            return;
        }

        if (!isPermanent && endDate.isBefore(startDate)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Dates",
                    "End date cannot be before start date.");
            return;
        }

        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Employee record not found.");
            return;
        }

        Leave_request request = new Leave_request();
        request.setEmployee(employeeOpt.get());
        request.setLeaveType(leaveType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReason(reason);
        request.setStatus("PENDING");

        leaveRequestRepository.save(request);

        showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted.");

        // Clear input controls
        leaveTypeInput.setValue(null);
        startDateInput.setValue(null);
        endDateInput.setValue(null);
        startDateInput.setDisable(false);
        endDateInput.setDisable(false);
        reasonInput.clear();

        refreshTable();
    }

    private void refreshTable() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        List<Leave_request> requests = leaveRequestRepository.findByEmployee_Id(employeeId);
        myRequests.setAll(requests);
        myLeaveTable.setItems(myRequests);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}