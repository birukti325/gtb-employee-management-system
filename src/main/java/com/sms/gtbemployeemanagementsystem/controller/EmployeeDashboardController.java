package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.UserSession;
import com.sms.gtbemployeemanagementsystem.Entity.ProfileInfo;
import com.sms.gtbemployeemanagementsystem.Service.EmployeeProfileService;
import com.sms.gtbemployeemanagementsystem.Service.ProfilePhotoService;
import com.sms.gtbemployeemanagementsystem.Entity.ProfilePhotoPopup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Component
public class EmployeeDashboardController {

    @FXML private VBox contentArea;
    @FXML private Button attendanceNavBtn;
    @FXML private Button leaveNavBtn;
    @FXML private Label nameLabel;
    @FXML private Label idLabel;
    @FXML private StackPane photoContainer;
    @FXML private Circle photoPlaceholder;
    @FXML private ImageView profileImageView;
    @FXML private Label photoPlusLabel;
    @FXML private StackPane bellContainer;
    @FXML private StackPane unreadBadge;
    @FXML private Label unreadCountLabel;
    @FXML private Button documentsNavBtn;
    @FXML private Button payrollNavBtn;
    @FXML private Button departmentsNavBtn;
    @FXML private Button projectsNavBtn;
    @FXML private Button payrollHistoryNavBtn;
    @FXML private Button settingsNavBtn;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProfilePhotoService photoService;

    @Autowired
    private EmployeeProfileService profileService;

    @Autowired
    private com.sms.gtbemployeemanagementsystem.Service.NotificationService notificationService;

    @Autowired
    private com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository employeeRepository;

    @Autowired
    private com.sms.gtbemployeemanagementsystem.Service.StaffDocumentService staffDocumentService;

    @Autowired
    private UserSession userSession;

    private static final String ACTIVE_STYLE =
            "-fx-background-color: white; -fx-text-fill: #6d28d9; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 12px; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 12px; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        if (userSession.getLoggedInUser() != null) {
            String fullName = userSession.getLoggedInUser().getUsername();
            nameLabel.setText(fullName);
            idLabel.setText("ID: " + userSession.getCurrentEmployeeId());

