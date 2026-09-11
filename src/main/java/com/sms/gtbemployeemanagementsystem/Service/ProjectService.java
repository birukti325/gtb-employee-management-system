package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Projects;
import com.sms.gtbemployeemanagementsystem.Entity.Project_assignment;
import com.sms.gtbemployeemanagementsystem.Repository.ProjectRepository;
import com.sms.gtbemployeemanagementsystem.Repository.ProjectAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    public List<Projects> findAll() {
        return projectRepository.findAll();
    }

    public Optional<Projects> findById(Long id) {
        return projectRepository.findById(id);
    }

    public List<Projects> findByStatus(String status) {
        return projectRepository.findByStatus(status);
    }

    @Transactional
    public Projects save(Projects project) {
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }

    public List<Project_assignment> findAssignmentsByProjectId(Long projectId) {
        return projectAssignmentRepository.findByProject_Id(projectId);
    }

    @Transactional
    public Project_assignment saveAssignment(Project_assignment assignment) {
        return projectAssignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteAssignmentById(Long id) {
        projectAssignmentRepository.deleteById(id);
    }
}
