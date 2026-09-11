package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.EmployeeCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeCredentialRepository extends JpaRepository<EmployeeCredential, Integer> {
    // Custom query to look up the ID and Role combination
    Optional<EmployeeCredential> findByEmployeeIdAndRole(Integer employeeId, String role);
}