package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DepartmentsController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentsController.class);

    @FXML private TableView<Departments> departmentTable;
    @FXML private TableColumn<Departments, Number> colDeptId;
    @FXML private TableColumn<Departments, String> colDeptName;
    @FXML private TableColumn<Departments, String> colDeptHead;
    @FXML private TableColumn<Departments, String> colLocation;
    @FXML private TableColumn<Departments, Number> colTotalEmployees;
    @FXML private TableColumn<Departments, Void> colActions;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @FXML
    public void initialize() {
        // Sequential row number instead of the raw database ID (no gaps after deletions)
        colDeptId.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        departmentTable.getItems().indexOf(data.getValue()) + 1));

        colDeptName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDeptHead.setCellValueFactory(new PropertyValueFactory<>("head"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        colTotalEmployees.setCellValueFactory(cellData -> {
            Long deptId = cellData.getValue().getId();
            long count = employeeRepository.countByDepartmentIdAndActiveTrue(deptId);
            return new SimpleLongProperty(count);
        });

        setupActionsColumn();
        loadDepartments();

        departmentTable.setRowFactory(tv -> {
            TableRow<Departments> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Departments dept = row.getItem();
                    showDepartmentRolesPopup(dept);
                }
            });
            return row;
        });
    }

    private void showDepartmentRolesPopup(Departments dept) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/sms/gtbemployeemanagementsystem/DepartmentRoles.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);

            javafx.scene.Parent popupRoot = loader.load();

            DepartmentRolesController controller = loader.getController();
            controller.setDepartment(dept);

            Scene scene = new Scene(popupRoot, 500, 450);
            Stage popup = new Stage();
            popup.setTitle(dept.getName() + " - Department Roles");
            popup.setScene(scene);
            popup.show();

        } catch (Exception e) {
            log.error("Could not open department roles view for department id={}", dept.getId(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open department roles view.");
        }
    }

    private List<String> getRegisteredEmployeeNames() {
        return employeeRepository.findAll().stream()
                .map(Employee::getFullName)
                .collect(Collectors.toList());
    }

    // Only employees who already belong to this specific department (any status)
    private List<String> getEmployeeNamesForDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(Employee::getFullName)
                .collect(Collectors.toList());
    }

    /**
     * Makes a ComboBox searchable: typing filters the dropdown live,
     * and keeps the ComboBox's value in sync with whatever is typed
     * (including empty text), so clearing the field actually sticks
     * instead of reverting on focus loss.
     */
    private void makeSearchable(ComboBox<String> comboBox, List<String> allItems) {
        comboBox.setEditable(true);
        comboBox.setItems(FXCollections.observableArrayList(allItems));

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;

            // Keep the ComboBox value synced with the raw typed text,
            // so an intentionally-cleared field stays cleared on save.
            comboBox.setValue(newText);

            String selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && selected.equals(newText)) {
                return;
            }

            ObservableList<String> filtered = FXCollections.observableArrayList();
            String lower = newText.toLowerCase();
            for (String name : allItems) {
                if (name.toLowerCase().contains(lower)) {
                    filtered.add(name);
                }
            }

            comboBox.setItems(filtered);
            if (!filtered.isEmpty() && !newText.isEmpty()) {
                comboBox.show();
            } else {
                comboBox.hide();
            }
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button menuButton = new Button("⋮");
            private final ContextMenu contextMenu = new ContextMenu();
            private final StackPane container = new StackPane(menuButton);

            {
                menuButton.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-fill: #374151; " +
                                "-fx-cursor: hand;"
                );

                MenuItem editItem = new MenuItem("Edit");
                editItem.setStyle("-fx-text-fill: black; -fx-font-size: 11px; -fx-font-weight: normal;");
                MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setStyle("-fx-text-fill: black; -fx-font-size: 11px; -fx-font-weight: normal;");

                contextMenu.getItems().addAll(editItem, deleteItem);
                contextMenu.setStyle("-fx-background-color: white;");

                editItem.setOnAction(event -> {
                    Departments dept = getTableView().getItems().get(getIndex());
                    handleEditDepartment(dept);
                });

                deleteItem.setOnAction(event -> {
                    Departments dept = getTableView().getItems().get(getIndex());
                    handleDeleteDepartment(dept);
                });

                menuButton.setOnAction(event ->
                        contextMenu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 0));

                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadDepartments() {
        List<Departments> departments = departmentsRepository.findAll(org.springframework.data.domain.Sort.by("id"));
        ObservableList<Departments> data = FXCollections.observableArrayList(departments);
        departmentTable.setItems(data);
        departmentTable.refresh();
    }

    @FXML
    private void handleAddDepartment() {
        Dialog<Departments> dialog = new Dialog<>();
        dialog.setTitle("Add Department");
        dialog.setHeaderText("Register a new department");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Department Name");

        // New department has no employees yet, so Head is picked from everyone for now
        ComboBox<String> headBox = new ComboBox<>();
        headBox.setPromptText("Type to search employee...");
        headBox.setPrefWidth(220);
        makeSearchable(headBox, getRegisteredEmployeeNames());

        TextField locationField = new TextField();
        locationField.setPromptText("Location");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Head:"), 0, 1);
        grid.add(headBox, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                Departments dept = new Departments();
                dept.setName(nameField.getText());
                String headText = headBox.getEditor().getText();
                dept.setHead(headText == null || headText.trim().isEmpty() ? null : headText.trim());
                dept.setLocation(locationField.getText());
                return dept;
            }
            return null;
        });

        Optional<Departments> result = dialog.showAndWait();

        List<String> validNamesForAdd = getRegisteredEmployeeNames();
        result.ifPresent(dept -> {
            if (dept.getName() == null || dept.getName().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Department name is required.");
                return;
            }
            if (dept.getHead() != null && !dept.getHead().trim().isEmpty() && !validNamesForAdd.contains(dept.getHead())) {
                showAlert(Alert.AlertType.WARNING, "Invalid Head", "Please select an existing employee from the list for Department Head.");
                return;
            }
            try {
                departmentsRepository.save(dept);
                showAlert(Alert.AlertType.INFORMATION, "Department Saved", "Department saved successfully.");
                loadDepartments();
            } catch (Exception e) {
                log.error("Something went wrong while saving department '{}'", dept.getName(), e);
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Something went wrong while saving the department.");
            }
        });
    }

    private void handleEditDepartment(Departments dept) {
        Dialog<Departments> dialog = new Dialog<>();
        dialog.setTitle("Edit Department");
        dialog.setHeaderText("Update department details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField(dept.getName());

        // Only this department's own employees are eligible to be Head
        ComboBox<String> headBox = new ComboBox<>();
        headBox.setPromptText("Type to search employee in this department...");
        headBox.setPrefWidth(220);
        List<String> deptEmployeeNames = getEmployeeNamesForDepartment(dept.getId());
        makeSearchable(headBox, deptEmployeeNames);

        if (dept.getHead() != null && !dept.getHead().trim().isEmpty()) {
            headBox.setValue(dept.getHead());
            headBox.getEditor().setText(dept.getHead());
        } else {
            headBox.getSelectionModel().clearSelection();
            headBox.setValue(null);
            headBox.getEditor().clear();
        }

        TextField locationField = new TextField(dept.getLocation());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Head:"), 0, 1);
        grid.add(headBox, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                dept.setName(nameField.getText());

                String headText = headBox.getEditor().getText();
                if (headText == null || headText.trim().isEmpty()) {
                    dept.setHead(null);
                } else {
                    dept.setHead(headText.trim());
                }

                dept.setLocation(locationField.getText());
                return dept;
            }
            return null;
        });

        Optional<Departments> result = dialog.showAndWait();

        result.ifPresent(updatedDept -> {
            if (updatedDept.getName() == null || updatedDept.getName().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Department name is required.");
                return;
            }
            if (updatedDept.getHead() != null && !updatedDept.getHead().trim().isEmpty()
                    && !deptEmployeeNames.contains(updatedDept.getHead())) {
                showAlert(Alert.AlertType.WARNING, "Invalid Head",
                        "Please select an employee who belongs to this department, or clear the field.");
                return;
            }
            try {
                departmentsRepository.save(updatedDept);
                showAlert(Alert.AlertType.INFORMATION, "Department Updated", "Department updated successfully.");
                loadDepartments();
            } catch (Exception e) {
                log.error("Something went wrong while updating department id={}", updatedDept.getId(), e);
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Something went wrong while updating the department.");
            }
        });
    }

    private void handleDeleteDepartment(Departments dept) {
        // Only currently-active employees need to be reassigned before deletion.
        List<Employee> activeEmployees = employeeRepository.findByDepartmentIdAndActiveTrue(dept.getId());

        if (!activeEmployees.isEmpty()) {
            boolean reassigned = showReassignDialog(dept, activeEmployees);
            if (!reassigned) {
                return;
            }
        }

        // Detach any inactive/former employees still pointing at this department
        // so they don't block deletion or become orphaned once it's gone.
        List<Employee> inactiveLeftovers = employeeRepository.findByDepartmentId(dept.getId());
        for (Employee e : inactiveLeftovers) {
            e.setDepartment(null);
            employeeRepository.save(e);
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Department");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete \"" + dept.getName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                departmentsRepository.delete(dept);
                showAlert(Alert.AlertType.INFORMATION, "Department Deleted", "Department deleted successfully.");
                loadDepartments();
            } catch (Exception e) {
                log.error("Something went wrong while deleting department id={}", dept.getId(), e);
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Something went wrong while deleting the department.");
            }
        }
    }

    private boolean showReassignDialog(Departments deptToDelete, List<Employee> employees) {
        List<Departments> otherDepartments = departmentsRepository.findAll().stream()
                .filter(d -> !d.getId().equals(deptToDelete.getId()))
                .collect(Collectors.toList());

        if (otherDepartments.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Other Departments",
                    "There are no other departments to reassign employees to. Create one first.");
            return false;
        }

        Dialog<Departments> dialog = new Dialog<>();
        dialog.setTitle("Reassign Employees");
        dialog.setHeaderText(employees.size() + " employee(s) in \"" + deptToDelete.getName() +
                "\" need a new department before it can be deleted.");

        ButtonType reassignButtonType = new ButtonType("Reassign & Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reassignButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        ComboBox<Departments> newDeptBox = new ComboBox<>(FXCollections.observableArrayList(otherDepartments));
        newDeptBox.setPromptText("Select new department");
        newDeptBox.setPrefWidth(220);

        grid.add(new Label("Employees affected:"), 0, 0);
        grid.add(new Label(employees.stream().map(Employee::getFullName).collect(Collectors.joining(", "))), 1, 0);
        grid.add(new Label("Move to:"), 0, 1);
        grid.add(newDeptBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == reassignButtonType) {
                return newDeptBox.getValue();
            }
            return null;
        });

        Optional<Departments> result = dialog.showAndWait();

        // If the dialog was cancelled, or "Reassign & Continue" was clicked
        // without actually picking a target department, we can't proceed.
        if (result.isEmpty() || result.get() == null) {
            return false;
        }

        Departments newDept = result.get();
        for (Employee e : employees) {
            e.setDepartment(newDept);
            employeeRepository.save(e);
        }

        return true;
    }

    @FXML
    private void handleViewAnalytics() {
        List<Departments> departments = departmentsRepository.findAll();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Departments dept : departments) {
            long count = employeeRepository.countByDepartmentIdAndActiveTrue(dept.getId());
            if (count > 0) {
                pieData.add(new PieChart.Data(dept.getName() + " (" + count + ")", count));
            }
        }

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Employees per Department");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);

        Scene scene = new Scene(pieChart, 700, 500);
        Stage stage = new Stage();
        stage.setTitle("Department Analytics");
        stage.setScene(scene);
        stage.show();

        String[] colors = {
                "#1E3A8A", "#0EA5E9", "#10B981", "#F59E0B",
                "#EF4444", "#8B5CF6", "#EC4899", "#14B8A6",
                "#F97316", "#6366F1"
        };

        int i = 0;
        for (PieChart.Data data : pieChart.getData()) {
            String color = colors[i % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}