package com.example.PlanR.controller;

import com.example.PlanR.repository.DepartmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoutineViewController {

    private final DepartmentRepository departmentRepository;

    public RoutineViewController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/routine-builder")
    public String showRoutineBuilder(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "routinebuilder";
    }
}