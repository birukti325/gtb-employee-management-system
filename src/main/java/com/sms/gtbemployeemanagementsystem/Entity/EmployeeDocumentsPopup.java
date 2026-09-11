package com.sms.gtbemployeemanagementsystem.Entity;

import com.sms.gtbemployeemanagementsystem.Service.StaffDocumentService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

public class EmployeeDocumentsPopup {

    public static void show(Window owner, Employee employee, StaffDocumentService documentService,
                            BiConsumer<Employee, String> onGuarantorSaved) {
        show(owner, employee, documentService, onGuarantorSaved, false);
    }

    public static void show(Window owner, Employee employee, StaffDocumentService documentService,
                            BiConsumer<Employee, String> onGuarantorSaved, boolean readOnly) {

        Stage popup = new Stage();
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);

        VBox contentBox = new VBox(16);
        contentBox.setPadding(new Insets(25));
        contentBox.setPrefWidth(440);

        Label title = new Label(employee.getFullName() + " — Documents");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Guarantor — read only here, edited via the Edit Employee dialog
        Label guarantorLabel = new Label("Guarantor Name");
        guarantorLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px;");
        Label guarantorValue = new Label(
                (employee.getGuarantorName() != null && !employee.getGuarantorName().isBlank())
                        ? employee.getGuarantorName() : "Not set");
        guarantorValue.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label docsLabel = new Label("Uploaded Documents");
        docsLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px;");

        VBox docsListBox = new VBox(8);
        refreshDocList(docsListBox, employee, documentService, popup, readOnly);

        // AFTER (Fixed)
        contentBox.getChildren().addAll(
                title,
                guarantorLabel, guarantorValue,
                new Separator(),
                docsLabel, docsListBox
        );

        if (!readOnly) {
            ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                    "ID_COPY", "EDUCATION_CERTIFICATE", "OTHER_CERTIFICATE"));
            typeBox.setValue("ID_COPY");
            typeBox.setStyle("-fx-background-radius: 6px;");

            Button uploadBtn = new Button("+ Upload Document");
            uploadBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 14px; -fx-cursor: hand;");
            uploadBtn.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select Document");
                File file = chooser.showOpenDialog(owner);
                if (file == null) return;

                try {
                    documentService.uploadDocument(employee, typeBox.getValue(), file);
                    refreshDocList(docsListBox, employee, documentService, popup, readOnly);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showError("Upload Failed", "Could not upload the document.\n\n" + ex.getMessage());
                }
            });

            HBox uploadRow = new HBox(8, typeBox, uploadBtn);
            uploadRow.setAlignment(Pos.CENTER_LEFT);
            contentBox.getChildren().add(uploadRow);
        }

        StackPane card = new StackPane(contentBox);
        card.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-background-radius: 16px;");
        card.setMaxSize(480, 600);

        StackPane overlay = new StackPane(card);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) popup.close();
        });

        Scene scene = new Scene(overlay);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);

        popup.setX(owner.getX());
        popup.setY(owner.getY());
        popup.setWidth(owner.getWidth());
        popup.setHeight(owner.getHeight());

        popup.show();
    }

    private static void refreshDocList(VBox docsListBox, Employee employee,
                                       StaffDocumentService documentService, Stage popup, boolean readOnly) {
        docsListBox.getChildren().clear();
        List<StaffDocument> docs = documentService.getDocuments(employee.getId());

        if (docs.isEmpty()) {
            Label empty = new Label("No documents uploaded yet.");
            empty.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 12px;");
            docsListBox.getChildren().add(empty);
            return;
        }

        for (StaffDocument doc : docs) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 6px; -fx-padding: 8px;");

            VBox info = new VBox(2);
            Label typeLabel = new Label(formatType(doc.getDocumentType()));
            typeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
            Label nameLabel = new Label(doc.getFileName());
            nameLabel.setStyle("-fx-text-fill: #d8b4fe; -fx-font-size: 11px;");
            info.getChildren().addAll(typeLabel, nameLabel);
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            Button viewBtn = new Button("View");
            viewBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
            viewBtn.setOnAction(e -> openDocument(documentService.resolveFile(doc)));

            row.getChildren().addAll(info, viewBtn);

            if (!readOnly) {
                Button deleteBtn = new Button("✕");
                deleteBtn.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: white; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
                deleteBtn.setOnAction(e -> {
                    documentService.deleteDocument(doc);
                    refreshDocList(docsListBox, employee, documentService, popup, readOnly);
                });
                row.getChildren().add(deleteBtn);
            }

            docsListBox.getChildren().add(row);
        }
    }

    private static void openFileWithOs(File file) {
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
        }
    }

    private static void openDocument(File file) {
        if (file == null || !file.exists()) {
            showError("File Not Found", "This document's file could not be found on disk.\nIt may have been moved, renamed, or deleted.");
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            showError("Cannot Open File", "Opening files isn't supported on this system.\n\nFile location:\n" + file.getAbsolutePath());
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Cannot Open File", "No application is associated with this file type, or it couldn't be opened.\n\n" + ex.getMessage());
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String formatType(String type) {
        if (type == null) return "Document";
        return switch (type) {
            case "ID_COPY" -> "ID Copy";
            case "EDUCATION_CERTIFICATE" -> "Educational Certificate";
            case "OTHER_CERTIFICATE" -> "Other Certificate";
            default -> type;
        };
    }
}