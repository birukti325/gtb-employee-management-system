package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Project_assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<Project_assignment, Long> {

    List<Project_assignment> findByProject_Id(Long projectId);

    List<Project_assignment> findByEmployee_Id(Long employeeId);

}