package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoutineViewController {

    @GetMapping("/routine-builder")
    public String showRoutineBuilder() {
        // This tells Spring Boot to look for routinebuilder.html in the templates folder
        return "routinebuilder";
    }

    // Process the form submission
    @PostMapping("/generate-routine")
    public String generateRoutine(
            @RequestParam("semester") int semester,
            @RequestParam("theoryCount") int theoryCount,
            @RequestParam("labCount") int labCount,
            Model model) {

        // --- MATH & LOGIC ---
        // 1 Lab = 3 Theory Slots
        int totalTheorySlots = theoryCount * 1;
        int totalLabSlots = labCount * 3;
        int totalSlotsRequired = totalTheorySlots + totalLabSlots;

        String successMessage = String.format(
            "Success! Generated skeleton for Semester %d. Allocated %d Theory slots and %d Lab slots (Total: %d slots).", 
            semester, totalTheorySlots, totalLabSlots, totalSlotsRequired
        );
        
        model.addAttribute("message", successMessage);
        
        // Return the same view so the user sees the success message
        return "routinebuilder";
    }
}