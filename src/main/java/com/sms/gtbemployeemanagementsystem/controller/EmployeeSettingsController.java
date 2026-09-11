package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import com.sms.gtbemployeemanagementsystem.Service.AccountSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmployeeSettingsController {

    @FXML private PasswordField currentPasswordField;
    @FXML private TextField currentPasswordVisible;
    @FXML private ToggleButton currentPasswordToggle;

    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisible;
    @FXML private ToggleButton newPasswordToggle;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisible;
    @FXML private ToggleButton confirmPasswordToggle;

    @Autowired
    private AccountSettingsService accountSettingsService;

    @Autowired
    private UserSession userSession;

    @FXML
    public void initialize() {
        bindPasswordToggle(currentPasswordField, currentPasswordVisible, currentPasswordToggle);
        bindPasswordToggle(newPasswordField, newPasswordVisible, newPasswordToggle);
        bindPasswordToggle(confirmPasswordField, confirmPasswordVisible, confirmPasswordToggle);
    }

    /**
     * Wires up a PasswordField + mirrored TextField + ToggleButton so the
     * toggle switches between masked and plain-text display, while keeping
     * both fields' text in sync via a bidirectional binding.
     */
    private void bindPasswordToggle(PasswordField passwordField, TextField visibleField, ToggleButton toggle) {
        visibleField.managedProperty().bind(toggle.selectedProperty());
        visibleField.visibleProperty().bind(toggle.selectedProperty());
        passwordField.managedProperty().bind(toggle.selectedProperty().not());
        passwordField.visibleProperty().bind(toggle.selectedProperty().not());
        visibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleChangePassword() {
        if (userSession.getLoggedInUser() == null) {
            showAlert(Alert.AlertType.ERROR, "Not Logged In", "No active session found. Please log in again.");
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword == null || currentPassword.isBlank()
                || newPassword == null || newPassword.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Passwords Don't Match", "New password and confirmation must match.");
            return;
        }

        String username = userSession.getLoggedInUser().getUsername();
        AccountSettingsService.ChangePasswordResult result =
                accountSettingsService.changeOwnPassword(username, currentPassword, newPassword);

        switch (result) {
            case SUCCESS -> {
                showAlert(Alert.AlertType.INFORMATION, "Password Updated", "Your password has been changed successfully.");
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            }
            case WRONG_CURRENT_PASSWORD ->
                    showAlert(Alert.AlertType.ERROR, "Incorrect Password", "Your current password is incorrect.");
            case WEAK_NEW_PASSWORD ->
                    showAlert(Alert.AlertType.WARNING, "Weak Password", "New password must be at least 6 characters long.");
            case USER_NOT_FOUND ->
                    showAlert(Alert.AlertType.ERROR, "Account Error", "Could not find your account. Please contact an admin.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}