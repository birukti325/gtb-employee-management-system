package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payroll_custom_value")
public class PayrollCustomValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payroll_id", nullable = false)
    private Integer payrollId;

    @Column(name = "column_id", nullable = false)
    private Long columnId;

    @Column(name = "value", nullable = false)
    private double value;
}