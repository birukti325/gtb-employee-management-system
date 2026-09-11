package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_reports")
public class ProjectReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "report_text", columnDefinition = "TEXT", nullable = false)
    private String reportText;

    @Column(name = "submitted_date")
    private LocalDate submittedDate = LocalDate.now();
}