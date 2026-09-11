package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Safety_training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyTrainingRepository extends JpaRepository<Safety_training, Long> {
    List<Safety_training> findByEmployee_Id(Long employeeId);
    List<Safety_training> findByStatus(String status);
}
