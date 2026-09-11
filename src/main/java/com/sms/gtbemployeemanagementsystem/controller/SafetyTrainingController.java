package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Safety_training;
import com.sms.gtbemployeemanagementsystem.Service.SafetyTrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SafetyTrainingController {

    @Autowired
    private SafetyTrainingService service;

    public List<Safety_training> getAll() {
        return service.findAll();
    }

    public Optional<Safety_training> getById(Long id) {
        return service.findById(id);
    }

    public List<Safety_training> getByEmployee(Long employeeId) {
        return service.findByEmployeeId(employeeId);
    }

    public List<Safety_training> getByStatus(String status) {
        return service.findByStatus(status);
    }

    public Safety_training create(Safety_training training) {
        return service.save(training);
    }

    public Safety_training update(Long id, Safety_training training) {
        training.setId(id);
        return service.save(training);
    }

    public void delete(Long id) {
        service.deleteById(id);
    }
}