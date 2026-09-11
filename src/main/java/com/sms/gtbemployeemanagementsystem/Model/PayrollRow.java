package com.sms.gtbemployeemanagementsystem.Model;

public class PayrollRow {
    private int id;
    private String employeeName;
    private double baseSalary;
    private double allowances;
    private double deductions;
    private double netPayout;

    // Full 6-field Constructor
    public PayrollRow(int id, String employeeName, double baseSalary, double allowances, double deductions, double netPayout) {
        this.id = id;
        this.employeeName = employeeName;
        this.baseSalary = baseSalary;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netPayout = netPayout;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public double getAllowances() {
        return allowances;
    }

    public void setAllowances(double allowances) {
        this.allowances = allowances;
    }

    public double getDeductions() {
        return deductions;
    }

    public void setDeductions(double deductions) {
        this.deductions = deductions;
    }

    public double getNetPayout() {
        return netPayout;
    }

    public void setNetPayout(double netPayout) {
        this.netPayout = netPayout;
    }
}