package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.*;
import com.sms.gtbemployeemanagementsystem.Repository.DepartmentsRepository;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.ProjectRepository;
import com.sms.gtbemployeemanagementsystem.Repository.ProjectReportRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import java.util.List;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmployeeProjectsController {

    @FXML private TableView<Projects> myProjectsTable;
    @FXML private TableColumn<Projects, String> idColumn;
    @FXML private TableColumn<Projects, String> titleColumn;
    @FXML private TableColumn<Projects, String> departmentColumn;
    @FXML private TableColumn<Projects, String> statusColumn;
    @FXML private TableColumn<Projects, Void> actionsColumn;

    @Autowired private ProjectRepository projectRepository;
    @Autowired private DepartmentsRepository departmentsRepository;
    @Autowired private ProjectReportRepository projectReportRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserSession userSession;

    private final ObservableList<Projects> myProjects = FXCollections.observableArrayList();
    private String currentEmployeeName;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> {
            int rowIndex = myProjectsTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(rowIndex));
        });
        titleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        departmentColumn.setCellValueFactory(cellData -> {
            Departments dept = cellData.getValue().getDepartment();
            return new SimpleStringProperty(dept != null ? dept.getName() : "");
        });
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        setupActionsColumn();
        loadMyProjects();
    }

    /** Filters the full project list down to only projects where this employee is the lead. */
    private void loadMyProjects() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        Employee me = employeeRepository.findById(employeeId).orElse(null);
        if (me == null) return;

        currentEmployeeName = me.getFullName();

        List<Projects> all = projectRepository.findAllWithDepartment();
        List<Projects> mine = all.stream()
                .filter(p -> currentEmployeeName.equalsIgnoreCase(p.getManager()))
                .collect(Collectors.toList());

        myProjects.setAll(mine);
        myProjectsTable.setItems(myProjects);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button menuBtn = new Button("⋮");
            private final ContextMenu contextMenu = new ContextMenu();
            private final MenuItem editItem = new MenuItem("Edit");
            private final MenuItem reportItem = new MenuItem("Write Report");

            {
                menuBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand; -fx-text-fill: #6d28d9;");
                contextMenu.getItems().addAll(editItem, reportItem);
                menuBtn.setOnAction(e -> contextMenu.show(menuBtn, javafx.geometry.Side.BOTTOM, 0, 0));

                editItem.setOnAction(e -> {
                    Projects project = getTableView().getItems().get(getIndex());
                    openEditDialog(project);
                });

                reportItem.setOnAction(e -> {
                    Projects project = getTableView().getItems().get(getIndex());
                    openReportDialog(project);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : menuBtn);
            }
        });
    }

    /** Lets the employee update their own project's title, department, dates, and status. */
    private void openEditDialog(Projects project) {
        Dialog<Projects> dialog = new Dialog<>();
        dialog.setTitle("Edit My Project");
        dialog.setHeaderText("Update details for " + project.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField titleField = new TextField(project.getName());
        titleField.setPrefWidth(260);

        ComboBox<Departments> departmentBox = new ComboBox<>(
                FXCollections.observableArrayList(departmentsRepository.findAll()));
        departmentBox.setValue(project.getDepartment());
        departmentBox.setPrefWidth(260);
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

        DatePicker startDatePicker = new DatePicker(project.getStartDate());
        startDatePicker.setPrefWidth(260);

        DatePicker endDatePicker = new DatePicker(project.getEndDate());
        endDatePicker.setPrefWidth(260);

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                "PLANNED", "IN_PROGRESS", "COMPLETED"));
        statusBox.setValue(project.getStatus());
        statusBox.setPrefWidth(260);

        grid.add(new Label("Project Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Assigned Department:"), 0, 1); grid.add(departmentBox, 1, 1);
        grid.add(new Label("Start Date:"), 0, 2); grid.add(startDatePicker, 1, 2);
        grid.add(new Label("End Date:"), 0, 3); grid.add(endDatePicker, 1, 3);
        grid.add(new Label("Status:"), 0, 4); grid.add(statusBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                project.setName(titleField.getText());
                project.setDepartment(departmentBox.getValue());
                project.setStartDate(startDatePicker.getValue());
                project.setEndDate(endDatePicker.getValue());
                project.setStatus(statusBox.getValue());
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
                showAlert(Alert.AlertType.INFORMATION, "Project Updated", "Your project details were updated.");
                loadMyProjects();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Something went wrong while saving.");
            }
        });
    }

    /** Lets the employee submit a written progress report for the admin to review. */
    private void openReportDialog(Projects project) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Reports — " + project.getName());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F8F7FC;");

        Label header = new Label("Progress reports for " + project.getName());
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        VBox myReportsBox = new VBox(10);
        refreshMyReports(project, myReportsBox, dialog);

        ScrollPane reportsScroll = new ScrollPane(myReportsBox);
        reportsScroll.setFitToWidth(true);
        reportsScroll.setPrefHeight(180);
        reportsScroll.setStyle("-fx-background-color: transparent;");

        Label newHeader = new Label("Write a new report");
        newHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        TextArea reportArea = new TextArea();
        reportArea.setPromptText("Describe progress, blockers, or updates for this project...");
        reportArea.setPrefRowCount(5);
        reportArea.setWrapText(true);

        Button submitBtn = new Button("Submit Report");
        submitBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");

        submitBtn.setOnAction(e -> {
            String text = reportArea.getText().trim();
            if (text.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Report", "Please write something before submitting.");
                return;
            }

            ProjectReport report = new ProjectReport();
            report.setProjectId(project.getId());
            report.setEmployeeName(currentEmployeeName);
            report.setDepartmentName(project.getDepartmentName());
            report.setReportText(text);
            report.setSubmittedDate(java.time.LocalDate.now());
            projectReportRepository.save(report);

            reportArea.clear();
            refreshMyReports(project, myReportsBox, dialog);
        });

        closeBtn.setOnAction(e -> dialog.close());

        HBox buttonRow = new HBox(10, submitBtn, closeBtn);

        root.getChildren().addAll(header, reportsScroll, newHeader, reportArea, buttonRow);

        dialog.setScene(new Scene(root, 460, 560));
        dialog.showAndWait();
    }

    /** Rebuilds the list of this employee's own reports for the given project, each with Edit/Delete controls. */
    private void refreshMyReports(Projects project, VBox container, Stage parentDialog) {
        container.getChildren().clear();

        List<ProjectReport> myReports = projectReportRepository
                .findByProjectIdOrderBySubmittedDateDesc(project.getId())
                .stream()
                .filter(r -> currentEmployeeName.equalsIgnoreCase(r.getEmployeeName()))
                .toList();

        if (myReports.isEmpty()) {
            Label empty = new Label("You haven't submitted any reports for this project yet.");
            empty.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 12px;");
            container.getChildren().add(empty);
            return;
        }

        for (ProjectReport report : myReports) {
            VBox card = new VBox(6);
            card.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-border-color: #d8b4fe; -fx-border-radius: 8px;");

            Label dateLabel = new Label(report.getSubmittedDate().toString());
            dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6d28d9; -fx-font-size: 11px;");

            Label textLabel = new Label(report.getReportText());
            textLabel.setWrapText(true);
            textLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #6d28d9; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-cursor: hand;");
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-cursor: hand;");

            editBtn.setOnAction(e -> openEditReportDialog(report, project, container, parentDialog));

            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete this report?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        projectReportRepository.delete(report);
                        refreshMyReports(project, container, parentDialog);
                    }
                });
            });

            HBox actionRow = new HBox(8, editBtn, deleteBtn);
            card.getChildren().addAll(dateLabel, textLabel, actionRow);
            container.getChildren().add(card);
        }
    }

    /** Small dialog for editing the text of an existing report the employee submitted. */
    private void openEditReportDialog(ProjectReport report, Projects project, VBox container, Stage parentDialog) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.setTitle("Edit Report");

        Label header = new Label("Edit your report");
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        TextArea editArea = new TextArea(report.getReportText());
        editArea.setPrefRowCount(6);
        editArea.setWrapText(true);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            String newText = editArea.getText().trim();
            if (newText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Report", "Report text cannot be empty.");
                return;
            }
            report.setReportText(newText);
            projectReportRepository.save(report);
            editStage.close();
            refreshMyReports(project, container, parentDialog);
        });

        cancelBtn.setOnAction(e -> editStage.close());

        HBox buttonRow = new HBox(10, saveBtn, cancelBtn);
        VBox layout = new VBox(15, header, editArea, buttonRow);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #F8F7FC;");

        editStage.setScene(new Scene(layout, 380, 280));
        editStage.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}