package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountSettingsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public enum ResetResult {
        SUCCESS,
        NO_ACCOUNTS_FOUND,
        WEAK_NEW_PASSWORD
    }

    public static class ResetOutcome {
        private final ResetResult result;
        private final int accountsUpdated;

        public ResetOutcome(ResetResult result, int accountsUpdated) {
            this.result = result;
            this.accountsUpdated = accountsUpdated;
        }

        public ResetResult getResult() {
            return result;
        }

        public int getAccountsUpdated() {
            return accountsUpdated;
        }
    }

    @Transactional
    public ResetOutcome resetSharedPasswordForRole(String role, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            return new ResetOutcome(ResetResult.WEAK_NEW_PASSWORD, 0);
        }

        List<User> accounts = userRepository.findAll().stream()
                .filter(u -> role.equalsIgnoreCase(u.getRole()))
                .toList();

        if (accounts.isEmpty()) {
            return new ResetOutcome(ResetResult.NO_ACCOUNTS_FOUND, 0);
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        for (User account : accounts) {
            account.setPassword(encodedNewPassword);
        }
        userRepository.saveAll(accounts);

        return new ResetOutcome(ResetResult.SUCCESS, accounts.size());
    }

    public boolean verifyCredentials(String role, String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> role.equalsIgnoreCase(user.getRole()))
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    public boolean verifyCredentialsForUser(User user, String role, String rawPassword) {
        if (user == null) return false;
        if (!role.equalsIgnoreCase(user.getRole())) return false;
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public enum ChangePasswordResult {
        SUCCESS,
        WRONG_CURRENT_PASSWORD,
        WEAK_NEW_PASSWORD,
        USER_NOT_FOUND
    }

    @Transactional
    public ChangePasswordResult changeOwnPassword(String username, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            return ChangePasswordResult.WEAK_NEW_PASSWORD;
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                        return ChangePasswordResult.WRONG_CURRENT_PASSWORD;
                    }
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ChangePasswordResult.SUCCESS;
                })
                .orElse(ChangePasswordResult.USER_NOT_FOUND);
    }

    public enum ForgotPasswordResult {
        SUCCESS,
        EMPLOYEE_NOT_FOUND,
        INACTIVE_EMPLOYEE,
        NO_LINKED_ACCOUNT,
        WEAK_NEW_PASSWORD
    }

    @Transactional
    public ForgotPasswordResult resetPasswordByIdentity(String fullName, String departmentName, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            return ForgotPasswordResult.WEAK_NEW_PASSWORD;
        }

        Optional<Employee> employeeOpt = employeeRepository.findByFullNameAndDepartmentName(fullName, departmentName);
        if (employeeOpt.isEmpty()) {
            return ForgotPasswordResult.EMPLOYEE_NOT_FOUND;
        }

        Employee employee = employeeOpt.get();
        if (!employee.isActive()) {
            return ForgotPasswordResult.INACTIVE_EMPLOYEE;
        }

        Optional<User> userOpt = userRepository.findByEmployee_Id(employee.getId());
        if (userOpt.isEmpty()) {
            return ForgotPasswordResult.NO_LINKED_ACCOUNT;
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ForgotPasswordResult.SUCCESS;
    }
}