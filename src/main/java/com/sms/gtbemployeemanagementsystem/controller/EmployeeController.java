package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    public List<Employee> getAll() {
        return employeeService.findAll();
    }

    public Optional<Employee> getById(Long id) {
        return employeeService.findById(id);
    }

    public List<Employee> getByDepartment(Long departmentId) {
        return employeeService.findByDepartmentId(departmentId);
    }

    public Employee create(Employee employee) {
        return employeeService.save(employee);
    }

    public Employee update(Long id, Employee employee) {
        employee.setId(id);
        return employeeService.save(employee);
    }

    public void delete(Long id) {
        employeeService.deleteById(id);
    }
}