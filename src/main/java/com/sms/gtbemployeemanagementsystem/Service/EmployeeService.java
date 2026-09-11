package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.Leave_requestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private Leave_requestRepository leaveRequestRepository;

    @Autowired
    private EmployeeDeletionHelper deletionHelper;

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public List<Employee> findAllWithDepartment() {
        return employeeRepository.findAllWithDepartment();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> findInactiveEmployees() {
        return employeeRepository.findByActiveFalseOrderByIdDesc();
    }

    public List<Employee> findByDepartmentId(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    @Transactional
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Permanently deletes an employee and every dependent record across
     * the schema that references their employee_id. Each table's cleanup
     * runs in its own independent transaction via EmployeeDeletionHelper.
     * The call itself is also wrapped here, since a REQUIRES_NEW
     * transaction's commit-phase failure can surface at the call site,
     * not just inside the helper's own try/catch.
     */
    @Transactional
    public void deleteById(Long id) {
        String[] dependentTables = {
                "leave_request",
                "payroll",
                "attendance",
                "notification",
                "staff_document",
                "performance_review",
                "project_assignment",
                "equipment_assignment",
                "safety_training",
                "employymenthistory",
                "employee_credentials",
                "\"user\""
        };

        for (String table : dependentTables) {
            try {
                deletionHelper.deleteFromTable(table, id);
            } catch (Exception ex) {
                System.err.println("Cleanup call failed for table " + table + ":");
                ex.printStackTrace();
            }
        }

        employeeRepository.deleteById(id);
    }

    @Transactional
    public void deleteEmployeeAndHistory(Long id) {
        // Remove dependent leave request records first to satisfy the foreign key constraint
        leaveRequestRepository.deleteAll(leaveRequestRepository.findByEmployee_Id(id));
        employeeRepository.deleteById(id);
    }

    @Transactional
    public void softDeleteById(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setActive(false);
            employee.setLeaveDate(java.time.LocalDate.now());
            employeeRepository.save(employee);
        });
    }
}