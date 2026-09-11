package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.EmploymentHistory;
import com.sms.gtbemployeemanagementsystem.Entity.Payroll;
import com.sms.gtbemployeemanagementsystem.Entity.StaffDocument;
import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollRepository;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import com.sms.gtbemployeemanagementsystem.Service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DirectoryController {

    @FXML private TableView<Employee> directoryTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> departmentColumn;
    @FXML private TableColumn<Employee, String> roleColumn;
    @FXML private TableColumn<Employee, String> statusColumn;
    @FXML private TableColumn<Employee, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private Button clearFilterBtn;
    @FXML private TableColumn<Employee, Void> photoColumn;

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentsRepository departmentsRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PayrollRepository payrollRepository; // Injected PayrollRepository
    @Autowired private EmployeeService employeeService;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Autowired private com.sms.gtbemployeemanagementsystem.Service.StaffDocumentService staffDocumentService;

    private final ObservableList<Employee> employeeMasterData = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredData;
    private final Map<Long, String> employeeRoleMap = new HashMap<>();
    private final Map<Long, User> employeeUserMap = new HashMap<>();
    private static final Path PHOTOS_DIR =
            Path.of(System.getProperty("user.home"), ".gtb_ems", "profile_photos");

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        directoryTable.getItems().indexOf(data.getValue()) + 1).asObject());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("profession"));

        statusColumn.setCellValueFactory(cellData -> {
            String role = employeeRoleMap.getOrDefault(cellData.getValue().getId(), "Employee");
            return new javafx.beans.property.SimpleStringProperty(role);
        });

        nameColumn.setCellFactory(col -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setOnMouseClicked(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #7c3aed; -fx-cursor: hand;");
                    setOnMouseClicked(event -> {
                        Employee employee = getTableView().getItems().get(getIndex());
                        openDocumentsDialog(employee);
                    });
                }
            }
        });

        setupPhotoColumn();
        setupActionsColumn();
        loadEmployeeRoleMap();
        refreshTable();

        List<String> departmentNames = departmentsRepository.findAll().stream()
                .map(Departments::getName).collect(Collectors.toList());
        departmentFilter.setItems(FXCollections.observableArrayList(departmentNames));

        searchField.setOnAction(e -> applyFilters());
        departmentFilter.setOnAction(e -> applyFilters());

        if (clearFilterBtn != null) {
            clearFilterBtn.setOnAction(e -> clearFilters());
        }
    }

    private void loadEmployeeRoleMap() {
        employeeRoleMap.clear();
        employeeUserMap.clear();
        userRepository.findAll().forEach(u -> {
            if (u.getEmployee() != null) {
                employeeRoleMap.put(u.getEmployee().getId(), u.getRole());
                employeeUserMap.put(u.getEmployee().getId(), u);
            }
        });
    }

    private void refreshTable() {
        List<Employee> employees = employeeRepository.findAllWithDepartment();

        List<Employee> activeEmployees = employees.stream()
                .filter(Employee::isActive)
                .collect(Collectors.toList());
        activeEmployees.sort(Comparator.comparingLong(Employee::getId));
        employeeMasterData.setAll(activeEmployees);
        filteredData = new FilteredList<>(employeeMasterData, e -> true);
        directoryTable.setItems(filteredData);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button actionButton = new Button("⋮");
            private final ContextMenu contextMenu = new ContextMenu();
            {
                actionButton.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
                MenuItem editItem = new MenuItem("Edit");
                MenuItem deleteItem = new MenuItem("Delete");
                editItem.setOnAction(e -> openEditDialog(getTableView().getItems().get(getIndex())));
                deleteItem.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));
                contextMenu.getItems().addAll(editItem, deleteItem);
                actionButton.setOnAction(e -> contextMenu.show(actionButton, javafx.geometry.Side.BOTTOM, 0, 0));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionButton);
            }
        });
    }

    private void openEditDialog(Employee employee) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Edit Employee");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(employee.getFullName());

        ComboBox<String> deptBox = new ComboBox<>(FXCollections.observableArrayList(
                departmentsRepository.findAll().stream().map(Departments::getName).collect(Collectors.toList())));
        deptBox.setValue(employee.getDepartmentName());

        ComboBox<String> roleField = new ComboBox<>();
        roleField.setEditable(true);
        roleField.setPrefWidth(220);
        roleField.setItems(FXCollections.observableArrayList(
                DepartmentRoleConfig.getRolesFor(deptBox.getValue())));
        roleField.setValue(employee.getProfession());

        deptBox.setOnAction(e -> {
            String selectedDept = deptBox.getValue();
            roleField.setItems(FXCollections.observableArrayList(
                    DepartmentRoleConfig.getRolesFor(selectedDept)));
            roleField.setValue(null);
            roleField.getEditor().clear();
        });

        // Fetch current payroll record for salary pre-population
        List<Payroll> existingPayrolls = payrollRepository.findByEmployeeId(employee.getId());
        double currentBaseSalary = existingPayrolls.isEmpty() ? 0.0 : existingPayrolls.get(existingPayrolls.size() - 1).getBaseSalary();

        // Editable Base Salary Field
        TextField salaryField = new TextField(String.format("%.2f", currentBaseSalary));

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Employee", "Admin"));
        statusBox.setValue(employeeRoleMap.getOrDefault(employee.getId(), "Employee"));

        CheckBox payrollAccessBox = new CheckBox("Payroll access (edit)");
        CheckBox departmentAccessBox = new CheckBox("Department management access");
        CheckBox projectAccessBox = new CheckBox("Project management access");

        User existingUserForAccess = employeeUserMap.get(employee.getId());
        if (existingUserForAccess != null) {
            payrollAccessBox.setSelected(existingUserForAccess.isPayrollAccess());
            departmentAccessBox.setSelected(existingUserForAccess.isDepartmentAccess());
            projectAccessBox.setSelected(existingUserForAccess.isProjectAccess());
        }

        VBox accessBox = new VBox(6, payrollAccessBox, departmentAccessBox, projectAccessBox);

        Runnable syncAccessBoxState = () ->
                accessBox.setDisable("Admin".equalsIgnoreCase(statusBox.getValue()));
        statusBox.setOnAction(e -> syncAccessBoxState.run());
        syncAccessBoxState.run();

        ComboBox<String> employmentStatusBox = new ComboBox<>(FXCollections.observableArrayList("Active", "Inactive"));
        employmentStatusBox.setValue(employee.isActive() ? "Active" : "Inactive");

        DatePicker leaveDatePicker = new DatePicker(
                employee.getLeaveDate() != null ? employee.getLeaveDate() : java.time.LocalDate.now());
        leaveDatePicker.setDisable(employee.isActive());

        employmentStatusBox.setOnAction(e ->
                leaveDatePicker.setDisable("Active".equals(employmentStatusBox.getValue())));

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Department:"), 0, 1); grid.add(deptBox, 1, 1);
        grid.add(new Label("Role:"), 0, 2); grid.add(roleField, 1, 2);
        grid.add(new Label("Base Salary (ETB):"), 0, 3); grid.add(salaryField, 1, 3); // Added Salary Row
        grid.add(new Label("Status:"), 0, 4); grid.add(statusBox, 1, 4);
        grid.add(new Label("Employment:"), 0, 5); grid.add(employmentStatusBox, 1, 5);
        grid.add(new Label("System Access:"), 0, 6);
        grid.add(accessBox, 1, 6);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(type -> {
            if (type == saveButtonType) {
                employee.setFullName(nameField.getText());
                employee.setProfession(roleField.getValue());
                departmentsRepository.findByName(deptBox.getValue()).ifPresent(employee::setDepartment);

                boolean nowActive = "Active".equals(employmentStatusBox.getValue());
                employee.setActive(nowActive);

                if (!nowActive) {
                    employee.setLeaveDate(leaveDatePicker.getValue());
                } else {
                    employee.setLeaveDate(null);
                }

                // Parse and update/save salary to payroll table
                try {
                    double newSalary = Double.parseDouble(salaryField.getText().trim());
                    saveOrUpdateEmployeePayroll(employee.getId(), newSalary);
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Salary", "Salary must be a valid numeric value.");
                }

                return employee;
            }
            return null;
        });

        String originalStatus = employeeRoleMap.getOrDefault(employee.getId(), "Employee");

        dialog.showAndWait().ifPresent(updated -> {
            employeeService.save(updated);

            String newStatus = statusBox.getValue();
            boolean statusChanged = !originalStatus.equalsIgnoreCase(newStatus);

            boolean newPayrollAccess = payrollAccessBox.isSelected();
            boolean newDepartmentAccess = departmentAccessBox.isSelected();
            boolean newProjectAccess = projectAccessBox.isSelected();

            User linkedUser = employeeUserMap.get(updated.getId());
            boolean anyAccessGranted = newPayrollAccess || newDepartmentAccess || newProjectAccess;

            if (linkedUser != null) {
                if (statusChanged) {
                    linkedUser.setRole(newStatus);
                }
                linkedUser.setPayrollAccess(newPayrollAccess);
                linkedUser.setDepartmentAccess(newDepartmentAccess);
                linkedUser.setProjectAccess(newProjectAccess);
                userRepository.save(linkedUser);

            } else if (statusChanged || anyAccessGranted) {
                User newUser = new User();
                newUser.setUsername(generateUsername(updated.getFullName(), updated.getId()));
                newUser.setPassword(passwordEncoder.encode("changeme123"));
                newUser.setRole(newStatus);
                newUser.setEmployee(updated);
                newUser.setPayrollAccess(newPayrollAccess);
                newUser.setDepartmentAccess(newDepartmentAccess);
                newUser.setProjectAccess(newProjectAccess);
                userRepository.save(newUser);

                showAlert(Alert.AlertType.INFORMATION, "Login Created",
                        "A login was auto-created for " + updated.getFullName()
                                + "\nUsername: " + newUser.getUsername()
                                + "\nTemporary password: changeme123"
                                + "\nPlease share this with them and have them change it.");
            }

            loadEmployeeRoleMap();
            refreshTable();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Changes saved successfully.");
        });
    }

    /**
     * Helper method to recalculate deductions/pension and sync with PostgreSQL payroll table
     */
    private void saveOrUpdateEmployeePayroll(Long employeeId, double baseSalary) {
        String currentMonthPeriod = YearMonth.now().toString(); // e.g. "2026-09"

        Optional<Payroll> payrollOpt = payrollRepository.findByEmployeeIdAndPayPeriod(employeeId, currentMonthPeriod);
        Payroll payroll = payrollOpt.orElseGet(Payroll::new);

        payroll.setEmployeeId(employeeId);
        payroll.setPayPeriod(currentMonthPeriod);
        payroll.setBaseSalary(baseSalary);

        // Calculate standard deductions (e.g. 7% Pension Emp, 11% Pension Employer, Tax estimated)
        double empPension = baseSalary * 0.07;
        double employerPension = baseSalary * 0.11;
        double estimatedTax = baseSalary * 0.15; // Standard estimated tax
        double netPayout = baseSalary - (empPension + estimatedTax);

        payroll.setPension(empPension);
        payroll.setEmployerPension(employerPension);
        payroll.setTax(estimatedTax);
        payroll.setDeductions(0.0);
        payroll.setAllowances(0.0);
        payroll.setTransportAllowance(0.0);
        payroll.setNetPayout(Math.max(netPayout, 0.0));

        payrollRepository.save(payroll);
    }

    private String generateUsername(String fullName, Long employeeId) {
        String base = fullName.toLowerCase().replaceAll("\\s+", ".");
        return base + "." + employeeId;
    }

    private void confirmAndDelete(Employee employee) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove " + employee.getFullName() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            employeeService.softDeleteById(employee.getId());
            refreshTable();
        }
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        departmentFilter.setValue(null);
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String selectedDept = departmentFilter.getValue();

        filteredData.setPredicate(e -> {
            boolean matchesSearch = searchText.isEmpty()
                    || e.getFullName().toLowerCase().contains(searchText)
                    || (e.getProfession() != null && e.getProfession().toLowerCase().contains(searchText));

            boolean matchesDept = selectedDept == null || selectedDept.equals(e.getDepartmentName());

            return matchesSearch && matchesDept;
        });
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void setupPhotoColumn() {
        photoColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Circle placeholder = new Circle(16, javafx.scene.paint.Color.web("#B0E0E6"));

            {
                imageView.setFitWidth(32);
                imageView.setFitHeight(32);
                imageView.setPreserveRatio(false);
                Circle clip = new Circle(16, 16, 16);
                imageView.setClip(clip);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Employee employee = getTableView().getItems().get(getIndex());
                File photoFile = findExistingPhoto(employee.getId());

                if (photoFile != null) {
                    imageView.setImage(new Image(photoFile.toURI().toString()));
                    setGraphic(imageView);
                } else {
                    setGraphic(placeholder);
                }
            }
        });
    }

    private File findExistingPhoto(Long employeeId) {
        File dir = PHOTOS_DIR.toFile();
        if (!dir.exists()) return null;
        File[] matches = dir.listFiles((d, name) -> name.startsWith("profile_" + employeeId + "."));
        return (matches != null && matches.length > 0) ? matches[0] : null;
    }

    // ==================== Employee Documents Viewer Dialog ====================

    private void openDocumentsDialog(Employee employee) {
        Stage stage = new Stage();
        stage.setTitle(employee.getFullName() + " - Employee Details & Documents");

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: white; -fx-padding: 25px;");

        VBox titleBox = new VBox(4);
        Label header = new Label(employee.getFullName());
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black;");
        Label subHeader = new Label("Employee Information & Documents");
        subHeader.setStyle("-fx-font-size: 12px; -fx-text-fill: #1D3A8A;");
        titleBox.getChildren().addAll(header, subHeader);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(12);
        detailsGrid.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-border-color: #B0E0E6; -fx-border-radius: 8px;");

        EmploymentHistory history = employee.getEmploymentHistory();

        detailsGrid.add(createDetailBox("Guarantor Name",
                employee.getGuarantorName() != null ? employee.getGuarantorName() : "Not provided"), 0, 0);
        detailsGrid.add(createDetailBox("Guarantor Phone",
                employee.getGuarantorContact() != null ? employee.getGuarantorContact() : "Not provided"), 1, 0);

        detailsGrid.add(createDetailBox("Employment Agreement Contract",
                (history != null && history.getContractAgreement() != null) ? history.getContractAgreement() : "Not provided"), 0, 1);
        detailsGrid.add(createDetailBox("Birth Date",
                employee.getBirthYear() != null ? employee.getBirthYear().toString() : "Not provided"), 1, 1);

        detailsGrid.add(createDetailBox("Employment Conditions",
                (history != null && history.getEmploymentType() != null) ? history.getEmploymentType() : "Not provided"), 0, 2);
        detailsGrid.add(createDetailBox("Employment Date",
                (history != null && history.getJoiningDate() != null) ? history.getJoiningDate().toString() : "Not provided"), 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        detailsGrid.getColumnConstraints().addAll(col1, col2);

        Label docsTitle = new Label("Uploaded Documents");
        docsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");

        VBox docsBox = new VBox(8);
        List<StaffDocument> docs = staffDocumentService.getDocuments(employee.getId());

        if (docs.isEmpty()) {
            Label empty = new Label("No documents uploaded yet.");
            empty.setStyle("-fx-text-fill: #1D3A8A; -fx-font-size: 12px;");
            docsBox.getChildren().add(empty);
        } else {
            for (StaffDocument doc : docs) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: white; -fx-background-radius: 6px; -fx-padding: 10px; -fx-border-color: #B0E0E6; -fx-border-radius: 6px;");
                VBox info = new VBox(2);
                Label typeLabel = new Label(formatDocType(doc.getDocumentType()));
                typeLabel.setStyle("-fx-text-fill: #4c1d95; -fx-font-weight: bold; -fx-font-size: 12px;");
                Label nameLabel = new Label(doc.getFileName());
                nameLabel.setStyle("-fx-text-fill: #1D3A8A; -fx-font-size: 11px;");
                info.getChildren().addAll(typeLabel, nameLabel);
                HBox.setHgrow(info, Priority.ALWAYS);

                Button viewBtn = new Button("View");
                viewBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
                viewBtn.setOnAction(e -> openDocumentFile(staffDocumentService.resolveFile(doc)));

                row.getChildren().addAll(info, viewBtn);
                docsBox.getChildren().add(row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(docsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(titleBox, detailsGrid, docsTitle, scrollPane);

        Scene scene = new Scene(root, 580, 580);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createDetailBox(String labelText, String valueText) {
        VBox box = new VBox(4);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #1D3A8A; -fx-font-weight: bold;");

        TextField valueField = new TextField(valueText);
        valueField.setEditable(false);
        valueField.setStyle("-fx-background-color: #E8F0FB; -fx-text-fill: black; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #B0E0E6; -fx-pref-height: 30px;");

        box.getChildren().addAll(label, valueField);
        return box;
    }

    private void openDocumentFile(File file) {
        if (file == null || !file.exists()) {
            showAlert(Alert.AlertType.ERROR, "File Not Found",
                    "This document's file could not be found on disk.\nIt may have been moved, renamed, or deleted.");
            return;
        }

        String name = file.getName().toLowerCase();
        boolean isImage = name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".gif") || name.endsWith(".bmp");

        if (isImage) {
            Stage stage = new Stage();
            stage.setTitle(file.getName());
            ImageView imageView = new ImageView(new Image(file.toURI().toString()));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(700);
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setStyle("-fx-background-color: #F8F7FC;");
            Scene scene = new Scene(scrollPane, 750, 600);
            stage.setScene(scene);
            stage.show();
            return;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "\"\"", file.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Cannot Open File",
                    "Could not open this document.\n\nFile location:\n" + file.getAbsolutePath());
        }
    }

    private String formatDocType(String type) {
        if (type == null) return "Document";
        return switch (type) {
            case "ID_COPY" -> "ID Copy";
            case "EDUCATION_CERTIFICATE" -> "Educational Certificate";
            case "OTHER_CERTIFICATE" -> "Other Certificate";
            default -> type;
        };
    }
}