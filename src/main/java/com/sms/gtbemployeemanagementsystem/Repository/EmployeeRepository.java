package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByDepartmentIdAndActiveTrue(Long departmentId);
    long countByDepartmentIdAndActiveTrue(Long departmentId);

    @Query(value = "SELECT COUNT(*) FROM employees WHERE UPPER(TRIM(status)) = 'ADMIN' AND active = true", nativeQuery = true)
    long countActiveAdminsNative();

    @Query("SELECT COUNT(e) FROM Employee e WHERE LOWER(e.status) = LOWER(:status) AND e.active = true")
    long countByStatusIgnoreCaseAndActiveTrue(String status);

    // 1. Fetch ALL active employees (For Directory)
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.active = true ORDER BY e.id ASC")
    List<Employee> findAllActiveWithDepartment();

    // 2. Fetch Former Staff (Inactive employees, excluding soft-deleted test records)
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department " +
            "WHERE e.active = false AND (e.status IS NULL OR UPPER(e.status) != 'DELETED') " +
            "ORDER BY e.id ASC")
    List<Employee> findAllInactiveWithDepartment();

    // 3. Fetch ALL employees regardless of status
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department ORDER BY e.id ASC")
    List<Employee> findAllWithDepartment();

    List<Employee> findByActiveFalseOrderByIdDesc();

    List<Employee> findByFullNameIgnoreCase(String fullName);

    long countByActiveTrue();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.createdAt > :date AND e.active = true")
    long countByCreatedAtAfterAndActiveTrue(@Param("date") LocalDate date);

    @Query("SELECT e FROM Employee e WHERE LOWER(TRIM(e.fullName)) = LOWER(TRIM(:fullName)) " +
            "AND LOWER(TRIM(e.department.name)) = LOWER(TRIM(:deptName))")
    Optional<Employee> findByFullNameAndDepartmentName(
            @Param("fullName") String fullName,
            @Param("deptName") String deptName
    );
}