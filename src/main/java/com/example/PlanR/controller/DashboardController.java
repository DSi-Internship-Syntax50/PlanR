package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.model.Room;

import java.util.List;
import com.example.PlanR.repository.UserRepository;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RoomType;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private RoomRepository roomRepository;

    private MasterRoutineRepository masterRoutineRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping({ "/", "/dashboard" })
    public String showCoordinatorCanvas(Model model) {
        model.addAttribute("pageTitle", "Coordinator's Canvas");
        return "dashboard";
    }

    @GetMapping("/canvas")
    public String showGlobalCanvas(Model model) {
        return "canvas";
    }

    @GetMapping("/schedules")
    public String showSchedulesHub(Model model) {
        return "schedules";
    }

    @GetMapping("/faculty")
    public String showFacultyHub(Model model) {
        List<User> faculties = userRepository.findByRole(Role.TEACHER);
        model.addAttribute("faculties", faculties);
        return "faculty";
    }

    @GetMapping("/students")
    public String showStudentsHub(Model model) {
        List<User> students = userRepository.findByRole(Role.STUDENT);
        model.addAttribute("students", students);
        return "students";
    }

    @GetMapping("/allocation")
    public String showAllocationHub(Model model) {
        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("rooms", rooms);
        return "allocation";
    }

    @GetMapping("/operations")
    public String showSeatPlanTool(Model model) {
        List<Room> rooms = roomRepository.findAll();
        List<com.example.PlanR.model.Department> departments = departmentRepository.findAll();
        model.addAttribute("rooms", rooms);
        model.addAttribute("departments", departments);
        return "seatplan"; // Loads seatplan.html
    }

    @GetMapping("/events")
    public String showEventBooking(Model model) {
        return "events";
    }

    @GetMapping("/analytics")
    public String showAnalytics(Model model) {
        List<MasterRoutine> routines = masterRoutineRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<Department> departments = departmentRepository.findAll();

        int totalWeeklyClasses = routines.size();

        // Active Scheduling Conflicts (routines without a room)
        long activeConflicts = routines.stream().filter(r -> r.getRoom() == null).count();

        // Overall Room utilization
        // Assuming 6 working days, 11 slots per day (81 slots)
        int totalPossibleSlots = rooms.size() * 6 * 11;
        int usedSlots = routines.stream()
                .filter(r -> r.getRoom() != null && r.getCourse() != null && r.getCourse().getSlotCount() != null)
                .mapToInt(r -> r.getCourse().getSlotCount())
                .sum();

        int overallUtilization = totalPossibleSlots == 0 ? 0
                : (int) Math.round((double) usedSlots / totalPossibleSlots * 100);

        // Weekly utilization trends (Mon to Sat)
        Map<DayOfWeek, Integer> slotsPerDay = new HashMap<>();
        for (MasterRoutine r : routines) {
            if (r.getRoom() != null && r.getCourse() != null && r.getCourse().getSlotCount() != null
                    && r.getDayOfWeek() != null) {
                slotsPerDay.put(r.getDayOfWeek(),
                        slotsPerDay.getOrDefault(r.getDayOfWeek(), 0) + r.getCourse().getSlotCount());
            }
        }

        int dailyPossibleSlots = rooms.size() * 11;
        List<Integer> utilizationTrends = new ArrayList<>();
        DayOfWeek[] days = { DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY };
        for (DayOfWeek day : days) {
            int dailyUsed = slotsPerDay.getOrDefault(day, 0);
            int util = dailyPossibleSlots == 0 ? 0 : (int) Math.round((double) dailyUsed / dailyPossibleSlots * 100);
            utilizationTrends.add(util);
        }

        // Space Allocation (Doughnut chart)
        Map<RoomType, Long> roomAllocationMap = rooms.stream()
                .filter(r -> r.getType() != null)
                .collect(Collectors.groupingBy(Room::getType, Collectors.counting()));

        List<String> allocationLabels = new ArrayList<>();
        List<Long> allocationData = new ArrayList<>();
        for (Map.Entry<RoomType, Long> entry : roomAllocationMap.entrySet()) {
            allocationLabels.add(entry.getKey().name());
            allocationData.add(entry.getValue());
        }

        // Department performance
        List<Map<String, Object>> deptPerformances = new ArrayList<>();
        for (Department dept : departments) {
            Map<String, Object> perf = new HashMap<>();
            perf.put("name", dept.getName());

            // routines for this department
            List<MasterRoutine> deptRoutines = routines.stream()
                    .filter(r -> r.getCourse() != null && dept.equals(r.getCourse().getDepartment()))
                    .collect(Collectors.toList());

            int assignedHours = (int) Math.round(deptRoutines.stream()
                    .filter(r -> r.getCourse().getSlotCount() != null)
                    .mapToInt(r -> r.getCourse().getSlotCount())
                    .sum() * 1.5);

            int overbookedHours = (int) Math.round(deptRoutines.stream()
                    .filter(r -> r.getRoom() == null && r.getCourse().getSlotCount() != null)
                    .mapToInt(r -> r.getCourse().getSlotCount())
                    .sum() * 1.5);

            int score = assignedHours == 0 ? 100
                    : (int) Math.round((double) (assignedHours - overbookedHours) / assignedHours * 100);

            perf.put("assignedHours", assignedHours);
            perf.put("overbookedHours", overbookedHours);
            perf.put("score", score);

            // Icon mapping (simplified)
            String icon = "fas fa-building text-gray-500";
            if (dept.getName() != null) {
                if (dept.getName().toLowerCase().contains("computer"))
                    icon = "fas fa-laptop-code text-blue-500";
                else if (dept.getName().toLowerCase().contains("elect"))
                    icon = "fas fa-bolt text-yellow-500";
                else if (dept.getName().toLowerCase().contains("bio"))
                    icon = "fas fa-dna text-purple-500";
                else if (dept.getName().toLowerCase().contains("phys"))
                    icon = "fas fa-atom text-blue-400";
            }
            perf.put("icon", icon);

            deptPerformances.add(perf);
        }

        model.addAttribute("totalWeeklyClasses", totalWeeklyClasses);
        model.addAttribute("activeConflicts", activeConflicts);
        model.addAttribute("overallUtilization", overallUtilization);
        model.addAttribute("utilizationTrends", utilizationTrends);
        model.addAttribute("allocationLabels", allocationLabels);
        model.addAttribute("allocationData", allocationData);
        model.addAttribute("deptPerformances", deptPerformances);

        return "analytics";
    }

    @GetMapping("/settings")
    public String showSettings(Model model) {
        return "settings";
    }
}