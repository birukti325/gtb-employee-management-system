package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Attendance;
import com.sms.gtbemployeemanagementsystem.Repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> findAllWithEmployeeAndDepartment() {
        return attendanceRepository.findAllWithEmployeeAndDepartment();
    }

    public Optional<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    public List<Attendance> findByEmployeeId(Long employeeId) {
        return attendanceRepository.findByEmployee_Id(employeeId);
    }

    public List<Attendance> findByStatus(String status) {
        return attendanceRepository.findByStatus(status);
    }

    public List<Attendance> findByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    @Transactional
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void deleteById(Long id) {
        attendanceRepository.deleteById(id);
    }

    @Transactional
    public Optional<Attendance> recordLunchOut(Long attendanceId) {
        return attendanceRepository.findById(attendanceId).map(a -> {
            a.setLunchOut(LocalTime.now());
            return attendanceRepository.save(a);
        });
    }

    @Transactional
    public Optional<Attendance> recordLunchBack(Long attendanceId) {
        return attendanceRepository.findById(attendanceId).map(a -> {
            a.setLunchIn(LocalTime.now());
            return attendanceRepository.save(a);
        });
    }
}