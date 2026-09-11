package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @FXML private VBox contentArea;
    @FXML private VBox employeeSubmenu;
    @FXML private Button dashboardBtn;
    @FXML private Button employeesBtn;
    @FXML private Button employeeDirectoryBtn;
    @FXML private Button addNewEmployeeBtn;
    @FXML private Button departementsBtn;
    @FXML private Button attendanceBtn;
    @FXML private Button leaveBtn;
    @FXML private Button payrollBtn;
    @FXML private Button projectsBtn;
    @FXML private Button logOutBtn;
    @FXML private Button leftEmployeesBtn;
    @FXML private Button adminSettingsBtn;
    @FXML private javafx.scene.layout.StackPane notificationBell;
    @FXML private javafx.scene.layout.StackPane notificationBadge;
    @FXML private Label notificationCountLabel;

    @Autowired
    private com.sms.gtbemployeemanagementsystem.Service.PasswordResetRequestService passwordResetRequestService;

    @Autowired private ApplicationContext applicationContext;

    @Autowired private UserSession userSession;

    private static final String ACTIVE_STYLE =
            "-fx-background-color: white; -fx-text-fill: #6d28d9; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 6; -fx-font-weight: bold;";

    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 6;";
    private Button currentActiveButton;

    @FXML
    public void initialize() {
        Platform.runLater(this::showDashboard);
        refreshNotificationBadge();
    }

    /**
     * Loads the given FXML file into the main content area of the dashboard.
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();

            VBox.setVgrow(view, Priority.ALWAYS);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            log.error("Failed to load view: {}", fxmlPath, e);
        }
    }

    private void refreshNotificationBadge() {
        long pending = passwordResetRequestService.getPendingCount();
        if (pending > 0) {
            notificationBadge.setVisible(true);
            notificationCountLabel.setText(String.valueOf(pending));
        } else {
            notificationBadge.setVisible(false);
        }
    }

    @FXML
    private void handleNotificationClick() {
        var requests = passwordResetRequestService.getPendingRequests();

        VBox listBox = new VBox(10);
        listBox.setPadding(new javafx.geometry.Insets(16));
        listBox.setPrefWidth(320);
        listBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");

        javafx.scene.control.Label header = new javafx.scene.control.Label("Password Reset Requests");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #14213D;");
        listBox.getChildren().add(header);

        if (requests.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label("No pending requests.");
            empty.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 12px;");
            listBox.getChildren().add(empty);
        } else {
            for (var req : requests) {
                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10px; -fx-background-radius: 6px;");

                VBox info = new VBox(2);
                javafx.scene.control.Label nameLbl = new javafx.scene.control.Label(req.getEmployeeName());
                nameLbl.setStyle("-fx-text-fill: #14213D; -fx-font-weight: bold; -fx-font-size: 12px;");
                javafx.scene.control.Label timeLbl = new javafx.scene.control.Label(req.getRequestedAt().toLocalDate().toString());
                timeLbl.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 10px;");
                info.getChildren().addAll(nameLbl, timeLbl);
                javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

                Button resolveBtn = new Button("Reset to Default");
                resolveBtn.setStyle("-fx-background-color: #14213D; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
                resolveBtn.setOnAction(e -> {
                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.CONFIRMATION,
                            "Reset " + req.getEmployeeName() + "'s password back to the default shared password?",
                            javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == javafx.scene.control.ButtonType.YES) {
                            passwordResetRequestService.resolveRequest(req.getId());
                            refreshNotificationBadge();
                            listBox.getChildren().remove(row);
                        }
                    });
                });

                row.getChildren().addAll(info, resolveBtn);
                listBox.getChildren().add(row);
            }
        }

        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.getContent().add(listBox);
        popup.setAutoHide(true);

        javafx.geometry.Bounds bounds = notificationBell.localToScreen(notificationBell.getBoundsInLocal());
        popup.show(notificationBell, bounds.getMinX(), bounds.getMaxY() + 10);
    }

    private void setActiveButton(Button selected) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(INACTIVE_STYLE);
        }
        selected.setStyle(ACTIVE_STYLE);
        currentActiveButton = selected;
    }

    @FXML
    private void toggleEmployeeMenu() {
        boolean isVisible = employeeSubmenu.isVisible();
        employeeSubmenu.setVisible(!isVisible);
        employeeSubmenu.setManaged(!isVisible);
    }

    @FXML
    private void showDashboard() {
        loadView("/com/sms/gtbemployeemanagementsystem/dashboard-overview.fxml");
        setActiveButton(dashboardBtn);
    }

    @FXML
    private void showEmployees() {
        loadView("/com/sms/gtbemployeemanagementsystem/directory-view.fxml");
        setActiveButton(employeesBtn);
    }

    @FXML
    private void showAddNewEmployee() {
        loadView("/com/sms/gtbemployeemanagementsystem/add-new-view.fxml");
        setActiveButton(employeesBtn);
    }

    @FXML
    private void showLeftEmployees() {
        loadView("/com/sms/gtbemployeemanagementsystem/former-staff.fxml");
        setActiveButton(leftEmployeesBtn);
    }

    @FXML
    private void showDepartements() {
        loadView("/com/sms/gtbemployeemanagementsystem/departments-view.fxml");
        setActiveButton(departementsBtn);
    }

    @FXML
    private void showAttendance() {
        loadView("/com/sms/gtbemployeemanagementsystem/attendance-view.fxml");
        setActiveButton(attendanceBtn);
    }

    @FXML
    private void showLeave() {
        loadView("/com/sms/gtbemployeemanagementsystem/leave_request-view.fxml");
        setActiveButton(leaveBtn);
    }

    @FXML
    private void showPayroll() {
        loadView("/com/sms/gtbemployeemanagementsystem/payroll-view.fxml");
        setActiveButton(payrollBtn);
    }

    @FXML
    private void showProjects() {
        loadView("/com/sms/gtbemployeemanagementsystem/projects-view.fxml");
        setActiveButton(projectsBtn);
    }

    @FXML
    private void showAdminSettings() {
        loadView("/com/sms/gtbemployeemanagementsystem/admin-settings-view.fxml");
        setActiveButton(adminSettingsBtn);
    }

    @FXML
    private void handleLogout() {
        try {
            userSession.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sms/gtbemployeemanagementsystem/login-view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginView = loader.load();

            Stage stage = (Stage) logOutBtn.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.setTitle("GTB Employee Management System - Login");
            stage.show();
        } catch (IOException e) {
            log.error("Failed to load login view during logout", e);
        }
    }
}