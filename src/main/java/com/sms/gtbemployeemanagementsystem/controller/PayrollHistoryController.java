package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Payroll;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PayrollHistoryController {

    @FXML private TextField yearSearchField;
    @FXML private VBox periodListBox;

    @Autowired
    private PayrollRepository payrollRepository;

    @FXML
    public void initialize() {
        loadAllPeriods();
    }

    private void loadAllPeriods() {
        renderPeriodList(payrollRepository.findDistinctSubmittedPayPeriods());
    }

    @FXML
    private void handleYearSearch() {
        String year = yearSearchField.getText().trim();
        if (year.isEmpty()) {
            loadAllPeriods();
            return;
        }
        List<String> periods = payrollRepository.findDistinctSubmittedPayPeriods()
                .stream()
                .filter(p -> p.startsWith(year))
                .toList();
        renderPeriodList(periods);
    }

    @FXML
    private void handleShowAll() {
        yearSearchField.clear();
        loadAllPeriods();
    }

    /** Renders one "file" row per pay period, each with a View button. */
    private void renderPeriodList(List<String> periods) {
        periodListBox.getChildren().clear();

        if (periods.isEmpty()) {
            Label empty = new Label("No payroll reports found.");
            empty.setStyle("-fx-text-fill: #6b7280;");
            periodListBox.getChildren().add(empty);
            return;
        }

        for (String period : periods) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #f3e8ff; -fx-padding: 14px; -fx-background-radius: 8px;");

            Label icon = new Label("📄");
            icon.setStyle("-fx-font-size: 18px;");

            Label nameLabel = new Label(formatPeriodLabel(period));
            nameLabel.setStyle("-fx-text-fill: #4c1d95; -fx-font-weight: bold; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button viewBtn = new Button("View");
            viewBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 6px; -fx-cursor: hand;");
            viewBtn.setOnAction(e -> openPeriodPopup(period));

            row.getChildren().addAll(icon, nameLabel, spacer, viewBtn);
            periodListBox.getChildren().add(row);
        }
    }

    /** Turns "2026-09" into "September 2026" for display. */
    private String formatPeriodLabel(String payPeriod) {
        try {
            YearMonth ym = YearMonth.parse(payPeriod);
            return ym.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        } catch (Exception e) {
            return payPeriod;
        }
    }

    /** Opens a popup showing every submitted payroll row for one specific month. */
    private void openPeriodPopup(String payPeriod) {
        List<Payroll> records = payrollRepository.findSubmittedByPayPeriod(payPeriod);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Payroll Report - " + formatPeriodLabel(payPeriod));

        Label header = new Label("Payroll Report — " + formatPeriodLabel(payPeriod));
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4c1d95;");

        TableView<Payroll> table = new TableView<>();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Payroll, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        TableColumn<Payroll, Double> baseCol = new TableColumn<>("Base Salary");
        baseCol.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));

        TableColumn<Payroll, Double> allowCol = new TableColumn<>("Allowances");
        allowCol.setCellValueFactory(new PropertyValueFactory<>("allowances"));

        TableColumn<Payroll, Double> transportCol = new TableColumn<>("Transport");
        transportCol.setCellValueFactory(new PropertyValueFactory<>("transportAllowance"));

        TableColumn<Payroll, Double> taxCol = new TableColumn<>("Tax");
        taxCol.setCellValueFactory(new PropertyValueFactory<>("tax"));

        TableColumn<Payroll, Double> deductCol = new TableColumn<>("Deductions");
        deductCol.setCellValueFactory(new PropertyValueFactory<>("deductions"));

        TableColumn<Payroll, Double> pensionCol = new TableColumn<>("Pension (7%)");
        pensionCol.setCellValueFactory(new PropertyValueFactory<>("pension"));

        TableColumn<Payroll, Double> netCol = new TableColumn<>("Net Payout");
        netCol.setCellValueFactory(new PropertyValueFactory<>("netPayout"));

        table.getColumns().addAll(nameCol, baseCol, allowCol, transportCol, taxCol, deductCol, pensionCol, netCol);
        table.getItems().setAll(records);

        Button exportBtn = new Button("Export to Excel");
        exportBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6px; -fx-cursor: hand;");
        exportBtn.setOnAction(e -> exportToExcel(records, payPeriod, dialog));

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 8 16; -fx-background-radius: 6px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttonRow = new HBox(10, exportBtn, closeBtn);

        VBox layout = new VBox(15, header, table, buttonRow);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #F8F7FC;");

        dialog.setScene(new Scene(layout, 800, 520));
        dialog.showAndWait();
    }

    /** Writes the given period's payroll records to an .xlsx file the user picks. */
    private void exportToExcel(List<Payroll> records, String payPeriod, Stage owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payroll Report");
        fileChooser.setInitialFileName("payroll_" + payPeriod + ".xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payroll " + payPeriod);

            String[] headers = {"Name", "Base Salary", "Allowances", "Transport",
                    "Tax", "Deductions", "Pension (7%)", "Net Payout"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Payroll p : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getEmployeeName());
                row.createCell(1).setCellValue(p.getBaseSalary());
                row.createCell(2).setCellValue(p.getAllowances());
                row.createCell(3).setCellValue(p.getTransportAllowance());
                row.createCell(4).setCellValue(p.getTax());
                row.createCell(5).setCellValue(p.getDeductions());
                row.createCell(6).setCellValue(p.getPension());
                row.createCell(7).setCellValue(p.getNetPayout());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export Complete");
            success.setHeaderText(null);
            success.setContentText("Payroll report exported to:\n" + file.getAbsolutePath());
            success.showAndWait();

        } catch (IOException ex) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Export Failed");
            error.setHeaderText(null);
            error.setContentText("Could not export the file: " + ex.getMessage());
            error.showAndWait();
        }
    }
}