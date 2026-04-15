package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.model.Room;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private com.example.PlanR.repository.DepartmentRepository departmentRepository;

    @GetMapping({"/", "/dashboard"})
    public String showCoordinatorCanvas(Model model) {
        // You can pass dynamic data here later (e.g., active conflicts, schedule health)
        model.addAttribute("pageTitle", "Coordinator's Canvas");
        return "dashboard"; // Loads dashboard.html
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
        return "faculty"; 
    }

    @GetMapping("/allocation")
    public String showAllocationHub(Model model) {
        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("rooms", rooms);
        return "allocation"; // Loads allocation.html
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
        return "events"; // Loads events.html
    }

    @GetMapping("/analytics")
    public String showAnalytics(Model model) {
        return "analytics"; // Loads analytics.html
    }

    @GetMapping("/settings")
    public String showSettings(Model model) {
        return "settings"; // Loads settings.html
    }
}