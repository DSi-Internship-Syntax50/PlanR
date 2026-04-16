package com.example.PlanR.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.service.UserService;

/**
 * Handles faculty and student listing pages.
 * Extracted from the monolithic DashboardController.
 */
@Controller
public class FacultyController {

    private final UserService userService;

    public FacultyController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String showFacultyHub(Model model) {
        List<User> faculties = userService.findUsersByRole(Role.TEACHER);
        model.addAttribute("faculties", faculties);
        return "faculty";
    }

    @GetMapping("/students")
    public String showStudentsHub(Model model) {
        List<User> students = userService.findUsersByRole(Role.STUDENT);
        model.addAttribute("students", students);
        return "students";
    }
}
