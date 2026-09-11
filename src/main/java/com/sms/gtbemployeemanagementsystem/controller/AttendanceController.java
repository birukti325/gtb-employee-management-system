package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Attendance;
import com.sms.gtbemployeemanagementsystem.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    public List<Attendance> getAll() {
        return attendanceService.findAll();
    }

    public Optional<Attendance> getById(Long id) {
        return attendanceService.findById(id);
    }

    public List<Attendance> getByEmployee(Long employeeId) {
        return attendanceService.findByEmployeeId(employeeId);
    }

    public Attendance create(Attendance attendance) {
        return attendanceService.save(attendance);
    }

    public Attendance update(Long id, Attendance attendance) {
        attendance.setId(id);
        return attendanceService.save(attendance);
    }

    public Optional<Attendance> lunchOut(Long id) {
        return attendanceService.recordLunchOut(id);
    }

    public Optional<Attendance> lunchIn(Long id) {
        return attendanceService.recordLunchBack(id);
    }

    public void delete(Long id) {
        attendanceService.deleteById(id);
    }
}
