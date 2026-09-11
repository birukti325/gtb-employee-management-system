package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Equipment;
import com.sms.gtbemployeemanagementsystem.Entity.Equipment_assignment;
import com.sms.gtbemployeemanagementsystem.Service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    public List<Equipment> getAll() {
        return equipmentService.findAll();
    }

    public Optional<Equipment> getById(Long id) {
        return equipmentService.findById(id);
    }

    public List<Equipment> getByStatus(String status) {
        return equipmentService.findByStatus(status);
    }

    public Equipment create(Equipment equipment) {
        return equipmentService.save(equipment);
    }

    public Equipment update(Long id, Equipment equipment) {
        equipment.setId(id);
        return equipmentService.save(equipment);
    }

    public void delete(Long id) {
        equipmentService.deleteById(id);
    }

    // ── Assignments ───────────────────────────────────────

    public List<Equipment_assignment> getAssignments(Long equipmentId) {
        return equipmentService.findAssignmentsByEquipmentId(equipmentId);
    }

    public Equipment_assignment addAssignment(Long equipmentId, Equipment_assignment assignment) {
        if (assignment.getEquipment() == null) {
            Equipment e = new Equipment();
            e.setId(equipmentId);
            assignment.setEquipment(e);
        }
        return equipmentService.saveAssignment(assignment);
    }

    public void removeAssignment(Long assignmentId) {
        equipmentService.deleteAssignmentById(assignmentId);
    }
}