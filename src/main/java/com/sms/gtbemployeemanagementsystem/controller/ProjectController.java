package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.Projects;
import com.sms.gtbemployeemanagementsystem.Entity.Project_assignment;
import com.sms.gtbemployeemanagementsystem.Service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    public List<Projects> getAll() {
        return projectService.findAll();
    }

    public Optional<Projects> getById(Long id) {
        return projectService.findById(id);
    }

    public List<Projects> getByStatus(String status) {
        return projectService.findByStatus(status);
    }

    public Projects create(Projects project) {
        return projectService.save(project);
    }

    public Projects update(Long id, Projects project) {
        project.setId(id);
        return projectService.save(project);
    }

    public void delete(Long id) {
        projectService.deleteById(id);
    }

    // ── Project Assignments ───────────────────────────────

    public List<Project_assignment> getAssignments(Long projectId) {
        return projectService.findAssignmentsByProjectId(projectId);
    }

    public Project_assignment addAssignment(Long projectId, Project_assignment assignment) {
        assignment.setProjectId(projectId);
        return projectService.saveAssignment(assignment);
    }

    public void removeAssignment(Long assignmentId) {
        projectService.deleteAssignmentById(assignmentId);
    }
}