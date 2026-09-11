package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Resolves: UserDetailsServiceImpl, AuthService, etc.
    Optional<User> findByUsername(String username);

    // Resolves: EmployeeAccountInfoController (Spring Data JPA convention for nested property employee.id)
    Optional<User> findByEmployeeId(Long employeeId);

    // Alternative alias if your code explicitly calls findByEmployee_Id
    default Optional<User> findByEmployee_Id(Long employeeId) {
        return findByEmployeeId(employeeId);
    }

    // Resolves: DashboardOverviewController active admin count
    @Query("SELECT COUNT(u) FROM User u WHERE UPPER(u.role) = 'ADMIN' AND u.employee IS NOT NULL AND u.employee.active = true")
    long countActiveAdminUsers();
}