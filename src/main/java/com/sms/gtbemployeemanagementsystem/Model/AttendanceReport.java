package com.sms.gtbemployeemanagementsystem.Model;

public class AttendanceReport {

    private Long employeeId;
    private String employeeName;
    private int totalDays;
    private int presentDays;
    private int absentDays;
    private int lateDays;
    private int halfDays;
    private double attendancePercentage;
    private String month;
    private int year;

    public AttendanceReport() {}

    public AttendanceReport(Long employeeId, String employeeName, int totalDays,
                            int presentDays, int absentDays, int lateDays,
                            int halfDays, String month, int year) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.lateDays = lateDays;
        this.halfDays = halfDays;
        this.month = month;
        this.year = year;
        this.attendancePercentage = totalDays > 0
                ? ((double) presentDays / totalDays) * 100
                : 0;
    }

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getPresentDays() { return presentDays; }
    public void setPresentDays(int presentDays) { this.presentDays = presentDays; }

    public int getAbsentDays() { return absentDays; }
    public void setAbsentDays(int absentDays) { this.absentDays = absentDays; }

    public int getLateDays() { return lateDays; }
    public void setLateDays(int lateDays) { this.lateDays = lateDays; }

    public int getHalfDays() { return halfDays; }
    public void setHalfDays(int halfDays) { this.halfDays = halfDays; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
