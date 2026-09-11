package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardOverviewController {

    @FXML private Label adminCountLabel;
    @FXML private Label employeeCountLabel;
    @FXML private Label newHiresLabel;

    @FXML private TableView<Employee> recentEmployeeTable;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> departmentColumn;
    @FXML private TableColumn<Employee, String> roleColumn;

    @FXML private BarChart<String, Number> departmentBarChart;
    @FXML private CategoryAxis departmentXAxis;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private UserRepository userRepository;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));

        departmentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDepartmentName()));

        roleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProfession()));

        refreshDashboard();
    }

    private void loadDashboardMetrics() {
        try {
            long adminCount = userRepository.countActiveAdminUsers();
            long employeeCount = employeeRepository.countByActiveTrue();

            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            long newHires = employeeRepository.countByCreatedAtAfterAndActiveTrue(startOfMonth.minusDays(1));

            List<Employee> employees = employeeRepository.findAllWithDepartment().stream()
                    .filter(Employee::isActive)
                    .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                    .limit(5)
                    .collect(Collectors.toList());

            ObservableList<Employee> dataList = FXCollections.observableArrayList(employees);

            Platform.runLater(() -> {
                adminCountLabel.setText(String.valueOf(adminCount));
                employeeCountLabel.setText(String.valueOf(employeeCount));
                newHiresLabel.setText(String.valueOf(newHires));
                recentEmployeeTable.setItems(dataList);
            });

        } catch (Exception e) {
            System.err.println("Error loading dashboard metrics:");
            e.printStackTrace();
        }
    }

    private void loadDepartmentChart() {
        try {
            List<Departments> departments = departmentsRepository.findAll(org.springframework.data.domain.Sort.by("id"));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Departments dept : departments) {
                long count = employeeRepository.countByDepartmentIdAndActiveTrue(dept.getId());
                if (count > 0) {
                    series.getData().add(new XYChart.Data<>(dept.getName(), count));
                }
            }

            Platform.runLater(() -> {
                departmentXAxis.setAnimated(false);
                departmentXAxis.setTickLabelRotation(0);

                if (!departmentBarChart.getData().isEmpty()) {
                    departmentBarChart.getData().get(0).getData().clear();
                    departmentBarChart.getData().get(0).getData().addAll(series.getData());
                } else {
                    departmentBarChart.getData().add(series);
                }

                String[] colors = {
                        "#7C3AED", "#0D9488", "#F59E0B",
                        "#EF4444", "#3B82F6", "#EC4899",
                        "#14B8A6", "#F97316"
                };

                int i = 0;
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        String color = colors[i % colors.length];
                        data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                    }
                    i++;
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading department chart:");
            e.printStackTrace();
        }
    }

    public void refreshDashboard() {
        loadDashboardMetrics();
        loadDepartmentChart();
    }
}