package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employmenthistory")
public class EmploymentHistory {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @EqualsAndHashCode.Exclude
    @OneToOne
    @MapsId
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "contract_agreement")
    private String contractAgreement;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "starting_salary")
    private double startingSalary;

    @Column(name = "current_salary")
    private double currentSalary;

    @Column(name = "special_benefits")
    private String specialBenefits;
}