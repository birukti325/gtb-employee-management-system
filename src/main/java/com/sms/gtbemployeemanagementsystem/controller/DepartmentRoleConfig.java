package com.sms.gtbemployeemanagementsystem.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central place defining which roles belong to which department.
 * Used by DepartmentRolesController (popup) and DirectoryController (Edit dialog)
 * so both stay in sync.
 */
public class DepartmentRoleConfig {

    public static final Map<String, List<String>> DEPARTMENT_ROLES = new LinkedHashMap<>();
    static {
        DEPARTMENT_ROLES.put("Management", List.of("Deputy Manager", "Project Manager"));
        DEPARTMENT_ROLES.put("Engineering/Technical", List.of("Engineer", "Surveyor"));
        DEPARTMENT_ROLES.put("Construction/Site Operations", List.of("Foreman", "Welder", "Painter"));
        DEPARTMENT_ROLES.put("Project Management/Coordination", List.of("Coordinator"));
        DEPARTMENT_ROLES.put("Finance", List.of("Accountant"));
        DEPARTMENT_ROLES.put("Human Resources", List.of("HR"));
        DEPARTMENT_ROLES.put("Administration/General Services",
                List.of("Administration Manager", "Driver", "Guard", "Cleaner", "Copier"));
    }

    public static List<String> getRolesFor(String departmentName) {
        return DEPARTMENT_ROLES.getOrDefault(departmentName, List.of());
    }

    private DepartmentRoleConfig() {}
}