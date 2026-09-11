package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Equipment_assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentAssignmentRepository extends JpaRepository<Equipment_assignment, Long> {
    List<Equipment_assignment> findByEmployee_Id(Long employeeId);
    List<Equipment_assignment> findByEquipment_Id(Long equipmentId);
    List<Equipment_assignment> findByStatus(String status);
}
