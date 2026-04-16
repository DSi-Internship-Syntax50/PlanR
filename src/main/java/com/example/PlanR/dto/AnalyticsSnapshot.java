package com.example.PlanR.dto;

import java.util.List;

/**
 * Immutable snapshot of all analytics data computed by AnalyticsService.
 * Passed as a single model attribute to the analytics template.
 */
public class AnalyticsSnapshot {

    public int totalWeeklyClasses;
    public long activeConflicts;
    public int overallUtilization;
    public List<Integer> utilizationTrends;
    public List<String> allocationLabels;
    public List<Long> allocationData;
    public List<DepartmentPerformance> deptPerformances;

    public AnalyticsSnapshot() {}

    public AnalyticsSnapshot(int totalWeeklyClasses, long activeConflicts, int overallUtilization,
                             List<Integer> utilizationTrends, List<String> allocationLabels,
                             List<Long> allocationData, List<DepartmentPerformance> deptPerformances) {
        this.totalWeeklyClasses = totalWeeklyClasses;
        this.activeConflicts = activeConflicts;
        this.overallUtilization = overallUtilization;
        this.utilizationTrends = utilizationTrends;
        this.allocationLabels = allocationLabels;
        this.allocationData = allocationData;
        this.deptPerformances = deptPerformances;
    }
}
