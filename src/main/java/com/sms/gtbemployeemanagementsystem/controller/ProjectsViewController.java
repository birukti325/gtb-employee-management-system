package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.Projects;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.ProjectRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.sms.gtbemployeemanagementsystem.Entity.ProjectReport;
import com.sms.gtbemployeemanagementsystem.Repository.ProjectReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProjectsViewController {

    @FXML private TableView<Projects> projectsTable;
    @FXML private TableColumn<Projects, String> idColumn;
    @FXML private TableColumn<Projects, String> titleColumn;
    @FXML private TableColumn<Projects, String> departmentColumn;
    @FXML private TableColumn<Projects, String> leadColumn;
    @FXML private TableColumn<Projects, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button initializeProjectBtn;

    @Autowired private ProjectRepository projectRepository;
    @Autowired private DepartmentsRepository departmentsRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ProjectReportRepository projectReportRepository;

    private final ObservableList<Projects> projectMasterData = FXCollections.observableArrayList();
    private Popup detailsPopup;

    private static final String STATUS_COMPLETED = "COMPLETED";

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> {
            int rowIndex = projectsTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(rowIndex));
        });
        titleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        departmentColumn.setCellValueFactory(cellData -> {
            Departments dept = cellData.getValue().getDepartment();
            return new SimpleStringProperty(dept != null ? dept.getName() : "");
        });
        leadColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getManager()));
        setupActionsColumn();
        setupRowFactory();

        refreshTable();

        if (searchField != null) {
            searchField.setOnKeyReleased(e -> applySearch());
        }
    }

    private void refreshTable() {
        List<Projects> projects = projectRepository.findAllWithDepartment();
        projects.sort(java.util.Comparator.comparing(Projects::getId));

        projectMasterData.setAll(projects);
        projectsTable.setItems(projectMasterData);
    }

    private void applySearch() {
        String text = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        if (text.isEmpty()) {
            projectsTable.setItems(projectMasterData);
            return;
        }
        List<Projects> filtered = projectMasterData.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(text))
                .collect(Collectors.toList());
        projectsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    /**
     * Row styling: completed projects get a muted look, and a selected row
     * gets a light-grey highlight instead of the default blue selection.
     * Clicking anywhere on a row (other than the actions menu button, which
     * consumes its own click) opens a small popup with the project's details.
     */
    private void setupRowFactory() {
        projectsTable.setRowFactory(tv -> {
            TableRow<Projects> row = new TableRow<>() {
                @Override
                protected void updateItem(Projects project, boolean empty) {
                    super.updateItem(project, empty);
                    updateRowStyle(this, project, empty);
                }
            };

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    showProjectDetailsPopup(row.getItem());
                }
            });

            return row;
        });
    }

    private void updateRowStyle(TableRow<Projects> row, Projects project, boolean empty) {
        if (empty || project == null) {
            row.setStyle("");
            return;
        }

        boolean isCompleted = STATUS_COMPLETED.equalsIgnoreCase(project.getStatus());
        row.setStyle(isCompleted ? "-fx-background-color: #d1d5db;" : "");
    }

    private void showProjectDetailsPopup(Projects project) {
        Stage stage = new Stage();
        stage.setTitle(project.getName() + " - Project Details");

        VBox root = new VBox(20.0);
        root.setStyle("-fx-background-color: #F8F7FC;");
        root.setPadding(new Insets(30));

        VBox header = new VBox(5.0);
        Label titleLabel = new Label(project.getName() != null ? project.getName() : "N/A");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");
        Label subtitleLabel = new Label("Project Information");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7c3aed;");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 10px; "
                + "-fx-border-color: #d8b4fe; -fx-border-radius: 10px;");

        grid.add(buildFieldBox("Assigned Department", project.getDepartmentName()), 0, 0);
        grid.add(buildFieldBox("Project Lead", project.getManager()), 1, 0);
        grid.add(buildFieldBox("Start Date", project.getStartDate() != null ? project.getStartDate().toString() : "Not set"), 0, 1);
        grid.add(buildFieldBox("End Date", project.getEndDate() != null ? project.getEndDate().toString() : "Not set"), 1, 1);
        grid.add(buildFieldBox("Status", project.getStatus() != null ? project.getStatus() : "Not set"), 0, 2);

        // ---- Submitted Reports section ----
        Label reportsHeader = new Label("Submitted Reports");
        reportsHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        VBox reportsBox = buildReportsBox(project);

        ScrollPane reportsScroll = new ScrollPane(reportsBox);
        reportsScroll.setFitToWidth(true);
        reportsScroll.setPrefHeight(220);
        reportsScroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(header, grid, reportsHeader, reportsScroll);

        Scene scene = new Scene(root, 620, 720);
        stage.setScene(scene);
        stage.show();
    }

    /** Builds the scrollable list of reports submitted for a project, shown inside the details popup. */
    private VBox buildReportsBox(Projects project) {
        VBox box = new VBox(10);

        List<ProjectReport> reports = projectReportRepository.findByProjectIdOrderBySubmittedDateDesc(project.getId());

        if (reports.isEmpty()) {
            Label empty = new Label("No reports submitted yet.");
            empty.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 12px;");
            box.getChildren().add(empty);
            return box;
        }

        for (ProjectReport r : reports) {
            VBox card = new VBox(4);
            card.setStyle("-fx-background-color: white; -fx-padding: 12px; -fx-background-radius: 8px; "
                    + "-fx-border-color: #d8b4fe; -fx-border-radius: 8px;");

            Label meta = new Label(r.getEmployeeName() + " — " + r.getDepartmentName() + " — " + r.getSubmittedDate());
            meta.setStyle("-fx-font-weight: bold; -fx-text-fill: #6d28d9; -fx-font-size: 12px;");

            Label text = new Label(r.getReportText());
            text.setWrapText(true);
            text.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");

            card.getChildren().addAll(meta, text);
            box.getChildren().add(card);
        }

        return box;
    }

    private VBox buildFieldBox(String labelText, String value) {
        VBox box = new VBox(5.0);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7c3aed;");

        Label valueField = new Label(value != null && !value.isBlank() ? value : "Not provided");
        valueField.setPrefWidth(260);
        valueField.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #4c1d95; "
                + "-fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-font-size: 13px;");

        box.getChildren().addAll(label, valueField);
        return box;
    }

    @FXML
    private void handleInitializeProject() {
        Dialog<Projects> dialog = new Dialog<>();
        dialog.setTitle("Initialize New Project");
        dialog.setHeaderText("Create a new project");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Project Title");
        titleField.setPrefWidth(280);

        ComboBox<Departments> departmentBox = new ComboBox<>(
                FXCollections.observableArrayList(departmentsRepository.findAll()));
        departmentBox.setPromptText("Select Department");
        departmentBox.setPrefWidth(280);
        departmentBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Departments dept, boolean empty) {
                super.updateItem(dept, empty);
                setText(empty || dept == null ? null : dept.getName());
            }
        });
        departmentBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Departments dept, boolean empty) {
                super.updateItem(dept, empty);
                setText(empty || dept == null ? null : dept.getName());
            }
        });

        ComboBox<Employee> leadBox = new ComboBox<>();
        leadBox.setPromptText("Select Project Lead");
        leadBox.setPrefWidth(280);
        leadBox.setDisable(true);
        leadBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? null : emp.getFullName());
            }
        });
        leadBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? null : emp.getFullName());
            }
        });

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setPrefWidth(280);

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setPrefWidth(280);

        departmentBox.setOnAction(e -> {
            Departments selectedDept = departmentBox.getValue();
            if (selectedDept == null) {
                leadBox.setDisable(true);
                leadBox.setItems(FXCollections.observableArrayList());
                return;
            }
            List<Employee> deptEmployees = employeeRepository.findAll().stream()
                    .filter(emp -> emp.getDepartment() != null
                            && emp.getDepartment().getId().equals(selectedDept.getId()))
                    .collect(Collectors.toList());
            leadBox.setItems(FXCollections.observableArrayList(deptEmployees));
            leadBox.setDisable(deptEmployees.isEmpty());
        });

        grid.add(new Label("Project Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Assigned Department:"), 0, 1);
        grid.add(departmentBox, 1, 1);
        grid.add(new Label("Project Lead:"), 0, 2);
        grid.add(leadBox, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endDatePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                Projects project = new Projects();
                project.setName(titleField.getText());
                project.setDepartment(departmentBox.getValue());
                Employee lead = leadBox.getValue();
                project.setManager(lead != null ? lead.getFullName() : null);
                project.setStartDate(startDatePicker.getValue());
                project.setEndDate(endDatePicker.getValue());
                project.setStatus("PLANNED");
                return project;
            }
            return null;
        });

        Optional<Projects> result = dialog.showAndWait();

        result.ifPresent(project -> {
            if (project.getName() == null || project.getName().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Project title is required.");
                return;
            }
            try {
                projectRepository.save(project);
                showAlert(Alert.AlertType.INFORMATION, "Project Created", "Project saved successfully.");
                refreshTable();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Something went wrong while saving the project.");
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button menuBtn = new Button("⋮");
            private final ContextMenu contextMenu = new ContextMenu();
            private final MenuItem editItem = new MenuItem("Edit");
            private final MenuItem markDoneItem = new MenuItem("Mark as Done");
            private final MenuItem deleteItem = new MenuItem("Delete");

            {
                menuBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #374151; -fx-cursor: hand;");

                editItem.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
                markDoneItem.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                deleteItem.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");

                contextMenu.getItems().addAll(editItem, markDoneItem, deleteItem);

                menuBtn.setOnAction(e -> contextMenu.show(menuBtn, Side.BOTTOM, 0, 0));

                editItem.setOnAction(e -> {
                    Projects project = getTableView().getItems().get(getIndex());
                    handleEditProject(project);
                });

                markDoneItem.setOnAction(e -> {
                    Projects project = getTableView().getItems().get(getIndex());
                    handleMarkAsDone(project);
                });

                deleteItem.setOnAction(e -> {
                    Projects project = getTableView().getItems().get(getIndex());
                    handleDeleteProject(project);
                });
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Projects project = getTableView().getItems().get(getIndex());
                    boolean isCompleted = STATUS_COMPLETED.equalsIgnoreCase(project.getStatus());
                    markDoneItem.setText(isCompleted ? "Mark as In Progress" : "Mark as Done");
                    setGraphic(menuBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void handleMarkAsDone(Projects project) {
        boolean isCompleted = STATUS_COMPLETED.equalsIgnoreCase(project.getStatus());

        if (isCompleted) {
            // Reverting back to in-progress — clear the end date since it's no longer "done"
            project.setStatus("IN_PROGRESS");
            project.setEndDate(null);
        } else {
            // Marking as done now — stamp today's date as the actual completion date
            project.setStatus(STATUS_COMPLETED);
            project.setEndDate(java.time.LocalDate.now());
        }

        try {
            projectRepository.save(project);
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update the project's status.");
        }
    }

    private void handleEditProject(Projects project) {
        Dialog<Projects> dialog = new Dialog<>();
        dialog.setTitle("Edit Project");
        dialog.setHeaderText("Update project details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField titleField = new TextField(project.getName());
        titleField.setPrefWidth(280);

        ComboBox<Departments> departmentBox = new ComboBox<>(
                FXCollections.observableArrayList(departmentsRepository.findAll()));
        departmentBox.setValue(project.getDepartment());
        departmentBox.setPrefWidth(280);
        departmentBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Departments dept, boolean empty) {
                super.updateItem(dept, empty);
                setText(empty || dept == null ? null : dept.getName());
            }
        });
        departmentBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Departments dept, boolean empty) {
                super.updateItem(dept, empty);
                setText(empty || dept == null ? null : dept.getName());
            }
        });

        ComboBox<Employee> leadBox = new ComboBox<>();
        leadBox.setPrefWidth(280);
        leadBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? null : emp.getFullName());
            }
        });
        leadBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? null : emp.getFullName());
            }
        });

        Runnable loadLeadsForDept = () -> {
            Departments selectedDept = departmentBox.getValue();
            if (selectedDept == null) {
                leadBox.setItems(FXCollections.observableArrayList());
                return;
            }
            List<Employee> deptEmployees = employeeRepository.findAll().stream()
                    .filter(emp -> emp.getDepartment() != null
                            && emp.getDepartment().getId().equals(selectedDept.getId()))
                    .collect(Collectors.toList());
            leadBox.setItems(FXCollections.observableArrayList(deptEmployees));
            deptEmployees.stream()
                    .filter(emp -> emp.getFullName().equals(project.getManager()))
                    .findFirst()
                    .ifPresent(leadBox::setValue);
        };
        loadLeadsForDept.run();
        departmentBox.setOnAction(e -> loadLeadsForDept.run());

        DatePicker startDatePicker = new DatePicker(project.getStartDate());
        startDatePicker.setPrefWidth(280);

        DatePicker endDatePicker = new DatePicker(project.getEndDate());
        endDatePicker.setPrefWidth(280);

        grid.add(new Label("Project Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Assigned Department:"), 0, 1);
        grid.add(departmentBox, 1, 1);
        grid.add(new Label("Project Lead:"), 0, 2);
        grid.add(leadBox, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endDatePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                project.setName(titleField.getText());
                project.setDepartment(departmentBox.getValue());
                Employee lead = leadBox.getValue();
                project.setManager(lead != null ? lead.getFullName() : null);
                project.setStartDate(startDatePicker.getValue());
                project.setEndDate(endDatePicker.getValue());
                return project;
            }
            return null;
        });

        Optional<Projects> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            if (updated.getName() == null || updated.getName().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Project title is required.");
                return;
            }
            try {
                projectRepository.save(updated);
                showAlert(Alert.AlertType.INFORMATION, "Project Updated", "Project updated successfully.");
                refreshTable();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Something went wrong while updating the project.");
            }
        });
    }

    private void handleDeleteProject(Projects project) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Project");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete \"" + project.getName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                projectRepository.deleteById(project.getId());
                showAlert(Alert.AlertType.INFORMATION, "Project Deleted", "Project deleted successfully.");
                refreshTable();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Something went wrong while deleting the project.");
            }
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