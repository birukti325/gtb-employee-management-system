package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Service.AccountSettingsService;
import com.sms.gtbemployeemanagementsystem.Service.AccountSettingsService.ResetOutcome;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminSettingsController {

    // Admin Current Password Fields
    @FXML private PasswordField adminCurrentPasswordInput;
    @FXML private TextField adminCurrentPasswordVisible;
    @FXML private ToggleButton adminCurrentPasswordToggle;

    // Admin New Password Fields
    @FXML private PasswordField adminNewPasswordInput;
    @FXML private TextField adminNewPasswordVisible;
    @FXML private ToggleButton adminNewPasswordToggle;

    // Employee Current Password Fields

    @Autowired
    private AccountSettingsService accountSettingsService;

    @FXML
    public void initialize() {
        // Setup visibility toggle bindings
        bindPasswordVisibility(adminCurrentPasswordInput, adminCurrentPasswordVisible, adminCurrentPasswordToggle);
        bindPasswordVisibility(adminNewPasswordInput, adminNewPasswordVisible, adminNewPasswordToggle);


        // Set default values and lock current password fields from editing
        if (adminCurrentPasswordInput != null && adminCurrentPasswordVisible != null) {
            adminCurrentPasswordInput.setText("GTB123");
            adminCurrentPasswordInput.setEditable(false);
            adminCurrentPasswordVisible.setEditable(false);
        }
    }

    private void bindPasswordVisibility(PasswordField passwordField, TextField visibleField, ToggleButton toggle) {
        if (passwordField != null && visibleField != null && toggle != null) {
            visibleField.managedProperty().bind(toggle.selectedProperty());
            visibleField.visibleProperty().bind(toggle.selectedProperty());
            passwordField.managedProperty().bind(toggle.selectedProperty().not());
            passwordField.visibleProperty().bind(toggle.selectedProperty().not());
            visibleField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }

    @FXML
    private void handleAdminPasswordReset() {
        confirmAndReset("ADMIN", adminCurrentPasswordInput, adminNewPasswordInput);
    }

    private void confirmAndReset(String role, PasswordField currentField, PasswordField newField) {
        String newPassword = newField != null ? newField.getText() : "";

        if (newPassword == null || newPassword.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter a new password.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Password Change");
        confirm.setHeaderText("Confirm the new " + role.toLowerCase() + " password");
        confirm.setContentText("This will replace the login password for every " + role.toLowerCase() + " account. Continue?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            applyReset(role, currentField, newField, newPassword);
        }
    }

    private void applyReset(String role, PasswordField currentField, PasswordField newField, String newPassword) {
        ResetOutcome outcome = accountSettingsService.resetSharedPasswordForRole(role, newPassword);

        switch (outcome.getResult()) {
            case SUCCESS -> {
                showAlert(Alert.AlertType.INFORMATION, "Password Updated",
                        "The " + role.toLowerCase() + " password has been changed for all "
                                + outcome.getAccountsUpdated() + " account(s).");

                // Reflect the new password immediately in the "Current Password" box
                // so the admin isn't shown stale data after a successful change.
                if (currentField != null) {
                    currentField.setText(newPassword);
                }
                newField.clear();
            }
            case NO_ACCOUNTS_FOUND ->
                    showAlert(Alert.AlertType.WARNING, "No Accounts Found", "There are no " + role.toLowerCase() + " accounts to update.");
            case WEAK_NEW_PASSWORD ->
                    showAlert(Alert.AlertType.WARNING, "Weak Password", "New password must be at least 6 characters long.");
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