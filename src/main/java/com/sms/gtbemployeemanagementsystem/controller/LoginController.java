package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Entity.UserSession;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import com.sms.gtbemployeemanagementsystem.Security.SecurityConfig.SessionContext;
import com.sms.gtbemployeemanagementsystem.Service.AccountSettingsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ToggleButton showPasswordToggle;
    @FXML private javafx.scene.layout.StackPane notificationBell;
    @FXML private javafx.scene.layout.StackPane notificationBadge;
    @FXML private Label notificationCountLabel;

    @Autowired
    private com.sms.gtbemployeemanagementsystem.Service.PasswordResetRequestService passwordResetRequestService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private SessionContext sessionContext;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountSettingsService accountSettingsService;
    @Autowired private UserSession userSession;

    @FXML
    public void initialize() {
        visiblePasswordField.managedProperty().bind(showPasswordToggle.selectedProperty());
        visiblePasswordField.visibleProperty().bind(showPasswordToggle.selectedProperty());
        passwordField.managedProperty().bind(showPasswordToggle.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordToggle.selectedProperty().not());
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    public void onLoginClicked() {
        String enteredUser = (usernameField.getText() != null) ? usernameField.getText().trim() : "";
        String enteredPass = (passwordField.getText() != null) ? passwordField.getText().trim() : "";

        if (enteredUser.isEmpty() || enteredPass.isEmpty()) {
            showErrorAlert("Login Error", "Please enter your username and password.");
            return;
        }

        handleLogin(enteredUser, enteredPass);
    }

    public void handleLogin(String enteredUser, String enteredPass) {
        // Admin still logs in with the fixed admin username (not tied to an Employee record)
        if (accountSettingsService.verifyCredentials("ADMIN", enteredUser, enteredPass)) {
            User adminUser = userRepository.findByUsername(enteredUser).orElse(null);
            if (adminUser != null) {
                userSession.setLoggedInUser(adminUser);
                sessionContext.setCurrentUser(adminUser);
            }
            redirectToAdminDashboard();
            return;
        }

        // Employees log in by full name instead of a generated username
        Optional<User> employeeUserOpt = resolveEmployeeUserByFullName(enteredUser);

        if (employeeUserOpt.isPresent()) {
            User employeeUser = employeeUserOpt.get();

            if (!accountSettingsService.verifyCredentialsForUser(employeeUser, "EMPLOYEE", enteredPass)) {
                showErrorAlert("Login Failed", "Invalid username or password.");
                return;
            }

            Employee employee = employeeUser.getEmployee();
            if (employee == null) {
                showErrorAlert("Login Failed", "This account is not linked to an employee record.");
                return;
            }

            if (!employee.isActive()) {
                showErrorAlert("Access Denied",
                        "You no longer have access to this system as you have left the company.\n\nThank you for working with us.");
                return;
            }

            userSession.setLoggedInUser(employeeUser);
            sessionContext.setCurrentUser(employeeUser);
            redirectToEmployeeDashboard();
            return;
        }

        showErrorAlert("Login Failed", "Invalid username or password.");
    }

    @FXML
    private void handleForgotPassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Enter your full name and we'll send a reset request to the admin.");

        ButtonType sendButtonType = new ButtonType("Send Request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != sendButtonType) return;

        String fullName = nameField.getText() != null ? nameField.getText().trim() : "";
        if (fullName.isEmpty()) {
            showErrorAlert("Missing Information", "Please enter your full name.");
            return;
        }

        var submitResult = passwordResetRequestService.submitRequest(fullName);

        switch (submitResult) {
            case SUCCESS -> {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Request Sent");
                success.setHeaderText(null);
                success.setContentText("Your password reset request has been sent to the admin.");
                success.showAndWait();
            }
            case EMPLOYEE_NOT_FOUND ->
                    showErrorAlert("Not Found", "No employee record matches that name.");
            case MULTIPLE_MATCHES ->
                    showErrorAlert("Ambiguous Name",
                            "More than one employee is registered with this name. Please contact an admin directly.");
        }
    }

    /**
     * Looks up the employee's linked User account by their full name.
     * If more than one employee shares that name, login is rejected with a
     * clear message rather than silently picking one.
     */
    private Optional<User> resolveEmployeeUserByFullName(String fullName) {
        List<Employee> matches = employeeRepository.findByFullNameIgnoreCase(fullName);

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        if (matches.size() > 1) {
            showErrorAlert("Login Failed",
                    "More than one employee is registered with this name. Please contact an admin to resolve this.");
            return Optional.empty();
        }

        Employee employee = matches.get(0);
        return userRepository.findByEmployee_Id(employee.getId());
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void redirectToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sms/gtbemployeemanagementsystem/dashboard-view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent adminView = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(adminView));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redirectToEmployeeDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sms/gtbemployeemanagementsystem/employee-dashboard-view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent employeeView = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(employeeView));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}