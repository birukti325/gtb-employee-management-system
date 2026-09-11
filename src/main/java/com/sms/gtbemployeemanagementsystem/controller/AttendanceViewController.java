package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Attendance;
import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;
import com.sms.gtbemployeemanagementsystem.Service.AttendanceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttendanceViewController {

    @FXML private TableView<Attendance> attendanceTable;
    @FXML private TableColumn<Attendance, String> idCol, nameCol, deptCol, dateCol, inCol, lunchOutCol, lunchInCol, outCol, statusCol;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private Button exportBtn;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private com.sms.gtbemployeemanagementsystem.Service.ExportService exportService;

    private LocalDate currentViewDate = LocalDate.now();
    private LocalDate selectedDate = LocalDate.now();

    private final ObservableList<Attendance> masterData = FXCollections.observableArrayList();
    private FilteredList<Attendance> filteredData;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDepartmentFilter();

        filteredData = new FilteredList<>(masterData, a -> true);
        attendanceTable.setItems(filteredData);

        searchField.setOnAction(e -> applyFilters());
        departmentFilter.setOnAction(e -> applyFilters());

        generateCalendar(currentViewDate);
        loadAllData();
    }

    private void loadAllData() {
        List<Attendance> all = attendanceService.findAllWithEmployeeAndDepartment();
        all.sort((a, b) -> {
            int dateCompare = b.getDate().compareTo(a.getDate()); // newest date first
            if (dateCompare != 0) return dateCompare;
            if (a.getCheckIn() == null || b.getCheckIn() == null) return 0;
            return b.getCheckIn().compareTo(a.getCheckIn()); // newest check-in time first
        });
        masterData.setAll(all);
        applyFilters();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(attendanceTable.getItems().indexOf(cellData.getValue()) + 1)));

        nameCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmployee() != null ? data.getValue().getEmployee().getFullName() : "N/A"));

        deptCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmployee() != null ? data.getValue().getEmployee().getDepartmentName() : "N/A"));

        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDate() != null ? data.getValue().getDate().toString() : "-"));

        inCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));

        lunchOutCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLunchOut() != null ? data.getValue().getLunchOut().toString() : "-"));

        lunchInCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLunchIn() != null ? data.getValue().getLunchIn().toString() : "-"));

        outCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadDepartmentFilter() {
        List<String> names = departmentsRepository.findAll()
                .stream()
                .map(Departments::getName)
                .collect(Collectors.toList());
        departmentFilter.setItems(FXCollections.observableArrayList(names));
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Attendance Export");
        fileChooser.setInitialFileName("attendance_" + java.time.LocalDate.now() + ".xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showSaveDialog(attendanceTable.getScene().getWindow());
        if (file == null) return;

        List<String> headers = List.of("ID", "Name", "Department", "Date", "Clock In", "Lunch Out", "Lunch Back", "Clock Out", "Status");
        List<List<Object>> rows = new ArrayList<>();

        int rowNum = 1;
        for (Attendance a : attendanceTable.getItems()) {
            rows.add(List.of(
                    rowNum++,
                    a.getEmployee() != null ? a.getEmployee().getFullName() : "N/A",
                    a.getEmployee() != null ? a.getEmployee().getDepartmentName() : "N/A",
                    a.getDate() != null ? a.getDate().toString() : "-",
                    a.getCheckIn() != null ? a.getCheckIn().toString() : "-",
                    a.getLunchOut() != null ? a.getLunchOut().toString() : "-",
                    a.getLunchIn() != null ? a.getLunchIn().toString() : "-",
                    a.getCheckOut() != null ? a.getCheckOut().toString() : "-",
                    a.getStatus() != null ? a.getStatus() : "-"
            ));
        }

        try {
            exportService.exportToExcel("Attendance", headers, rows, file);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Attendance data exported successfully.");
            alert.setTitle("Export Complete");
            alert.setHeaderText(null);
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not save the Excel file. " + e.getMessage());
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void generateCalendar(LocalDate date) {
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        monthLabel.setText(date.getMonth().name() + " " + date.getYear());

        int daysInMonth = date.lengthOfMonth();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate thisDate = date.withDayOfMonth(i);
            Button dayButton = new Button(String.valueOf(i));

            boolean isSelected = thisDate.equals(selectedDate);
            dayButton.setStyle(isSelected
                    ? "-fx-background-color: #14213D; -fx-text-fill: white; -fx-background-radius: 20;"
                    : "-fx-background-color: transparent;");

            dayButton.setOnAction(e -> {
                selectedDate = thisDate;
                currentViewDate = thisDate;
                generateCalendar(currentViewDate);
                loadTableData(selectedDate);
            });

            calendarGrid.add(dayButton, (i + dayOfWeek - 1) % 7, (i + dayOfWeek - 1) / 7 + 1);
        }
    }

    @FXML
    private void handlePrevMonth() {
        currentViewDate = currentViewDate.minusMonths(1);
        generateCalendar(currentViewDate);
    }

    @FXML
    private void handleNextMonth() {
        currentViewDate = currentViewDate.plusMonths(1);
        generateCalendar(currentViewDate);
    }

    private void loadTableData(LocalDate date) {
        masterData.setAll(attendanceService.findByDate(date));
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedDept = departmentFilter.getValue();

        filteredData.setPredicate(attendance -> {
            String name = attendance.getEmployee() != null ? attendance.getEmployee().getFullName() : "";
            String dept = attendance.getEmployee() != null ? attendance.getEmployee().getDepartmentName() : "";

            boolean matchesSearch = searchText.isEmpty() || name.toLowerCase().contains(searchText);
            boolean matchesDept = selectedDept == null || selectedDept.isEmpty() || selectedDept.equals(dept);

            return matchesSearch && matchesDept;
        });
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        departmentFilter.setValue(null);
        loadAllData();
    }
}