package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoutineViewController {

    @GetMapping("/routine-builder")
    public String showRoutineBuilder() {
        // This tells Spring Boot to look for routinebuilder.html in the templates
        // folder
        return "routinebuilder";
    }
}