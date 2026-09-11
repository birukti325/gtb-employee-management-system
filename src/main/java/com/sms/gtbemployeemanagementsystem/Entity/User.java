package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role; // "ADMIN" or "EMPLOYEE"

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee; // null for admin accounts

    @Column(name = "payroll_access", nullable = false)
    private boolean payrollAccess = false;

    @Column(name = "department_access", nullable = false)
    private boolean departmentAccess = false;

    @Column(name = "project_access", nullable = false)
    private boolean projectAccess = false;
}