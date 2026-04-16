package com.example.PlanR.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.UserRepository;

/**
 * Handles faculty and student listing pages.
 * Extracted from the monolithic DashboardController.
 */
@Controller
public class FacultyController {

    private final UserRepository userRepository;

    public FacultyController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasRole('SUPERADMIN')")
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
}
