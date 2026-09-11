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
@Table(name = "leave_request")
public class Leave_request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // ANNUAL, SICK, MATERNITY, GRIEF, UNPAID, PERMANENT
    @Column(name = "leave_type")
    private String leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Nullable: PERMANENT leave requests have no end date
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "reason")
    private String reason;

    // PENDING, APPROVED, REJECTED
    @Column(name = "status")
    private String status;

    @Column(name = "approved_by")
    private String approvedBy;
}