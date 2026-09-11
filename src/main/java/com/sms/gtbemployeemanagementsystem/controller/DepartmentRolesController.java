package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Departments;
import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DepartmentRolesController {

    // =========================================================
    // FXML CONTROLS
    // =========================================================

    @FXML
    private Label deptNameLabel;

    @FXML
    private TableView<DepartmentRoleGroup> rolesTable;

    @FXML
    private TableColumn<DepartmentRoleGroup, String> colRole;

    @FXML
    private TableColumn<DepartmentRoleGroup, String> colEmployees;


    // =========================================================
    // REPOSITORY
    // =========================================================

    @Autowired
    private EmployeeRepository employeeRepository;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        colRole.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );

        colEmployees.setCellValueFactory(
                new PropertyValueFactory<>("employees")
        );
    }


    // =========================================================
    // SET DEPARTMENT
    // =========================================================

    public void setDepartment(Departments dept) {

        if (dept == null) {
            return;
        }

        // Display department name
        deptNameLabel.setText(
                dept.getName()
        );


        // =====================================================
        // GET EMPLOYEES FROM DATABASE
        // =====================================================

        List<Employee> employees =
                employeeRepository.findByDepartmentId(
                        dept.getId()
                );


        // =====================================================
        // GROUP EMPLOYEES BY PROFESSION
        // =====================================================

        /*
         * Example:
         *
         * Management
         *
         * Tewodros Desta       -> Chef
         * Eskedar Wondimu      -> Accountant
         * Kbede Belachew      -> General Executive
         *
         * becomes:
         *
         * Chef              -> Tewodros Desta
         * Accountant        -> Eskedar Wondimu
         * General Executive -> Kbede Belachew
         */

        Map<String, List<String>> employeesByRole =
                employees.stream()

                        // Only employees having a profession
                        .filter(employee ->
                                employee.getProfession() != null
                                        && !employee.getProfession()
                                        .trim()
                                        .isEmpty()
                        )

                        .collect(
                                Collectors.groupingBy(

                                        employee ->
                                                employee.getProfession()
                                                        .trim(),

                                        LinkedHashMap::new,

                                        Collectors.mapping(
                                                Employee::getFullName,
                                                Collectors.toList()
                                        )
                                )
                        );


        // =====================================================
        // SORT ROLES ALPHABETICALLY
        // =====================================================

        List<Map.Entry<String, List<String>>> sortedRoles =
                new ArrayList<>(
                        employeesByRole.entrySet()
                );


        sortedRoles.sort(
                Map.Entry.comparingByKey(
                        String.CASE_INSENSITIVE_ORDER
                )
        );


        // =====================================================
        // CREATE TABLE ROWS
        // =====================================================

        List<DepartmentRoleGroup> rows =
                new ArrayList<>();


        for (Map.Entry<String, List<String>> entry :
                sortedRoles) {

            String role =
                    entry.getKey();

            List<String> employeeNames =
                    entry.getValue();


            // Sort employee names
            employeeNames.sort(
                    String.CASE_INSENSITIVE_ORDER
            );


            String employeesText =
                    String.join(
                            ", ",
                            employeeNames
                    );


            rows.add(
                    new DepartmentRoleGroup(
                            role,
                            employeesText
                    )
            );
        }


        // =====================================================
        // HANDLE EMPLOYEES WITH NO PROFESSION
        // =====================================================

        /*
         * Employees without a profession are not included
         * in the role grouping above.
         *
         * If you want them displayed, show them under
         * "No Role Assigned".
         */

        List<String> employeesWithoutRole =
                employees.stream()

                        .filter(employee ->
                                employee.getProfession() == null
                                        || employee.getProfession()
                                        .trim()
                                        .isEmpty()
                        )

                        .map(Employee::getFullName)

                        .filter(name ->
                                name != null
                                        && !name.trim().isEmpty()
                        )

                        .sorted(
                                String.CASE_INSENSITIVE_ORDER
                        )

                        .collect(
                                Collectors.toList()
                        );


        if (!employeesWithoutRole.isEmpty()) {

            rows.add(
                    new DepartmentRoleGroup(
                            "No Role Assigned",
                            String.join(
                                    ", ",
                                    employeesWithoutRole
                            )
                    )
            );
        }


        // =====================================================
        // DISPLAY DATA
        // =====================================================

        rolesTable.setItems(
                FXCollections.observableArrayList(
                        rows
                )
        );


        rolesTable.refresh();
    }
}