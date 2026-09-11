package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Equipment;
import com.sms.gtbemployeemanagementsystem.Entity.Equipment_assignment;
import com.sms.gtbemployeemanagementsystem.Repository.EquipmentAssignmentRepository;
import com.sms.gtbemployeemanagementsystem.Repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentAssignmentRepository assignmentRepository;

    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }

    public Optional<Equipment> findById(Long id) {
        return equipmentRepository.findById(id);
    }

    public List<Equipment> findByStatus(String status) {
        return equipmentRepository.findByStatus(status);
    }

    @Transactional
    public Equipment save(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public void deleteById(Long id) {
        equipmentRepository.deleteById(id);
    }

    // ── Assignments ───────────────────────────────────────

    public List<Equipment_assignment> findAssignmentsByEquipmentId(Long equipmentId) {
        return assignmentRepository.findByEquipment_Id(equipmentId);
    }

    public List<Equipment_assignment> findAssignmentsByEmployeeId(Long employeeId) {
        return assignmentRepository.findByEmployee_Id(employeeId);
    }

    @Transactional
    public Equipment_assignment saveAssignment(Equipment_assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteAssignmentById(Long id) {
        assignmentRepository.deleteById(id);
    }
}
