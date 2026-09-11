package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AddNewController {

    @FXML private TextField fullNameInput;
    @FXML private ComboBox<String> departmentInput;
    @FXML private ComboBox<String> roleInput;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    @FXML
    public void initialize() {
        departmentInput.getItems().clear();

        try {
            List<Departments> departmentsList = departmentsRepository.findAll();
            ObservableList<String> options = FXCollections.observableArrayList();

            for (Departments dept : departmentsList) {
                if (dept.getName() != null) {
                    options.add(dept.getName());
                }
            }
            departmentInput.setItems(options);
        } catch (Exception e) {
            System.err.println("Error fetching departments:");
            e.printStackTrace();
        }

        roleInput.setEditable(true);

        // Whenever the admin picks a different department, refresh the
        // Role suggestions to that department's existing roles. A brand
        // new department with no employees yet will just show an empty
        // list, and the admin can type a fresh role in that case.
        departmentInput.valueProperty().addListener((obs, oldDept, newDept) -> {
            populateRoleOptionsForDepartment(newDept);
        });
    }

    private void populateRoleOptionsForDepartment(String departmentName) {
        roleInput.getItems().clear();
        roleInput.setValue(null);
        roleInput.getEditor().clear();

        if (departmentName == null || departmentName.trim().isEmpty()) {
            return;
        }

        Optional<Departments> departmentOpt = departmentsRepository.findByName(departmentName);
        if (departmentOpt.isEmpty()) {
            return;
        }

        List<Employee> deptEmployees = employeeRepository.findByDepartmentId(departmentOpt.get().getId());

        List<String> distinctRoles = deptEmployees.stream()
                .map(Employee::getProfession)
                .filter(role -> role != null && !role.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        roleInput.setItems(FXCollections.observableArrayList(distinctRoles));
    }

    @FXML
    private void handleSaveEmployee() {
        String name = fullNameInput.getText();
        String selectedDept = departmentInput.getValue();
        String role = roleInput.getEditor().getText();

        if (name == null || name.trim().isEmpty()
                || selectedDept == null
                || role == null || role.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please fill in all fields before saving.");
            return;
        }

        try {
            Optional<Departments> departmentOpt = departmentsRepository.findByName(selectedDept);

            if (departmentOpt.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Department Not Found",
                        "The selected department could not be found.");
                return;
            }

            Employee employee = new Employee();
            employee.setFullName(name.trim());
            employee.setDepartment(departmentOpt.get());
            employee.setProfession(role.trim());
            employee.setStatus("Active");

            employeeService.save(employee);

            showAlert(Alert.AlertType.INFORMATION, "Staff Saved",
                    name.trim() + " has been successfully added.");

            handleClearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Failed",
                    "Something went wrong while saving. Please try again.");
        }
    }

    @FXML
    private void handleClearForm() {
        fullNameInput.clear();
        roleInput.getItems().clear();
        roleInput.setValue(null);
        roleInput.getEditor().clear();
        departmentInput.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}