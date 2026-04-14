package com.example.PlanR.controller;

import com.example.PlanR.dto.UserRegistrationDto;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.UserRepository;
import com.example.PlanR.service.NotificationService;
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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users") // Enterprise-grade URL structure
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public RegistrationController(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @GetMapping("/create")
    public String showRegistrationForm(Model model) {
        logger.info("Displaying admin user creation form");
        model.addAttribute("registrationForm", new UserRegistrationDto());
        model.addAttribute("departments", departmentRepository.findAll().stream()
                .filter(dept -> !"SYS".equals(dept.getShortCode()))
                .collect(Collectors.toList()));

        // Ensure SUPERADMIN is not an assignable role in the dropdown
        List<Role> assignableRoles = Arrays.stream(Role.values())
                .filter(role -> role != Role.SUPERADMIN)
                .collect(Collectors.toList());
        model.addAttribute("roles", assignableRoles);

        return "register"; // Keeping the existing thymeleaf template name for now
    }

    @PostMapping("/create")
    public String registerUserAccount(@Valid @ModelAttribute("registrationForm") UserRegistrationDto registrationDto,
            BindingResult result,
            Model model) {

        String email = registrationDto.getEmail().trim().toLowerCase();
        logger.info("Admin attempting to create account with email: {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Creation failed: Email {} already exists", email);
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll().stream()
                    .filter(dept -> !"SYS".equals(dept.getShortCode()))
                    .collect(Collectors.toList()));
            model.addAttribute("roles",
                    Arrays.stream(Role.values()).filter(r -> r != Role.SUPERADMIN).collect(Collectors.toList()));
            return "register";
        }

        try {
            User user = new User();
            user.setName(registrationDto.getName());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setRole(registrationDto.getRole());

            Department department = departmentRepository.findById(registrationDto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid department Id:" + registrationDto.getDepartmentId()));
            user.setDepartment(department);

            userRepository.save(user);
            logger.info("User {} created successfully by admin with role {}", email, user.getRole());

            // Send welcome notification
            notificationService.createNotification(user, "Welcome to PlanR", "Your account has been initialized. You can now access your schedules and seat plans.");

            // Redirect back to the form with a success parameter
            return "redirect:/admin/users/create?success=true";

        } catch (Exception e) {
            logger.error("Critical error during user creation for {}: {}", email, e.getMessage(), e);
            model.addAttribute("registrationError", "A system error occurred. Please try again.");
            model.addAttribute("departments", departmentRepository.findAll().stream()
                    .filter(dept -> !"SYS".equals(dept.getShortCode()))
                    .collect(Collectors.toList()));
            model.addAttribute("roles",
                    Arrays.stream(Role.values()).filter(r -> r != Role.SUPERADMIN).collect(Collectors.toList()));
            return "register";
        }
    }
}