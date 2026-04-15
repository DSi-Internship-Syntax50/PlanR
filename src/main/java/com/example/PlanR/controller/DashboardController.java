package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.model.Room;
import java.util.List;
import com.example.PlanR.repository.UserRepository;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;

@Controller
public class DashboardController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

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
        return "allocation"; // Loads allocation.html
    }

    @GetMapping("/operations")
    public String showSeatPlanTool(Model model) {
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