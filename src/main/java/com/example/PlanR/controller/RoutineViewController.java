package com.example.PlanR.controller;

import com.example.PlanR.service.DepartmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoutineViewController {

    private final DepartmentService departmentService;

    public RoutineViewController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/routine-builder")
    public String showRoutineBuilder(Model model) {
        model.addAttribute("departments", departmentService.findAllDepartments());
        return "routinebuilder";
    }
}