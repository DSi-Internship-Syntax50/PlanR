package com.example.PlanR.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.PlanR.dto.AnalyticsSnapshot;
import com.example.PlanR.dto.DepartmentPerformance;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RoomType;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;

/**
 * Service encapsulating all analytics business logic.
 * Extracted from DashboardController.showAnalytics() to enable
 * unit testing, reuse, and adherence to SRP.
 */
@Service
public class AnalyticsService {

    private final MasterRoutineRepository routineRepository;
    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;

    public AnalyticsService(MasterRoutineRepository routineRepository,
                            RoomRepository roomRepository,
                            DepartmentRepository departmentRepository) {
        this.routineRepository = routineRepository;
        this.roomRepository = roomRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Computes a full analytics snapshot for the dashboard.
     */
    public AnalyticsSnapshot computeAnalytics() {
        List<MasterRoutine> routines = routineRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<Department> departments = departmentRepository.findAll();

        int totalWeeklyClasses = routines.size();
        long activeConflicts = computeActiveConflicts(routines);
        int overallUtilization = computeOverallUtilization(routines, rooms);
        List<Integer> utilizationTrends = computeUtilizationTrends(routines, rooms);
        List<String> allocationLabels = new ArrayList<>();
        List<Long> allocationData = new ArrayList<>();
        computeSpaceAllocation(rooms, allocationLabels, allocationData);
        List<DepartmentPerformance> deptPerformances = computeDepartmentPerformances(departments, routines);

        return new AnalyticsSnapshot(
                totalWeeklyClasses, activeConflicts, overallUtilization,
                utilizationTrends, allocationLabels, allocationData, deptPerformances);
    }

    private long computeActiveConflicts(List<MasterRoutine> routines) {
        return routines.stream().filter(r -> r.getRoom() == null).count();
    }

    private int computeOverallUtilization(List<MasterRoutine> routines, List<Room> rooms) {
        // Assuming 6 working days, 11 slots per day
        int totalPossibleSlots = rooms.size() * 6 * 11;
        int usedSlots = routines.stream()
                .filter(r -> r.getRoom() != null && r.getCourse() != null && r.getCourse().getSlotCount() != null)
                .mapToInt(r -> r.getCourse().getSlotCount())
                .sum();

        return totalPossibleSlots == 0 ? 0
                : (int) Math.round((double) usedSlots / totalPossibleSlots * 100);
    }

    private List<Integer> computeUtilizationTrends(List<MasterRoutine> routines, List<Room> rooms) {
        Map<DayOfWeek, Integer> slotsPerDay = new HashMap<>();
        for (MasterRoutine r : routines) {
            if (r.getRoom() != null && r.getCourse() != null && r.getCourse().getSlotCount() != null
                    && r.getDayOfWeek() != null) {
                slotsPerDay.put(r.getDayOfWeek(),
                        slotsPerDay.getOrDefault(r.getDayOfWeek(), 0) + r.getCourse().getSlotCount());
            }
        }

        int dailyPossibleSlots = rooms.size() * 11;
        List<Integer> trends = new ArrayList<>();
        DayOfWeek[] days = { DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY };

        for (DayOfWeek day : days) {
            int dailyUsed = slotsPerDay.getOrDefault(day, 0);
            int util = dailyPossibleSlots == 0 ? 0
                    : (int) Math.round((double) dailyUsed / dailyPossibleSlots * 100);
            trends.add(util);
        }
        return trends;
    }

    private void computeSpaceAllocation(List<Room> rooms, List<String> labels, List<Long> data) {
        Map<RoomType, Long> roomAllocationMap = rooms.stream()
                .filter(r -> r.getType() != null)
                .collect(Collectors.groupingBy(Room::getType, Collectors.counting()));

        for (Map.Entry<RoomType, Long> entry : roomAllocationMap.entrySet()) {
            labels.add(entry.getKey().name());
            data.add(entry.getValue());
        }
    }

    private List<DepartmentPerformance> computeDepartmentPerformances(
            List<Department> departments, List<MasterRoutine> routines) {

        List<DepartmentPerformance> performances = new ArrayList<>();

        for (Department dept : departments) {
            List<MasterRoutine> deptRoutines = routines.stream()
                    .filter(r -> r.getCourse() != null && 
                                 r.getCourse().getDepartment() != null && 
                                 dept.getId().equals(r.getCourse().getDepartment().getId()))
                    .collect(Collectors.toList());

            int assignedHours = deptRoutines.stream()
                    .filter(r -> r.getCourse().getSlotCount() != null)
                    .mapToInt(r -> r.getCourse().getSlotCount())
                    .sum();

            int overbookedHours = deptRoutines.stream()
                    .filter(r -> r.getRoom() == null && r.getCourse().getSlotCount() != null)
                    .mapToInt(r -> r.getCourse().getSlotCount())
                    .sum();

            int score = assignedHours == 0 ? 100
                    : (int) Math.round((double) (assignedHours - overbookedHours) / assignedHours * 100);

            String icon = resolveDepartmentIcon(dept);

            performances.add(new DepartmentPerformance(dept.getName(), assignedHours, overbookedHours, score, icon));
        }
        return performances;
    }

    private String resolveDepartmentIcon(Department dept) {
        String icon = "fas fa-building text-gray-500";
        if (dept.getName() != null) {
            String nameLower = dept.getName().toLowerCase();
            if (nameLower.contains("computer"))
                icon = "fas fa-laptop-code text-blue-500";
            else if (nameLower.contains("elect"))
                icon = "fas fa-bolt text-yellow-500";
            else if (nameLower.contains("bio"))
                icon = "fas fa-dna text-purple-500";
            else if (nameLower.contains("phys"))
                icon = "fas fa-atom text-blue-400";
        }
        return icon;
    }
}
