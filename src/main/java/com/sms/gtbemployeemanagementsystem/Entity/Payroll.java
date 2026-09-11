package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "pay_period", nullable = false)
    private String payPeriod = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
    @Column(name = "report_month")
    private String reportMonth;

    @Column(name = "submitted")
    private boolean submitted = false;

    @Column(name = "base_salary", nullable = false)
    private double baseSalary;

    @Column(name = "allowances", nullable = false)
    private double allowances;

    @Column(name = "transport_allowance", nullable = false)
    private double transportAllowance;

    @Column(name = "tax", nullable = false)
    private double tax;

    @Column(name = "deductions", nullable = false)
    private double deductions;

    @Column(name = "pension", nullable = false)
    private double pension; // Employee pension contribution, 7%

    @Column(name = "employer_pension", nullable = false)
    private double employerPension; // Employer pension contribution, 11%

    @Column(name = "net_payout", nullable = false)
    private double netPayout;
}