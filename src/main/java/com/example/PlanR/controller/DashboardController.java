package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

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