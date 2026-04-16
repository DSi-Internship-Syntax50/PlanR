package com.example.PlanR.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles simple page-rendering routes that require no business logic.
 * Extracted from the monolithic DashboardController.
 */
@Controller
public class NavigationController {

    @GetMapping({ "/", "/dashboard" })
    public String showCoordinatorCanvas(Model model) {
        model.addAttribute("pageTitle", "Coordinator's Canvas");
        return "dashboard";
    }

    @GetMapping("/canvas")
    public String showGlobalCanvas() {
        return "canvas";
    }

    @GetMapping("/schedules")
    public String showSchedulesHub() {
        return "schedules";
    }

    @GetMapping("/events")
    public String showEventBooking() {
        return "events";
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String showSettings() {
        return "settings";
    }
}
