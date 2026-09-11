package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payroll_custom_column")
public class PayrollCustomColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "column_name", nullable = false, unique = true)
    private String columnName;
}