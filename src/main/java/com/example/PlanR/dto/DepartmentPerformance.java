package com.example.PlanR.dto;

/**
 * DTO representing a department's scheduling performance metrics.
 * Used by the AnalyticsService and rendered in the analytics dashboard.
 */
public class DepartmentPerformance {

    public String name;
    public int assignedHours;
    public int overbookedHours;
    public int score;
    public String icon;

    public DepartmentPerformance() {}

    public DepartmentPerformance(String name, int assignedHours, int overbookedHours, int score, String icon) {
        this.name = name;
        this.assignedHours = assignedHours;
        this.overbookedHours = overbookedHours;
        this.score = score;
        this.icon = icon;
    }
}
