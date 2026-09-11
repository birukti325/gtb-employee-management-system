package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.Payroll;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollRepository;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class PayrollController {

    @FXML private TableView<Payroll> payrollTable;
    @FXML private TableColumn<Payroll, Integer> idColumn;
    @FXML private TableColumn<Payroll, String> nameColumn;
    @FXML private TableColumn<Payroll, Double> baseSalaryColumn;
    @FXML private TableColumn<Payroll, Double> allowancesColumn;
    @FXML private TableColumn<Payroll, Double> transportAllowanceColumn;
    @FXML private TableColumn<Payroll, Double> taxColumn;
    @FXML private TableColumn<Payroll, Double> deductionsColumn;
    @FXML private TableColumn<Payroll, Double> pensionColumn;
    @FXML private TableColumn<Payroll, Double> employerPensionColumn;
    @FXML private TableColumn<Payroll, Double> netPayoutColumn;
    @FXML private Label grossPayrollLabel;
    @FXML private Label totalNetDistributionLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<YearMonth> monthSelector;

    @Autowired private PayrollRepository payrollRepository;
    @Autowired private EmployeeRepository employeeRepository;

    private final ObservableList<Payroll> payrollList = FXCollections.observableArrayList();
    private final Map<Long, String> employeeNames = new HashMap<>();
    private FilteredList<Payroll> filteredPayrollList;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupMonthSelector();
        setupSearchFilter();
        fetchEmployeesAndLoadPayroll();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(payrollTable.getItems().indexOf(data.getValue()) + 1).asObject());
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(employeeNames.getOrDefault(data.getValue().getEmployeeId(), "Unknown Employee")));

        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        allowancesColumn.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        transportAllowanceColumn.setCellValueFactory(new PropertyValueFactory<>("transportAllowance"));
        taxColumn.setCellValueFactory(new PropertyValueFactory<>("tax"));
        deductionsColumn.setCellValueFactory(new PropertyValueFactory<>("deductions"));
        pensionColumn.setCellValueFactory(new PropertyValueFactory<>("pension"));
        employerPensionColumn.setCellValueFactory(new PropertyValueFactory<>("employerPension"));
        netPayoutColumn.setCellValueFactory(new PropertyValueFactory<>("netPayout"));

        payrollTable.setEditable(false);
    }

    private void setupMonthSelector() {
        ObservableList<YearMonth> allMonths = FXCollections.observableArrayList();
        int currentYear = YearMonth.now().getYear();

        for (int month = 1; month <= 12; month++) {
            allMonths.add(YearMonth.of(currentYear, month));
        }

        FilteredList<YearMonth> filteredMonths = new FilteredList<>(allMonths, p -> true);
        monthSelector.setItems(filteredMonths);

        StringConverter<YearMonth> converter = new StringConverter<>() {
            @Override
            public String toString(YearMonth object) {
                return object != null ? object.format(monthFormatter) : "";
            }

            @Override
            public YearMonth fromString(String string) {
                if (string == null || string.trim().isEmpty()) return null;
                for (YearMonth ym : allMonths) {
                    if (ym.format(monthFormatter).equalsIgnoreCase(string.trim())) {
                        return ym;
                    }
                }
                return monthSelector.getValue();
            }
        };

        monthSelector.setConverter(converter);
        monthSelector.setValue(YearMonth.now());
        monthSelector.setOnAction(e -> loadPayrollDataForSelectedMonth());
    }

    private void fetchEmployeesAndLoadPayroll() {
        List<Employee> employees = employeeRepository.findAll();

        employeeNames.clear();
        for (Employee e : employees) {
            employeeNames.put(e.getId(), e.getFullName());
        }

        loadPayrollDataForSelectedMonth();
    }

    private void loadPayrollDataForSelectedMonth() {
        YearMonth selected = monthSelector.getValue();
        if (selected == null) return;

        // Matches 'yyyy-MM' and 'MMMM yyyy' database formats
        String dbPeriodFormat = selected.toString();
        String formattedMonthFormat = selected.format(monthFormatter);

        List<Payroll> monthPayroll = payrollRepository.findByPayPeriod(dbPeriodFormat);
        if (monthPayroll.isEmpty()) {
            monthPayroll = payrollRepository.findByPayPeriod(formattedMonthFormat);
        }

        List<Payroll> activePayroll = monthPayroll.stream()
                .filter(p -> employeeNames.containsKey(p.getEmployeeId()))
                .sorted(Comparator.comparingLong(Payroll::getEmployeeId))
                .toList();

        payrollList.setAll(activePayroll);
        updateSummaryLabels();
    }

    private void setupSearchFilter() {
        filteredPayrollList = new FilteredList<>(payrollList, p -> true);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();
                filteredPayrollList.setPredicate(payroll -> {
                    if (filter.isEmpty()) return true;
                    String name = employeeNames.getOrDefault(payroll.getEmployeeId(), "");
                    return name.toLowerCase().contains(filter) || matchesAmount(payroll.getNetPayout(), filter);
                });
                updateSummaryLabels();
            });
        }

        SortedList<Payroll> sortedList = new SortedList<>(filteredPayrollList);
        sortedList.comparatorProperty().bind(payrollTable.comparatorProperty());
        payrollTable.setItems(sortedList);
    }

    private boolean matchesAmount(double amount, String filter) {
        return String.valueOf(amount).contains(filter);
    }

    private void updateSummaryLabels() {
        double totalGross = 0.0;
        double totalNet = 0.0;

        for (Payroll p : filteredPayrollList) {
            totalGross += p.getBaseSalary() + p.getAllowances();
            totalNet += p.getNetPayout();
        }

        if (grossPayrollLabel != null) {
            grossPayrollLabel.setText(String.format("ETB %,.2f", totalGross));
        }
        if (totalNetDistributionLabel != null) {
            totalNetDistributionLabel.setText(String.format("ETB %,.2f", totalNet));
        }
    }
}