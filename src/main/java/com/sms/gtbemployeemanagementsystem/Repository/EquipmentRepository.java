package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatus(String status);
    List<Equipment> findByCategory(String category);
}
