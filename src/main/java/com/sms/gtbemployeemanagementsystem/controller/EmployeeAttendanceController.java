package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Attendance;
import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Security.SecurityConfig;
import com.sms.gtbemployeemanagementsystem.Service.AttendanceService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
public class EmployeeAttendanceController {

    @FXML private Label todayStatusLabel;
    @FXML private Button checkInBtn;
    @FXML private Button checkOutBtn;
    @FXML private Button lunchOutBtn;
    @FXML private Button lunchInBtn;

    @FXML private TableView<Attendance> myAttendanceTable;
    @FXML private TableColumn<Attendance, String> dateColumn;
    @FXML private TableColumn<Attendance, String> checkInColumn;
    @FXML private TableColumn<Attendance, String> lunchOutColumn;
    @FXML private TableColumn<Attendance, String> lunchInColumn;
    @FXML private TableColumn<Attendance, String> checkOutColumn;
    @FXML private TableColumn<Attendance, String> statusColumn;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SecurityConfig.SessionContext sessionContext;

    private Attendance todayRecord;

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        lunchOutColumn.setCellValueFactory(new PropertyValueFactory<>("lunchOut"));
        lunchInColumn.setCellValueFactory(new PropertyValueFactory<>("lunchIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadTodayStatus();
        loadHistory();
    }

    private void loadTodayStatus() {
        Long empId = sessionContext.getCurrentEmployeeId();
        if (empId == null) return;

        List<Attendance> todayList = attendanceService.findByEmployeeId(empId)
                .stream()
                .filter(a -> a.getDate().equals(LocalDate.now()))
                .toList();

        if (!todayList.isEmpty()) {
            todayRecord = todayList.get(0);

            // Once checked in today, Check In is permanently disabled for the rest of the day
            checkInBtn.setDisable(true);

            boolean onLunch = todayRecord.getLunchOut() != null && todayRecord.getLunchIn() == null;

            // Lunch Out is only available once, after check-in and before check-out
            lunchOutBtn.setDisable(todayRecord.getLunchOut() != null);

            // Lunch Back is only available while currently on lunch
            lunchInBtn.setDisable(!onLunch);

            // Block Check Out while the employee is still on lunch
            checkOutBtn.setDisable(onLunch);

            if (todayRecord.getCheckOut() != null) {
                todayStatusLabel.setText("Checked out at " + todayRecord.getCheckOut());
            } else if (onLunch) {
                todayStatusLabel.setText("On lunch since " + todayRecord.getLunchOut());
            } else {
                todayStatusLabel.setText("Checked in at " + todayRecord.getCheckIn());
            }
        } else {
            todayRecord = null;
            todayStatusLabel.setText("Not checked in yet");
            checkInBtn.setDisable(false);
            checkOutBtn.setDisable(true);
            lunchOutBtn.setDisable(true);
            lunchInBtn.setDisable(true);
        }
    }

    private void loadHistory() {
        Long empId = sessionContext.getCurrentEmployeeId();
        if (empId == null) return;

        List<Attendance> history = attendanceService.findByEmployeeId(empId);
        myAttendanceTable.setItems(FXCollections.observableArrayList(history));
    }

    @FXML
    private void handleCheckIn() {
        Long empId = sessionContext.getCurrentEmployeeId();
        if (empId == null) {
            showAlert(Alert.AlertType.ERROR, "Not Logged In", "No employee account is linked to this login.");
            return;
        }

        Optional<Employee> employeeOpt = employeeRepository.findById(empId);
        if (employeeOpt.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Employee record not found.");
            return;
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employeeOpt.get());
        attendance.setDate(LocalDate.now());
        attendance.setCheckIn(LocalTime.now().withNano(0));
        attendance.setStatus("PRESENT");

        attendanceService.save(attendance);

        showAlert(Alert.AlertType.INFORMATION, "Checked In", "You have successfully checked in.");

        loadTodayStatus();
        loadHistory();
    }

    @FXML
    private void handleLunchOut() {
        if (todayRecord == null) {
            showAlert(Alert.AlertType.WARNING, "Not Checked In", "You need to check in before going to lunch.");
            return;
        }

        todayRecord.setLunchOut(LocalTime.now().withNano(0));
        attendanceService.save(todayRecord);

        showAlert(Alert.AlertType.INFORMATION, "Lunch Started", "Enjoy your lunch break!");

        loadTodayStatus();
        loadHistory();
    }

    @FXML
    private void handleLunchIn() {
        if (todayRecord == null || todayRecord.getLunchOut() == null) {
            showAlert(Alert.AlertType.WARNING, "Not On Lunch", "You haven't started your lunch break yet.");
            return;
        }

        todayRecord.setLunchIn(LocalTime.now().withNano(0));
        attendanceService.save(todayRecord);

        showAlert(Alert.AlertType.INFORMATION, "Welcome Back", "Lunch break ended.");

        loadTodayStatus();
        loadHistory();
    }

    @FXML
    private void handleCheckOut() {
        if (todayRecord == null) {
            showAlert(Alert.AlertType.WARNING, "Not Checked In", "You need to check in before checking out.");
            return;
        }

        todayRecord.setCheckOut(LocalTime.now().withNano(0));
        attendanceService.save(todayRecord);

        showAlert(Alert.AlertType.INFORMATION, "Checked Out", "You have successfully checked out.");

        loadTodayStatus();
        loadHistory();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}