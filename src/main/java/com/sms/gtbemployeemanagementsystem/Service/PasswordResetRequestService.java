package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.PasswordResetRequest;
import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Repository.EmployeeRepository;
import com.sms.gtbemployeemanagementsystem.Repository.PasswordResetRequestRepository;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetRequestService {

    private static final String DEFAULT_EMPLOYEE_PASSWORD = "gtb_password";

    @Autowired private PasswordResetRequestRepository requestRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public enum SubmitResult {
        SUCCESS,
        EMPLOYEE_NOT_FOUND,
        MULTIPLE_MATCHES
    }

    @Transactional
    public SubmitResult submitRequest(String fullName) {
        List<Employee> matches = employeeRepository.findByFullNameIgnoreCase(fullName);

        if (matches.isEmpty()) {
            return SubmitResult.EMPLOYEE_NOT_FOUND;
        }
        if (matches.size() > 1) {
            return SubmitResult.MULTIPLE_MATCHES;
        }

        Employee employee = matches.get(0);

        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmployeeId(employee.getId());
        request.setEmployeeName(employee.getFullName());
        request.setRequestedAt(LocalDateTime.now());
        request.setResolved(false);
        requestRepository.save(request);

        return SubmitResult.SUCCESS;
    }

    public List<PasswordResetRequest> getPendingRequests() {
        return requestRepository.findByResolvedFalseOrderByRequestedAtDesc();
    }

    public long getPendingCount() {
        return requestRepository.countByResolvedFalse();
    }

    /**
     * Resets the requesting employee's password back to the shared default
     * password, then marks the request as resolved.
     */
    @Transactional
    public boolean resolveRequest(Long requestId) {
        Optional<PasswordResetRequest> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isEmpty()) return false;

        PasswordResetRequest request = requestOpt.get();

        Optional<User> userOpt = userRepository.findByEmployee_Id(request.getEmployeeId());
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(DEFAULT_EMPLOYEE_PASSWORD));
        userRepository.save(user);

        request.setResolved(true);
        requestRepository.save(request);

        return true;
    }
}