            applyAccessPermissions(userSession.getLoggedInUser());
        }

        Circle clip = new Circle(28, 28, 28);
        profileImageView.setClip(clip);

        loadProfilePhoto();
        refreshNotificationBadge();
        showAttendance();
    }

    @FXML
    public void showPayrollHistory() {
        loadIntoContentArea("payroll-history-view.fxml");
        setActiveNav(payrollHistoryNavBtn);
    }

    private void applyAccessPermissions(com.sms.gtbemployeemanagementsystem.Entity.User user) {
        payrollNavBtn.setVisible(user.isPayrollAccess());
        payrollNavBtn.setManaged(user.isPayrollAccess());

        payrollHistoryNavBtn.setVisible(user.isPayrollAccess());
        payrollHistoryNavBtn.setManaged(user.isPayrollAccess());

        departmentsNavBtn.setVisible(user.isDepartmentAccess());
        departmentsNavBtn.setManaged(user.isDepartmentAccess());

        projectsNavBtn.setVisible(user.isProjectAccess());
        projectsNavBtn.setManaged(user.isProjectAccess());
    }
    @FXML
    public void showAttendance() {
        loadIntoContentArea("employee-attendance-view.fxml");
        setActiveNav(attendanceNavBtn);
    }

    @FXML
    public void showPayroll() {
        loadIntoContentArea("employee-payroll.fxml");
        setActiveNav(payrollNavBtn);
    }

    @FXML
    public void showDepartments() {
        loadIntoContentArea("departments-view.fxml");
        setActiveNav(departmentsNavBtn);
    }

    @FXML
    public void showProjects() {
        loadIntoContentArea("employee-projects-view.fxml");
        setActiveNav(projectsNavBtn);
    }

    @FXML
    public void showLeaveRequests() {
        loadIntoContentArea("employee-leave-view.fxml");
        setActiveNav(leaveNavBtn);
    }

    @FXML
    public void showDocuments() {
        loadIntoContentArea("employee-documents-view.fxml");
        setActiveNav(documentsNavBtn);
    }

    @FXML
    public void showSettings() {
        loadIntoContentArea("employee-settings-view.fxml");
        setActiveNav(settingsNavBtn);
    }

    private void setActiveNav(Button active) {
        attendanceNavBtn.setStyle(INACTIVE_STYLE);
        leaveNavBtn.setStyle(INACTIVE_STYLE);
        documentsNavBtn.setStyle(INACTIVE_STYLE);
        payrollNavBtn.setStyle(INACTIVE_STYLE);
        payrollHistoryNavBtn.setStyle(INACTIVE_STYLE);
        departmentsNavBtn.setStyle(INACTIVE_STYLE);
        projectsNavBtn.setStyle(INACTIVE_STYLE);
        settingsNavBtn.setStyle(INACTIVE_STYLE);
        if (active != null) {
            active.setStyle(ACTIVE_STYLE);
        }
    }

    @FXML
    public void handleLogout() {
        try {
            userSession.clear();
            URL resource = resolveResourcePath("login-view.fxml");
            if (resource == null) {
                System.err.println("Could not find login-view.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginView = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePhotoView() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        File photoFile = photoService.findExistingPhoto(employeeId);
        String fullName = userSession.getLoggedInUser() != null ? userSession.getLoggedInUser().getUsername() : "";
        ProfileInfo info = profileService.getProfileInfo(employeeId, fullName);

        ProfilePhotoPopup.show(
                contentArea.getScene().getWindow(),
                photoFile,
                info,
                this::openFileChooserAndSave
        );
    }

    private void refreshNotificationBadge() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        long unread = notificationService.getUnreadCount(employeeId);
        if (unread > 0) {
            unreadBadge.setVisible(true);
            unreadCountLabel.setText(String.valueOf(unread));
        } else {
            unreadBadge.setVisible(false);
        }
    }

    @FXML
    public void handleBellClick() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        var notifications = notificationService.getAll(employeeId);

        VBox listBox = new VBox(10);
        listBox.setPadding(new javafx.geometry.Insets(16));
        listBox.setPrefWidth(300);
        listBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");

        Label header = new Label("Notifications");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #4c1d95;");
        listBox.getChildren().add(header);

        if (notifications.isEmpty()) {
            Label empty = new Label("No notifications yet.");
            empty.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 12px;");
            listBox.getChildren().add(empty);
        } else {
            for (var n : notifications) {
                VBox item = new VBox(2);
                item.setStyle("-fx-padding: 8px; -fx-background-color: " + (n.isRead() ? "transparent" : "#f3e8ff") + "; -fx-background-radius: 6px;");
                Label msg = new Label(n.getMessage());
                msg.setWrapText(true);
                msg.setStyle("-fx-text-fill: #4c1d95; -fx-font-size: 12px;");
                Label time = new Label(n.getCreatedAt().toLocalDate().toString());
                time.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 10px;");
                item.getChildren().addAll(msg, time);
                listBox.getChildren().add(item);
            }
        }

        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.getContent().add(listBox);
        popup.setAutoHide(true);

        javafx.geometry.Bounds bounds = bellContainer.localToScreen(bellContainer.getBoundsInLocal());
        popup.show(bellContainer, bounds.getMinX() - 250, bounds.getMaxY() + 10);

        notificationService.markAllRead(employeeId);
        refreshNotificationBadge();
    }

    private void openFileChooserAndSave() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(contentArea.getScene().getWindow());
        if (selectedFile == null) return;

        try {
            photoService.savePhoto(employeeId, selectedFile);
            loadProfilePhoto();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProfilePhoto() {
        Long employeeId = userSession.getCurrentEmployeeId();
        if (employeeId == null) return;

        File photoFile = photoService.findExistingPhoto(employeeId);
        if (photoFile != null) {
            Image image = new Image(photoFile.toURI().toString());
            profileImageView.setImage(image);
            photoPlaceholder.setVisible(false);
            photoPlusLabel.setVisible(false);
        } else {
            profileImageView.setImage(null);
            photoPlaceholder.setVisible(true);
            photoPlusLabel.setVisible(true);
        }
    }

    private void loadIntoContentArea(String fxmlFileName) {
        try {
            URL resource = resolveResourcePath(fxmlFileName);
            if (resource == null) {
                System.err.println("[ERROR] FXML file not found in classloader resources: " + fxmlFileName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private URL resolveResourcePath(String fileName) {
        // 1. Try direct root resource load
        URL resource = getClass().getResource("/" + fileName);
        if (resource != null) return resource;

        // 2. Try relative class package location
        resource = getClass().getResource(fileName);
        if (resource != null) return resource;

        // 3. Try project subfolder fallback
        resource = getClass().getResource("/com/sms/gtbemployeemanagementsystem/" + fileName);
        if (resource != null) return resource;

        // 4. Try Context ClassLoader
        return Thread.currentThread().getContextClassLoader().getResource(fileName);
    }
}