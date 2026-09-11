package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployee_Id(Long employeeId);

    List<Attendance> findByStatus(String status);

    List<Attendance> findByDate(LocalDate date);

    List<Attendance> findByEmployee_IdAndDate(Long employeeId, LocalDate date);

    // Eagerly fetches employee and department together so the JavaFX controller
    // can safely call employee.getDepartmentName() after the session closes.
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e JOIN FETCH e.department")
    List<Attendance> findAllWithEmployeeAndDepartment();
}