package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.PlanR.dto.AnalyticsSnapshot;
import com.example.PlanR.service.AnalyticsService;

/**
 * Handles the analytics dashboard page.
 * Delegates all business logic to AnalyticsService.
 */
@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public String showAnalytics(Model model) {
        AnalyticsSnapshot analytics = analyticsService.computeAnalytics();
        model.addAttribute("analytics", analytics);
        return "analytics";
    }
}
