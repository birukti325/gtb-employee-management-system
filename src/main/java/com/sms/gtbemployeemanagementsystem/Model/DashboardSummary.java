package com.sms.gtbemployeemanagementsystem.Model;

public class DashboardSummary {

    private long totalEmployees;
    private long activeEmployees;
    private long totalDepartements;
    private long totalProjects;
    private long activeProjects;
    private long pendingLeaveRequests;
    private long todayPresentCount;
    private long todayAbsentCount;
    private double averageAttendancePercentage;

    public DashboardSummary() {}

    // Getters and Setters
    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(long activeEmployees) { this.activeEmployees = activeEmployees; }

    public long getTotalDepartements() { return totalDepartements; }
    public void setTotalDepartements(long totalDepartements) { this.totalDepartements = totalDepartements; }

    public long getTotalProjects() { return totalProjects; }
    public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }

    public long getActiveProjects() { return activeProjects; }
    public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }

    public long getPendingLeaveRequests() { return pendingLeaveRequests; }
    public void setPendingLeaveRequests(long pendingLeaveRequests) { this.pendingLeaveRequests = pendingLeaveRequests; }

    public long getTodayPresentCount() { return todayPresentCount; }
    public void setTodayPresentCount(long todayPresentCount) { this.todayPresentCount = todayPresentCount; }

    public long getTodayAbsentCount() { return todayAbsentCount; }
    public void setTodayAbsentCount(long todayAbsentCount) { this.todayAbsentCount = todayAbsentCount; }

    public double getAverageAttendancePercentage() { return averageAttendancePercentage; }
    public void setAverageAttendancePercentage(double averageAttendancePercentage) {
        this.averageAttendancePercentage = averageAttendancePercentage;
    }
}
