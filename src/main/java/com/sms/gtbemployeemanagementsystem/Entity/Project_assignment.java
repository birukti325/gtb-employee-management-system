package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_assignment")
public class Project_assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Projects project;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "role")
    private String role;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Projects getProject() { return project; }
    public void setProject(Projects project) { this.project = project; }

    public void setProjectId(Long projectId) {
        if (this.project == null) this.project = new Projects();
        this.project.setId(projectId);
    }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
