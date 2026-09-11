package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByEmployeeId(Long employeeId);

    Optional<Payroll> findByEmployeeIdAndPayPeriod(Long employeeId, String payPeriod);

    // Ensure this method is present
    List<Payroll> findByPayPeriod(String payPeriod);

    @Query("SELECT p FROM Payroll p WHERE p.submitted = true AND p.payPeriod = :payPeriod ORDER BY p.employeeName ASC")
    List<Payroll> findSubmittedByPayPeriod(@Param("payPeriod") String payPeriod);

    @Query("SELECT p FROM Payroll p WHERE p.submitted = true AND p.payPeriod LIKE CONCAT(:year, '%') ORDER BY p.payPeriod DESC, p.employeeName ASC")
    List<Payroll> findSubmittedByYear(@Param("year") String year);

    @Query("SELECT DISTINCT p.payPeriod FROM Payroll p WHERE p.submitted = true ORDER BY p.payPeriod DESC")
    List<String> findDistinctSubmittedPayPeriods();

    @Query(value = """
    SELECT p.id, 
           p.employee_id, 
           COALESCE(e.full_name, u.username, 'Employee #' || p.employee_id) AS employee_name,
           p.base_salary AS base_salary,
           p.allowances, 
           p.transport_allowance, 
           p.tax, 
           p.deductions, 
           p.pension, 
           p.employer_pension, 
           p.net_payout, 
           p.pay_period, 
           p.report_month, 
           p.submitted
    FROM payroll p
    LEFT JOIN employees e ON p.employee_id = e.id
    LEFT JOIN "user" u ON e.id = u.id
    WHERE p.submitted IS NOT TRUE OR p.submitted = FALSE
    """, nativeQuery = true)
    List<Payroll> findAllActivePayrollWithDetails();
}