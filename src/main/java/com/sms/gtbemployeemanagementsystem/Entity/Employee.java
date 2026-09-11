package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "profession")
    private String profession;

    @Column(name = "education_details", columnDefinition = "TEXT")
    private String educationDetails;

    @Column(name = "pension_number")
    private String pensionNumber;

    @Column(name = "guarantor_name")
    private String guarantorName;

    @Column(name = "guarantor_contact")
    private String guarantorContact;

    @Column(name = "status")
    private String status;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private java.time.LocalDate createdAt = java.time.LocalDate.now();

    @Column(name = "leave_date")
    private java.time.LocalDate leaveDate;

    // FetchType.EAGER ensures JavaFX can access department details anywhere
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Departments department;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    private EmploymentHistory employmentHistory;

    public String getDepartmentName() {
        if (department != null) {
            return department.getName();
        }
        return "N/A";
    }
}