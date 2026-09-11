package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.Payroll;
import com.sms.gtbemployeemanagementsystem.Entity.PayrollCustomColumn;
import com.sms.gtbemployeemanagementsystem.Entity.PayrollCustomValue;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollCustomColumnRepository;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollCustomValueRepository;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.sms.gtbemployeemanagementsystem.Entity.PayrollColumnPreference;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollColumnPreferenceRepository;
import java.util.LinkedHashMap;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmployeePayrollController {

    @FXML private TableView<Payroll> payrollTable;
    @FXML private TableColumn<Payroll, Integer> idColumn;
    @FXML private TableColumn<Payroll, String> nameColumn;
    @FXML private TableColumn<Payroll, Double> baseSalaryColumn;
    @FXML private TableColumn<Payroll, Double> allowancesColumn;
    @FXML private TableColumn<Payroll, Double> transportAllowanceColumn;
    @FXML private TableColumn<Payroll, Double> taxColumn;
    @FXML private TableColumn<Payroll, Double> deductionsColumn;
    @FXML private TableColumn<Payroll, Double> pensionColumn;
    @FXML private TableColumn<Payroll, Double> netPayoutColumn;
    @FXML private TableColumn<Payroll, Void> actionColumn;
    @FXML private Button generatePayrollListBtn;
    @FXML private Button sendReportBtn;
    @FXML private Label currentPeriodLabel;
    @FXML private Button exportPayrollBtn;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollCustomColumnRepository customColumnRepository;

    @Autowired
    private PayrollCustomValueRepository customValueRepository;

    private final ObservableList<Payroll> payrollData = FXCollections.observableArrayList();

    // Custom column definitions, and payrollId -> (columnId -> value) cache
    private List<PayrollCustomColumn> customColumns = new ArrayList<>();
    private final Map<Integer, Map<Long, Double>> customValueCache = new HashMap<>();
    private final List<TableColumn<Payroll, ?>> dynamicColumns = new ArrayList<>();

    @Autowired
    private PayrollColumnPreferenceRepository columnPreferenceRepository;

    // Ordered map of hideable fixed columns: key -> the actual TableColumn.
    // id and action columns are intentionally excluded — they're structural, not data.
    private final Map<String, TableColumn<Payroll, ?>> fixedColumnMap = new LinkedHashMap<>();

    // key -> display label, used in the Manage Columns dialog
    private final Map<String, String> fixedColumnLabels = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        applyColumnVisibility();
        currentPeriodLabel.setOnMouseClicked(e -> handleEditPeriod());
        loadCustomColumnsAndValues();
        buildCustomColumns();
        loadPayrollData();
    }

    /**
     * Opens a small dialog letting the admin change the pay period (month/year)
     * for every currently active (unsubmitted) payroll row at once.
     * Triggered by clicking the "Payroll List — <Month Year>" label.
     * Future periods (after the current real-world month) are blocked;
     * past periods are always allowed for backdating/corrections.
     */
    private void handleEditPeriod() {
        if (payrollData.isEmpty()) {
            showAlert("No Active Payroll", "There is no active payroll list to change the period for.");
            return;
        }

        String currentPeriod = payrollData.get(0).getPayPeriod();
        java.time.YearMonth currentYm;
        try {
            currentYm = java.time.YearMonth.parse(currentPeriod);
        } catch (Exception e) {
            currentYm = java.time.YearMonth.now();
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Payroll Period");

        Label header = new Label("Change the pay period for this active list");
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"));
        monthBox.setValue(currentYm.getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH));

        Spinner<Integer> yearSpinner = new Spinner<>(2000, 2100, currentYm.getYear());
        yearSpinner.setEditable(true);

        HBox pickerRow = new HBox(10, monthBox, yearSpinner);
        pickerRow.setStyle("-fx-alignment: center-left;");

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            int monthIndex = java.time.Month.valueOf(monthBox.getValue().toUpperCase()).getValue();
            java.time.YearMonth newYm = java.time.YearMonth.of(yearSpinner.getValue(), monthIndex);

            if (newYm.isAfter(java.time.YearMonth.now())) {
                showAlert("Invalid Period", "The payroll period cannot be set to a future month.");
                return;
            }

            String newPeriod = newYm.toString();

            for (Payroll record : payrollData) {
                record.setPayPeriod(newPeriod);
                payrollRepository.save(record);
            }

            updateCurrentPeriodLabel(payrollData, newPeriod);
            dialog.close();
        });

        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttonRow = new HBox(10, saveBtn, cancelBtn);
        buttonRow.setStyle("-fx-alignment: center-right;");

        VBox layout = new VBox(15, header, pickerRow, buttonRow);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #F8F7FC;");

        dialog.setScene(new Scene(layout, 320, 180));
        dialog.showAndWait();
    }

    private void applyColumnVisibility() {
        for (Map.Entry<String, TableColumn<Payroll, ?>> entry : fixedColumnMap.entrySet()) {
            boolean visible = columnPreferenceRepository.findByColumnKey(entry.getKey())
                    .map(PayrollColumnPreference::isVisible)
                    .orElse(true);
            entry.getValue().setVisible(visible);
        }
    }

    private void setColumnVisible(String columnKey, boolean visible) {
        TableColumn<Payroll, ?> col = fixedColumnMap.get(columnKey);
        if (col != null) {
            col.setVisible(visible);
        }
        PayrollColumnPreference pref = columnPreferenceRepository.findByColumnKey(columnKey)
                .orElseGet(() -> {
                    PayrollColumnPreference p = new PayrollColumnPreference();
                    p.setColumnKey(columnKey);
                    return p;
                });
        pref.setVisible(visible);
        columnPreferenceRepository.save(pref);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        payrollTable.getItems().indexOf(data.getValue()) + 1).asObject());

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        allowancesColumn.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        transportAllowanceColumn.setCellValueFactory(new PropertyValueFactory<>("transportAllowance"));
        taxColumn.setCellValueFactory(new PropertyValueFactory<>("tax"));
        deductionsColumn.setCellValueFactory(new PropertyValueFactory<>("deductions"));
        pensionColumn.setCellValueFactory(new PropertyValueFactory<>("pension"));
        netPayoutColumn.setCellValueFactory(new PropertyValueFactory<>("netPayout"));

        fixedColumnMap.put("allowances", allowancesColumn);
        fixedColumnMap.put("transportAllowance", transportAllowanceColumn);
        fixedColumnMap.put("tax", taxColumn);
        fixedColumnMap.put("deductions", deductionsColumn);
        fixedColumnMap.put("pension", pensionColumn);
        fixedColumnMap.put("netPayout", netPayoutColumn);

        fixedColumnLabels.put("allowances", "Allowances");
        fixedColumnLabels.put("transportAllowance", "Transport Allowance");
        fixedColumnLabels.put("tax", "Tax");
        fixedColumnLabels.put("deductions", "Deductions");
        fixedColumnLabels.put("pension", "Pension");
        fixedColumnLabels.put("netPayout", "Net Payout");
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button menuBtn = new Button("⋮");

            {
                menuBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand; -fx-text-fill: #6d28d9;");

                ContextMenu contextMenu = new ContextMenu();
                MenuItem editItem = new MenuItem("Edit Payroll");
                MenuItem deleteItem = new MenuItem("Delete Record");

                editItem.setOnAction(e -> openEditDialog(getTableView().getItems().get(getIndex())));
                deleteItem.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));

                contextMenu.getItems().addAll(editItem, deleteItem);
                menuBtn.setOnAction(e -> contextMenu.show(menuBtn, javafx.geometry.Side.BOTTOM, 0, 0));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : menuBtn);
            }
        });
    }

    /** Reloads the list of custom column definitions and every saved custom value, into memory. */
    private void loadCustomColumnsAndValues() {
        customColumns = customColumnRepository.findAll();

        customValueCache.clear();
        for (PayrollCustomValue v : customValueRepository.findAll()) {
            customValueCache
                    .computeIfAbsent(v.getPayrollId(), k -> new HashMap<>())
                    .put(v.getColumnId(), v.getValue());
        }
    }

    /** (Re)builds the dynamic TableColumns for every custom column, inserted just before the Action column. */
    private void buildCustomColumns() {
        payrollTable.getColumns().removeAll(dynamicColumns);
        dynamicColumns.clear();

        int insertIndex = payrollTable.getColumns().indexOf(actionColumn);
        if (insertIndex < 0) insertIndex = payrollTable.getColumns().size();

        for (PayrollCustomColumn col : customColumns) {
            TableColumn<Payroll, String> tableColumn = new TableColumn<>(col.getColumnName());
            tableColumn.setPrefWidth(100);
            tableColumn.setCellValueFactory(data -> {
                Payroll p = data.getValue();
                Map<Long, Double> valuesForRow = customValueCache.getOrDefault(p.getId(), Map.of());
                Double val = valuesForRow.get(col.getId());
                return new javafx.beans.property.SimpleStringProperty(
                        val != null ? String.format("%.2f", val) : "0.00");
            });
            dynamicColumns.add(tableColumn);
            payrollTable.getColumns().add(insertIndex, tableColumn);
            insertIndex++;
        }
    }

    /**
     * Loads only the CURRENT, unsubmitted payroll list (submitted = false).
     * Once "Send Monthly Report to Admin" is clicked, active rows are flipped to
     * submitted = true and disappear from here until a new list is generated.
     */
    private void loadPayrollData() {
        List<Payroll> records = payrollRepository.findAllActivePayrollWithDetails();

        records.removeIf(r -> r.getEmployeeId() == null || r.isSubmitted());
        records.sort(Comparator.comparingLong(Payroll::getEmployeeId));

        String currentPeriod = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (Payroll record : records) {
            if (record.getPayPeriod() == null || record.getPayPeriod().isBlank()) {
                record.setPayPeriod(currentPeriod);
            }
            recalculate(record);
            payrollRepository.save(record);
        }

        payrollTable.getSortOrder().clear();
        payrollData.setAll(records);
        payrollTable.setItems(payrollData);

        updateCurrentPeriodLabel(records, currentPeriod);

        generatePayrollListBtn.setDisable(!payrollData.isEmpty());
        sendReportBtn.setDisable(payrollData.isEmpty());
    }

    /** Shows which pay period the active list on screen belongs to, e.g. "Payroll List — September 2026". */
    private void updateCurrentPeriodLabel(List<Payroll> records, String fallbackPeriod) {
        String period = records.isEmpty() ? fallbackPeriod : records.get(0).getPayPeriod();
        try {
            java.time.YearMonth ym = java.time.YearMonth.parse(period);
            String formatted = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            currentPeriodLabel.setText("Payroll List — " + formatted);
        } catch (Exception e) {
            currentPeriodLabel.setText("Payroll List — " + period);
        }
    }

    /**
     * Creates one fresh payroll row for every active employee who doesn't already
     * have an open (unsubmitted) row. Base salary is carried forward from each
     * employee's most recent payroll record, so it stays fixed unless the admin
     * changed it. Only usable once the current list has been submitted and cleared.
     */
    @FXML
    private void handleGenerateNewPayrollList() {
        if (!payrollData.isEmpty()) {
            showAlert("Current List Still Open",
                    "Please submit this month's payroll report before generating a new list.");
            return;
        }

        List<Employee> activeEmployees = employeeRepository.findAllWithDepartment().stream()
                .filter(Employee::isActive)
                .sorted(Comparator.comparingLong(Employee::getId))
                .collect(Collectors.toList());

        if (activeEmployees.isEmpty()) {
            showAlert("No Active Employees", "There are no active employees to generate payroll for.");
            return;
        }

        String currentPeriod = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (Employee emp : activeEmployees) {
            // Carry forward the employee's last known base salary (0.0 if this is their first ever entry)
            double carriedBaseSalary = payrollRepository.findByEmployeeId(emp.getId()).stream()
                    .max(Comparator.comparing(Payroll::getId))
                    .map(Payroll::getBaseSalary)
                    .orElse(0.0);

            Payroll newPayroll = new Payroll();
            newPayroll.setEmployeeId(emp.getId());
            newPayroll.setEmployeeName(emp.getFullName());
            newPayroll.setBaseSalary(carriedBaseSalary);
            newPayroll.setAllowances(0.0);
            newPayroll.setTransportAllowance(0.0);
            newPayroll.setTax(0.0);
            newPayroll.setDeductions(0.0);
            newPayroll.setPayPeriod(currentPeriod);
            newPayroll.setSubmitted(false);

            recalculate(newPayroll);
            payrollRepository.save(newPayroll);
        }

        loadPayrollData();
        showAlert("Payroll List Generated", "A new payroll list has been created for " + currentPeriod + ".");
    }

    /**
     * Exports the currently active (unsubmitted) payroll list to a CSV file,
     * including any custom columns currently defined. Opens a save dialog
     * so the admin picks the destination and filename.
     */
    @FXML
    private void handleExportPayroll() {
        if (payrollData.isEmpty()) {
            showAlert("Nothing to Export", "There is no active payroll list to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Payroll");
        String period = payrollData.get(0).getPayPeriod();
        fileChooser.setInitialFileName("Payroll_" + (period != null ? period : "export") + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(payrollTable.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }

        try (FileWriter writer = new FileWriter(file)) {
            // Header row
            StringBuilder header = new StringBuilder(
                    "ID,Name,Base Salary,Allowances,Transport,Tax,Deductions,Pension (7%),Net Payout");
            for (PayrollCustomColumn col : customColumns) {
                header.append(",").append(escapeCsv(col.getColumnName()));
            }
            writer.write(header + "\n");

            // Data rows
            int rowNum = 1;
            for (Payroll p : payrollData) {
                StringBuilder row = new StringBuilder();
                row.append(rowNum++).append(",");
                row.append(escapeCsv(p.getEmployeeName())).append(",");
                row.append(p.getBaseSalary()).append(",");
                row.append(p.getAllowances()).append(",");
                row.append(p.getTransportAllowance()).append(",");
                row.append(p.getTax()).append(",");
                row.append(p.getDeductions()).append(",");
                row.append(String.format("%.2f", p.getPension())).append(",");
                row.append(String.format("%.2f", p.getNetPayout()));

                Map<Long, Double> valuesForRow = customValueCache.getOrDefault(p.getId(), Map.of());
                for (PayrollCustomColumn col : customColumns) {
                    Double val = valuesForRow.get(col.getId());
                    row.append(",").append(val != null ? String.format("%.2f", val) : "0.00");
                }

                writer.write(row + "\n");
            }

            showAlert("Export Complete", "Payroll list exported successfully to:\n" + file.getAbsolutePath());

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Export Failed", "Could not save the file. Please try again.");
        }
    }

    /** Wraps a CSV field in quotes and escapes internal quotes, only when needed (commas, quotes, or newlines present). */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void openEditDialog(Payroll p) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Payroll");

        Label headerLabel = new Label("Update salary details for " + p.getEmployeeName());
        headerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 0, 0, 0));

        TextField baseField = new TextField(String.valueOf(p.getBaseSalary()));
        baseField.setEditable(false);
        baseField.setStyle("-fx-background-color: #f3e8ff;");

        TextField allowancesField = new TextField(String.valueOf(p.getAllowances()));
        TextField transportField = new TextField(String.valueOf(p.getTransportAllowance()));
        TextField taxField = new TextField(String.valueOf(p.getTax()));
        TextField deductionsField = new TextField(String.valueOf(p.getDeductions()));

        TextField pensionField = new TextField(String.format("%.2f", p.getBaseSalary() * 0.07));
        pensionField.setEditable(false);
        pensionField.setStyle("-fx-background-color: #f3e8ff;");

        TextField grossSalaryField = new TextField(String.format("%.2f",
                p.getBaseSalary() + p.getAllowances() + p.getTransportAllowance()));
        grossSalaryField.setEditable(false);
        grossSalaryField.setStyle("-fx-background-color: #f3e8ff;");

        TextField totalDeductionField = new TextField(String.format("%.2f",
                p.getDeductions() + p.getTax() + (p.getBaseSalary() * 0.07)));
        totalDeductionField.setEditable(false);
        totalDeductionField.setStyle("-fx-background-color: #f3e8ff;");

        Runnable recalcDerivedFields = () -> {
            try {
                double base = p.getBaseSalary();
                double allowances = Double.parseDouble(allowancesField.getText().trim());
                double transport = Double.parseDouble(transportField.getText().trim());
                double tax = Double.parseDouble(taxField.getText().trim());
                double deductions = Double.parseDouble(deductionsField.getText().trim());
                double pension = base * 0.07;

                pensionField.setText(String.format("%.2f", pension));
                grossSalaryField.setText(String.format("%.2f", base + allowances + transport));
                totalDeductionField.setText(String.format("%.2f", deductions + tax + pension));
            } catch (NumberFormatException ignored) {
                // leave fields showing their last valid values while the user is mid-typing
            }
        };

        allowancesField.textProperty().addListener((obs, oldVal, newVal) -> recalcDerivedFields.run());
        transportField.textProperty().addListener((obs, oldVal, newVal) -> recalcDerivedFields.run());
        taxField.textProperty().addListener((obs, oldVal, newVal) -> recalcDerivedFields.run());
        deductionsField.textProperty().addListener((obs, oldVal, newVal) -> recalcDerivedFields.run());

        int[] rowIndex = {0};
        grid.add(new Label("Base Salary:"), 0, rowIndex[0]); grid.add(baseField, 1, rowIndex[0]++);
        grid.add(new Label("Allowances:"), 0, rowIndex[0]); grid.add(allowancesField, 1, rowIndex[0]++);
        grid.add(new Label("Transport Allowance:"), 0, rowIndex[0]); grid.add(transportField, 1, rowIndex[0]++);
        grid.add(new Label("Tax:"), 0, rowIndex[0]); grid.add(taxField, 1, rowIndex[0]++);
        grid.add(new Label("Deductions:"), 0, rowIndex[0]); grid.add(deductionsField, 1, rowIndex[0]++);
        grid.add(new Label("Pension Employee (7%):"), 0, rowIndex[0]); grid.add(pensionField, 1, rowIndex[0]++);
        grid.add(new Label("Gross Salary:"), 0, rowIndex[0]); grid.add(grossSalaryField, 1, rowIndex[0]++);
        grid.add(new Label("Total Deduction:"), 0, rowIndex[0]); grid.add(totalDeductionField, 1, rowIndex[0]++);

        // ---- Custom columns: one editable field per existing custom column ----
        Map<Long, TextField> customFieldMap = new HashMap<>();
        Map<Long, Double> existingCustomValues = customValueCache.getOrDefault(p.getId(), Map.of());

        for (PayrollCustomColumn col : customColumns) {
            Double existing = existingCustomValues.get(col.getId());
            TextField field = new TextField(existing != null ? String.valueOf(existing) : "0.0");
            customFieldMap.put(col.getId(), field);
            grid.add(new Label(col.getColumnName() + ":"), 0, rowIndex[0]);
            grid.add(field, 1, rowIndex[0]++);
        }

        Button addColumnBtn = new Button("+ Add Column");
        addColumnBtn.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #6d28d9; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6px; -fx-cursor: hand;");
        grid.add(addColumnBtn, 0, rowIndex[0], 2, 1);
        rowIndex[0]++;

        addColumnBtn.setOnAction(e -> {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Add Column");
            nameDialog.setHeaderText(null);
            nameDialog.setContentText("New column name:");
            nameDialog.showAndWait().ifPresent(name -> {
                String trimmed = name.trim();
                if (trimmed.isEmpty()) return;

                PayrollCustomColumn newCol = new PayrollCustomColumn();
                newCol.setColumnName(trimmed);
                newCol = customColumnRepository.save(newCol);
                customColumns.add(newCol);

                TextField field = new TextField("0.0");
                customFieldMap.put(newCol.getId(), field);

                int newRow = rowIndex[0] - 1;
                grid.getChildren().remove(addColumnBtn);
                grid.add(new Label(newCol.getColumnName() + ":"), 0, newRow);
                grid.add(field, 1, newRow);
                grid.add(addColumnBtn, 0, rowIndex[0], 2, 1);
                rowIndex[0]++;

                buildCustomColumns();
                payrollTable.refresh();
            });
        });

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            try {
                p.setAllowances(Double.parseDouble(allowancesField.getText().trim()));
                p.setTransportAllowance(Double.parseDouble(transportField.getText().trim()));
                p.setTax(Double.parseDouble(taxField.getText().trim()));
                p.setDeductions(Double.parseDouble(deductionsField.getText().trim()));

                recalculate(p);
                payrollRepository.save(p);

                for (Map.Entry<Long, TextField> entry : customFieldMap.entrySet()) {
                    double val;
                    try {
                        val = Double.parseDouble(entry.getValue().getText().trim());
                    } catch (NumberFormatException ex) {
                        val = 0.0;
                    }
                    PayrollCustomValue cv = customValueRepository
                            .findByPayrollIdAndColumnId(p.getId(), entry.getKey())
                            .orElseGet(PayrollCustomValue::new);
                    cv.setPayrollId(p.getId());
                    cv.setColumnId(entry.getKey());
                    cv.setValue(val);
                    customValueRepository.save(cv);
                }

                loadCustomColumnsAndValues();
                buildCustomColumns();
                payrollTable.refresh();
                dialog.close();
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numeric amounts.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttonRow = new HBox(10, saveBtn, cancelBtn);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox layout = new VBox(15, headerLabel, scrollPane, buttonRow);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #F8F7FC;");
        dialog.setScene(new Scene(layout, 420, 520));
        dialog.showAndWait();
    }

    private void confirmAndDelete(Payroll p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete payroll entry for " + p.getEmployeeName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                payrollRepository.delete(p);
                payrollData.remove(p);
                generatePayrollListBtn.setDisable(!payrollData.isEmpty());
                sendReportBtn.setDisable(payrollData.isEmpty());
            }
        });
    }

    private void recalculate(Payroll p) {
        double base = p.getBaseSalary();
        double empPension = base * 0.07;
        double emprPension = base * 0.11;

        p.setPension(empPension);
        p.setEmployerPension(emprPension);

        double net = (base + p.getAllowances() + p.getTransportAllowance()) - (empPension + p.getTax() + p.getDeductions());
        p.setNetPayout(Math.max(net, 0.0));
    }

    @FXML
    private void handleManageColumns() {
        List<PayrollCustomColumn> currentColumns = customColumnRepository.findAll();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Manage Columns");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #F8F7FC;");

        // ---- Fixed columns: show/hide checkboxes ----
        Label fixedHeader = new Label("Fixed Columns");
        fixedHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        VBox fixedBox = new VBox(8);
        for (Map.Entry<String, TableColumn<Payroll, ?>> entry : fixedColumnMap.entrySet()) {
            String key = entry.getKey();
            TableColumn<Payroll, ?> col = entry.getValue();

            CheckBox cb = new CheckBox(fixedColumnLabels.get(key));
            cb.setSelected(col.isVisible());
            cb.setStyle("-fx-text-fill: #4c1d95;");
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> setColumnVisible(key, newVal));

            HBox row = new HBox(cb);
            row.setStyle("-fx-background-color: #f3e8ff; -fx-padding: 8px; -fx-background-radius: 6px;");
            fixedBox.getChildren().add(row);
        }

        // ---- Custom columns: existing delete list ----
        Label customHeader = new Label("Custom Columns");
        customHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        VBox customBox = new VBox(10);
        if (currentColumns.isEmpty()) {
            Label empty = new Label("No custom columns yet.");
            empty.setStyle("-fx-text-fill: #6b7280;");
            customBox.getChildren().add(empty);
        } else {
            for (PayrollCustomColumn col : currentColumns) {
                HBox row = new HBox(10);
                row.setStyle("-fx-background-color: #f3e8ff; -fx-padding: 10px; -fx-background-radius: 6px;");
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label nameLabel = new Label(col.getColumnName());
                nameLabel.setStyle("-fx-text-fill: #4c1d95; -fx-font-weight: bold;");
                HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 4px; -fx-cursor: hand;");

                deleteBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete the \"" + col.getColumnName() + "\" column?\n\n"
                                    + "This removes it from every employee's payroll and cannot be undone.",
                            ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            customColumnRepository.delete(col);
                            customColumns.removeIf(c -> c.getId().equals(col.getId()));
                            customBox.getChildren().remove(row);
                            buildCustomColumns();
                            payrollTable.refresh();
                        }
                    });
                });

                row.getChildren().addAll(nameLabel, deleteBtn);
                customBox.getChildren().add(row);
            }
        }

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 8 20; -fx-background-radius: 6px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        VBox content = new VBox(15, fixedHeader, fixedBox, customHeader, customBox);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        layout.getChildren().addAll(scrollPane, closeBtn);
        dialog.setScene(new Scene(layout, 380, 500));
        dialog.showAndWait();
    }

    /**
     * Submits the current month's payroll to the admin (creates a historical
     * snapshot row, submitted = true, reportMonth set) and closes out every
     * active row on this screen by flipping it to submitted = true as well.
     * Once this runs, the screen empties and "Generate New Payroll List"
     * becomes available again.
     */
    @FXML
    public void handleSendReport() {
        if (payrollData.isEmpty()) {
            showAlert("Nothing to Submit", "There is no active payroll list to submit.");
            return;
        }

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        for (Payroll activeRecord : new ArrayList<>(payrollData)) {
            Payroll snapshot = new Payroll();
            // ...copies all fields...
            snapshot.setReportMonth(currentMonth);
            snapshot.setSubmitted(true);
            payrollRepository.save(snapshot);              // ← new row #1

            activeRecord.setSubmitted(true);
            payrollRepository.save(activeRecord);           // ← same data, row #2, but reportMonth stays null
        }

        showAlert("Report Sent", "Monthly payroll report for " + currentMonth + " sent to admin.\n\n"
                + "Click \"Generate New Payroll List\" to start next month's payroll.");

        loadPayrollData();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}