package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Payroll;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    public List<Payroll> getAllEmployeesPayroll() {
        return payrollRepository.findAll();
    }

    // Returns all payroll records history for an employee
    public List<Payroll> getPayrollByEmployeeId(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    // Returns a specific monthly payroll record for an employee
    public Optional<Payroll> getEmployeePayrollForMonth(Long employeeId, String payPeriod) {
        return payrollRepository.findByEmployeeIdAndPayPeriod(employeeId, payPeriod);
    }
}