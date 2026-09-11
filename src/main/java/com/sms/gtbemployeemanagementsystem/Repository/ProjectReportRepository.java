package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.ProjectReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectReportRepository extends JpaRepository<ProjectReport, Long> {
    List<ProjectReport> findByProjectIdOrderBySubmittedDateDesc(Long projectId);
}