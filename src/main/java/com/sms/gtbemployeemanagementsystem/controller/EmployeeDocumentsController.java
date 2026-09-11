package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.EmploymentHistory;
import com.sms.gtbemployeemanagementsystem.Entity.StaffDocument;
import com.sms.gtbemployeemanagementsystem.Entity.UserSession;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Service.StaffDocumentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class EmployeeDocumentsController {

    @FXML private TextField guarantorField;
    @FXML private TextField guarantorPhoneField;
    @FXML private ComboBox<String> employmentAgreementField;
    @FXML private TextField birthDateField;
    @FXML private ComboBox<String> employmentConditionsField;
    @FXML private TextField employmentDateField;

    @FXML private VBox docsListBox;
    @FXML private ComboBox<String> typeBox;

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private StaffDocumentService documentService;
    @Autowired private UserSession userSession;

    private Employee employee;

    private static final DateTimeFormatter FLEXIBLE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-M-d");

    @FXML
    public void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(
                "ID Copy",
                "Education Certeficate",
                "Other Certeficate"
        ));
        typeBox.setValue("ID_COPY");

        employmentAgreementField.setItems(FXCollections.observableArrayList(
                "Attached", "Not Attached"
        ));

        employmentConditionsField.setItems(FXCollections.observableArrayList(
                "Permanent", "Contract"
        ));

        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) return;

        EmploymentHistory history = employee.getEmploymentHistory();

        guarantorField.setText(nullSafe(employee.getGuarantorName()));
        guarantorPhoneField.setText(nullSafe(employee.getGuarantorContact()));
        birthDateField.setText(employee.getBirthYear() != null ? employee.getBirthYear().toString() : "");

        employmentAgreementField.setValue(history != null ? history.getContractAgreement() : null);
        employmentConditionsField.setValue(history != null ? history.getEmploymentType() : null);
        employmentDateField.setText(history != null && history.getJoiningDate() != null
                ? history.getJoiningDate().toString() : "");

        refreshDocList();
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String formatType(String type) {
        if (type == null) return "Document";
        return switch (type) {
            case "ID_COPY" -> "ID Copy";
            case "EDUCATION_CERTIFICATE" -> "Educational Certificate";
            case "OTHER_CERTIFICATE" -> "Other Certificate";
            default -> type;
        };
    }

    private LocalDate parseFlexibleDate(String text) throws DateTimeParseException {
        String normalized = text.trim().replace('/', '-').replace('.', '-');
        return LocalDate.parse(normalized, FLEXIBLE_DATE_FORMAT);
    }

    @FXML
    public void handleSaveDetails() {
        if (employee == null) return;

        // --- Employee-level fields ---
        employee.setGuarantorName(guarantorField.getText().trim());
        employee.setGuarantorContact(guarantorPhoneField.getText().trim());

        String birthYearText = birthDateField.getText().trim();
        if (!birthYearText.isEmpty()) {
            try {
                employee.setBirthYear(Integer.parseInt(birthYearText));
            } catch (NumberFormatException ex) {
                showError("Invalid Birth Year", "Please enter birth year as a whole number (e.g. 1990).");
                return;
            }
        } else {
            employee.setBirthYear(null);
        }

        // --- EmploymentHistory fields (create if missing) ---
        EmploymentHistory history = employee.getEmploymentHistory();
        if (history == null) {
            history = new EmploymentHistory();
            history.setEmployee(employee);
            employee.setEmploymentHistory(history);
        }

        String agreementValue = employmentAgreementField.getEditor().getText().trim();
        history.setContractAgreement(agreementValue.isEmpty() ? null : agreementValue);

        String conditionValue = employmentConditionsField.getEditor().getText().trim();
        history.setEmploymentType(conditionValue.isEmpty() ? null : conditionValue);

        String joiningDateText = employmentDateField.getText().trim();
        if (!joiningDateText.isEmpty()) {
            try {
                history.setJoiningDate(parseFlexibleDate(joiningDateText));
            } catch (DateTimeParseException ex) {
                showError("Invalid Employment Date",
                        "Could not parse \"" + joiningDateText + "\" as a date.\nPlease enter it as YYYY-MM-DD (e.g. 2022-01-10).");
                return;
            }
        } else {
            history.setJoiningDate(null);
        }

        employeeRepository.save(employee); // cascades to EmploymentHistory
        showInfo("Saved", "Employee details updated.");
    }

    @FXML
    public void handleUpload() {
        if (employee == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Document");
        File file = chooser.showOpenDialog(docsListBox.getScene().getWindow());
        if (file == null) return;

        try {
            documentService.uploadDocument(employee, typeBox.getValue(), file);
            refreshDocList();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Upload Failed", "Could not upload the document.\n\n" + ex.getMessage());
        }
    }

    private void refreshDocList() {
        docsListBox.getChildren().clear();
        List<StaffDocument> docs = documentService.getDocuments(employee.getId());

        if (docs.isEmpty()) {
            Label empty = new Label("No documents uploaded yet.");
            empty.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 12px;");
            docsListBox.getChildren().add(empty);
            return;
        }

        for (StaffDocument doc : docs) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #f3e8ff; -fx-background-radius: 6px; -fx-padding: 10px;");

            VBox info = new VBox(2);
            Label typeLabel = new Label(formatType(doc.getDocumentType()));
            typeLabel.setStyle("-fx-text-fill: #4c1d95; -fx-font-weight: bold; -fx-font-size: 12px;");
            Label nameLabel = new Label(doc.getFileName());
            nameLabel.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 11px;");
            info.getChildren().addAll(typeLabel, nameLabel);
            HBox.setHgrow(info, Priority.ALWAYS);

            Button viewBtn = new Button("View");
            viewBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
            viewBtn.setOnAction(e -> openDocument(documentService.resolveFile(doc)));

            Button deleteBtn = new Button("✕");
            deleteBtn.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #7f1d1d; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
            deleteBtn.setOnAction(e -> {
                documentService.deleteDocument(doc);
                refreshDocList();
            });

            row.getChildren().addAll(info, viewBtn, deleteBtn);
            docsListBox.getChildren().add(row);
        }
    }

    private void openDocument(File file) {
        if (file == null || !file.exists()) {
            showError("File Not Found", "This document's file could not be found on disk.\nIt may have been moved, renamed, or deleted.");
            return;
        }

        String name = file.getName().toLowerCase();
        boolean isImage = name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".gif") || name.endsWith(".bmp");

        if (isImage) {
            showImagePreview(file);
            return;
        }

        try {
            openFileExternally(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Cannot Open File", "Could not open this document.\n\nFile location:\n" + file.getAbsolutePath());
        }
    }

    private void showImagePreview(File file) {
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
    }

    private void openFileExternally(File file) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            new ProcessBuilder("cmd", "/c", "start", "\"\"", file.getAbsolutePath()).start();
        } else if (os.contains("mac")) {
            new ProcessBuilder("open", file.getAbsolutePath()).start();
        } else {
            new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}