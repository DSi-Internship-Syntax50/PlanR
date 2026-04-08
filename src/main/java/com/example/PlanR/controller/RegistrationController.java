package com.example.PlanR.controller;

import com.example.PlanR.dto.UserRegistrationDto;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", Role.values()); 
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      Model model) {

        // 1. Check if email exists
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        // 2. If there are validation errors, return to the form and repopulate dropdowns
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("roles", Role.values());
            return "register";
        }

        // 3. Map DTO to User Entity
        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(registrationDto.getRole());

        // 4. Fetch and set Department
        Department department = departmentRepository.findById(registrationDto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + registrationDto.getDepartmentId()));
        user.setDepartment(department);

        // 5. Save the user
        userRepository.save(user);

        // Redirect to login with success parameter
        return "redirect:/login?success=true";
    }
}
