package com.sms.gtbemployeemanagementsystem.controller;

public class DepartmentRoleGroup {
    private final String role;
    private final String employees;

    public DepartmentRoleGroup(String role, String employees) {
        this.role = role;
        this.employees = employees;
    }

    public String getRole() {
        return role;
    }

    public String getEmployees() {
        return employees;
    }
}