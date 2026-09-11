package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Projects, Long> {
    List<Projects> findByStatus(String status);

    @Query("SELECT p FROM Projects p LEFT JOIN FETCH p.department")
    List<Projects> findAllWithDepartment();
}