package com.example.PlanR.controller;

import com.example.PlanR.dto.UserRegistrationDto;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
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
        logger.info("Displaying registration form");
        model.addAttribute("registrationForm", new UserRegistrationDto());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", Role.values()); 
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("registrationForm") UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      Model model) {
        
        String email = registrationDto.getEmail().trim().toLowerCase();
        logger.info("Attempting to register account with email: {}", email);

        // 1. Check if email exists
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Registration failed: Email {} already exists", email);
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        // 2. If there are validation errors, return to the form and repopulate dropdowns
        if (result.hasErrors()) {
            logger.error("Registration failed: {} validation errors found", result.getErrorCount());
            result.getAllErrors().forEach(err -> logger.error("Validation error: {}", err.getDefaultMessage()));
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("roles", Role.values());
            return "register";
        }

        try {
            // 3. Map DTO to User Entity
            User user = new User();
            user.setName(registrationDto.getName());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setRole(registrationDto.getRole());

            // 4. Fetch and set Department
            Department department = departmentRepository.findById(registrationDto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + registrationDto.getDepartmentId()));
            user.setDepartment(department);

            // 5. Save the user
            userRepository.save(user);
            logger.info("User {} registered successfully with role {}", email, user.getRole());

            // Redirect to login with success parameter
            return "redirect:/login?success=true";
        } catch (Exception e) {
            logger.error("Critical error during registration for {}: {}", email, e.getMessage(), e);
            model.addAttribute("registrationError", "A system error occurred. Please try again.");
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("roles", Role.values());
            return "register";
        }
    }
}
