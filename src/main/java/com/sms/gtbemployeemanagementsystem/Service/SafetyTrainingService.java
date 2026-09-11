package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Safety_training;
import com.sms.gtbemployeemanagementsystem.Repository.SafetyTrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SafetyTrainingService {

    @Autowired
    private SafetyTrainingRepository repository;

    public List<Safety_training> findAll() {
        return repository.findAll();
    }

    public Optional<Safety_training> findById(Long id) {
        return repository.findById(id);
    }

    public List<Safety_training> findByEmployeeId(Long employeeId) {
        return repository.findByEmployee_Id(employeeId);
    }

    public List<Safety_training> findByStatus(String status) {
        return repository.findByStatus(status);
    }

    @Transactional
    public Safety_training save(Safety_training training) {
        return repository.save(training);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
