package com.sms.gtbemployeemanagementsystem.Entity;

public class ProfileInfo {
    private final String fullName;
    private final String username;
    private final String department;
    private final String role;
    private final String salary;

    public ProfileInfo(String fullName, String username, String department, String role, String salary) {
        this.fullName = fullName;
        this.username = username;
        this.department = department;
        this.role = role;
        this.salary = salary;
    }

    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getDepartment() { return department; }
    public String getRole() { return role; }
    public String getSalary() { return salary; }
}