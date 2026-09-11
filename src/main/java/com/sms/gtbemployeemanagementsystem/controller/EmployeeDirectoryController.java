package com.sms.gtbemployeemanagementsystem.controller;



import com.sms.gtbemployeemanagementsystem.Entity.Employee; // Ensure this matches your Employee entity package location

import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository; // Ensure this matches your repository package location

import javafx.collections.FXCollections;

import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.TableColumn;

import javafx.scene.control.TableView;

import javafx.scene.control.cell.PropertyValueFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;



import java.util.List;



@Component

public class EmployeeDirectoryController {



    @FXML private TableView<Employee> employeeTable;

    @FXML private TableColumn<Employee, Long> idColumn;

    @FXML private TableColumn<Employee, String> nameColumn;

    @FXML private TableColumn<Employee, String> departmentColumn;

    @FXML private TableColumn<Employee, String> roleColumn;

    @FXML private TableColumn<Employee, String> statusColumn;



    @Autowired

    private EmployeeRepository employeeRepository;



    @FXML

    public void initialize() {

// Set up the columns to map fields directly out of your Employee Entity properties

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));

        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));



        loadEmployeeData();

    }



    private void loadEmployeeData() {

        try {

            List<Employee> employeeList = employeeRepository.findAll();

            ObservableList<Employee> observableList = FXCollections.observableArrayList(employeeList);

            employeeTable.setItems(observableList);

            System.out.println("Populated directory layout with " + employeeList.size() + " active database rows.");

        } catch (Exception e) {

            System.err.println("Failed to parse database results into directory table layout view:");

            e.printStackTrace();

        }

    }

}