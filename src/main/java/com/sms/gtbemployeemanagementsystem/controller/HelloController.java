package com.sms.gtbemployeemanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    public void initialize() {
        welcomeText.setText("Welcome to GTB Employee Management System");
    }
}
