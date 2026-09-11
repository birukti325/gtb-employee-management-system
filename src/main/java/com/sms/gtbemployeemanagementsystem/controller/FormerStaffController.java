package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.EmploymentHistory;
import com.sms.gtbemployeemanagementsystem.Entity.StaffDocument;
import com.sms.gtbemployeemanagementsystem.Service.EmployeeService;
import com.sms.gtbemployeemanagementsystem.Service.StaffDocumentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class FormerStaffController {

    @FXML private TableView<Employee> leftEmployeesTable;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> departmentColumn;
    @FXML private TableColumn<Employee, String> roleColumn;
    @FXML private TableColumn<Employee, String> leaveDateColumn;
    @FXML private TableColumn<Employee, Void> actionsColumn;
    @FXML private TextField searchField;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private StaffDocumentService staffDocumentService;

    private final ObservableList<Employee> masterData = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredData;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));
        departmentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDepartmentName()));
        roleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProfession()));

        if (leaveDateColumn != null) {
            leaveDateColumn.setCellValueFactory(cellData -> {
                LocalDate leaveDate = cellData.getValue().getLeaveDate();
                String display = leaveDate != null ? leaveDate.format(DATE_FORMAT) : "-";
                return new SimpleStringProperty(display);
            });
        }

        if (actionsColumn != null) {
            setupActionsColumn();
        }

        setupRowClick();
        refreshTable();

        if (searchField != null) {
            searchField.setOnKeyReleased(e -> applySearch());
        }
    }

    /**
     * Clicking anywhere on a row (other than the Actions "⋮" button) opens
     * a read-only popup showing that former employee's full hire-time details
     * and any documents uploaded while they were active. Restoring an employee
     * to active status is done from that popup, not from this row menu.
     */
    private void setupRowClick() {
        leftEmployeesTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && !(event.getTarget() instanceof Button)) {
                    openEmployeeDetailsDialog(row.getItem());
                }
            });
            return row;
        });
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
        dialog.setTitle("Edit Former Employee");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(employee.getFullName());
        TextField roleField = new TextField(employee.getProfession());

        DatePicker leaveDatePicker = new DatePicker(
                employee.getLeaveDate() != null ? employee.getLeaveDate() : LocalDate.now());

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Role:"), 0, 1); grid.add(roleField, 1, 1);
        grid.add(new Label("Leave Date:"), 0, 2); grid.add(leaveDatePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(type -> {
            if (type == saveButtonType) {
                employee.setFullName(nameField.getText());
                employee.setProfession(roleField.getText());
                employee.setLeaveDate(leaveDatePicker.getValue());
                return employee;
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            employeeService.save(updated);
            refreshTable();
        });
    }

    private void confirmAndDelete(Employee employee) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Permanently remove " + employee.getFullName() + " from records?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            employeeService.deleteById(employee.getId());
            refreshTable();
        }
    }

    public void refreshTable() {
        List<Employee> inactive = employeeService.findInactiveEmployees();

        // Most recently left employees first; anyone with no leave date sinks to the bottom
        inactive.sort((a, b) -> {
            if (a.getLeaveDate() == null && b.getLeaveDate() == null) return 0;
            if (a.getLeaveDate() == null) return 1;
            if (b.getLeaveDate() == null) return -1;
            return b.getLeaveDate().compareTo(a.getLeaveDate());
        });

        masterData.setAll(inactive);
        filteredData = new FilteredList<>(masterData, e -> true);
        leftEmployeesTable.setItems(filteredData);
    }

    private void applySearch() {
        String text = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        filteredData.setPredicate(e ->
                text.isEmpty() || (e.getFullName() != null && e.getFullName().toLowerCase().contains(text)));
    }

    // ==================== Former Employee Details & Documents Dialog ====================

    private void openEmployeeDetailsDialog(Employee employee) {
        Stage stage = new Stage();
        stage.setTitle(employee.getFullName() + " - Former Employee Details");

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #D6E2F4; -fx-padding: 25px;");

        VBox titleBox = new VBox(4);
        Label header = new Label(employee.getFullName());
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #14213D;");
        Label subHeader = new Label("Former Employee Information & Documents"
                + (employee.getLeaveDate() != null ? " — Left " + employee.getLeaveDate().format(DATE_FORMAT) : ""));
        subHeader.setStyle("-fx-font-size: 12px; -fx-text-fill: #1D3A8A;");
        titleBox.getChildren().addAll(header, subHeader);

        Button restoreBtn = new Button("Restore to Active");
        restoreBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 16px; -fx-cursor: hand;");
        restoreBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Restore " + employee.getFullName() + " to active employee status?\n\n"
                            + "They will reappear in the active Staff Directory and regain system access.");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                employee.setActive(true);
                employee.setLeaveDate(null);
                employeeService.save(employee);
                refreshTable();
                stage.close();
            }
        });

        HBox titleRow = new HBox(20, titleBox);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        titleRow.getChildren().add(restoreBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(12);
        detailsGrid.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-border-color: #D6DCE8; -fx-border-radius: 8px;");

        EmploymentHistory history = employee.getEmploymentHistory();

        detailsGrid.add(createDetailBox("Department", employee.getDepartmentName()), 0, 0);
        detailsGrid.add(createDetailBox("Role", employee.getProfession()), 1, 0);

        detailsGrid.add(createDetailBox("Guarantor Name",
                employee.getGuarantorName() != null ? employee.getGuarantorName() : "Not provided"), 0, 1);
        detailsGrid.add(createDetailBox("Guarantor Phone",
                employee.getGuarantorContact() != null ? employee.getGuarantorContact() : "Not provided"), 1, 1);

        detailsGrid.add(createDetailBox("Employment Agreement Contract",
                (history != null && history.getContractAgreement() != null) ? history.getContractAgreement() : "Not provided"), 0, 2);
        detailsGrid.add(createDetailBox("Birth Date",
                employee.getBirthYear() != null ? employee.getBirthYear().toString() : "Not provided"), 1, 2);

        detailsGrid.add(createDetailBox("Employment Conditions",
                (history != null && history.getEmploymentType() != null) ? history.getEmploymentType() : "Not provided"), 0, 3);
        detailsGrid.add(createDetailBox("Employment Date",
                (history != null && history.getJoiningDate() != null) ? history.getJoiningDate().toString() : "Not provided"), 1, 3);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        detailsGrid.getColumnConstraints().addAll(col1, col2);

        Label docsTitle = new Label("Uploaded Documents");
        docsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #14213D;");

        VBox docsBox = new VBox(8);
        List<StaffDocument> docs = staffDocumentService.getDocuments(employee.getId());

        if (docs.isEmpty()) {
            Label empty = new Label("No documents were uploaded for this employee.");
            empty.setStyle("-fx-text-fill: #1D3A8A; -fx-font-size: 12px;");
            docsBox.getChildren().add(empty);
        } else {
            for (StaffDocument doc : docs) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: white; -fx-background-radius: 6px; -fx-padding: 10px; -fx-border-color: #D6DCE8; -fx-border-radius: 6px;");

                VBox info = new VBox(2);
                Label typeLabel = new Label(formatDocType(doc.getDocumentType()));
                typeLabel.setStyle("-fx-text-fill: #14213D; -fx-font-weight: bold; -fx-font-size: 12px;");
                Label nameLabel = new Label(doc.getFileName());
                nameLabel.setStyle("-fx-text-fill: #1D3A8A; -fx-font-size: 11px;");
                info.getChildren().addAll(typeLabel, nameLabel);
                HBox.setHgrow(info, Priority.ALWAYS);

                Button viewBtn = new Button("View");
                viewBtn.setStyle("-fx-background-color: #14213D; -fx-text-fill: white; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
                viewBtn.setOnAction(e -> openDocumentFile(staffDocumentService.resolveFile(doc)));

                row.getChildren().addAll(info, viewBtn);
                docsBox.getChildren().add(row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(docsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(titleRow, detailsGrid, docsTitle, scrollPane);

        Scene scene = new Scene(root, 580, 600);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createDetailBox(String labelText, String valueText) {
        VBox box = new VBox(4);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #1D3A8A; -fx-font-weight: bold;");

        TextField valueField = new TextField(valueText != null ? valueText : "Not provided");
        valueField.setEditable(false);
        valueField.setStyle("-fx-background-color: #E8ECF5; -fx-text-fill: #14213D; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #D6DCE8; -fx-pref-height: 30px;");

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
            scrollPane.setStyle("-fx-background-color: #D6E2F4;");
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}