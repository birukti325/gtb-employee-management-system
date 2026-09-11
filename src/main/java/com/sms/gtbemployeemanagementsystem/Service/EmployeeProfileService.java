package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.Payroll;
import com.sms.gtbemployeemanagementsystem.Entity.ProfileInfo;
import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.PayrollRepository;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String DEFAULT_SHARED_PASSWORD = "changeme123";

    /**
     * Finds the User account linked to this employee, or creates one
     * with a default shared password if this is their first login.
     * Centralized here so every entry point (profile popup, login flow,
     * etc.) creates accounts the same way.
     */
    public User getOrCreateUserAccount(Employee employee, String sessionFullName) {
        Optional<User> userOpt = userRepository.findByEmployee_Id(employee.getId());
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        String nameForUsername = (sessionFullName != null && !sessionFullName.isBlank())
                ? sessionFullName
                : employee.getFullName();

        User newUser = new User();
        newUser.setUsername(nameForUsername.toLowerCase().replaceAll("\\s+", ".") + "." + employee.getId());
        newUser.setPassword(passwordEncoder.encode(DEFAULT_SHARED_PASSWORD));
        newUser.setRole("EMPLOYEE");
        newUser.setEmployee(employee);
        return userRepository.save(newUser);
    }

    public ProfileInfo getProfileInfo(Long employeeId, String sessionFullName) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            return new ProfileInfo(sessionFullName, "N/A", "N/A", "N/A", "ETB 0.00");
        }
        Employee employee = employeeOpt.get();

        User currentAccount = getOrCreateUserAccount(employee, sessionFullName);

        String salary = "ETB 0.00";
        List<Payroll> payrollList = payrollRepository.findByEmployeeId(employeeId);

        // Grab the latest payroll entry from the list if available
        if (!payrollList.isEmpty()) {
            Payroll latestPayroll = payrollList.get(payrollList.size() - 1);
            salary = String.format("ETB %,.2f", latestPayroll.getNetPayout());
        }

        return new ProfileInfo(
                employee.getFullName(),
                currentAccount.getUsername(),
                employee.getDepartmentName(),
                currentAccount.getRole(),
                salary
        );
    }
}