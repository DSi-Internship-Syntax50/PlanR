package com.example.PlanR.controller;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.PlanR.dto.UserRegistrationDto;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
public class AuthController {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(DepartmentRepository departmentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("registrationForm", new UserRegistrationDto());
        model.addAttribute("departments", departmentRepository.findAll().stream()
                .filter(dept -> !"SYS".equals(dept.getShortCode()))
                .collect(Collectors.toList()));
        
        List<Role> assignableRoles = Arrays.stream(Role.values())
                .filter(role -> role != Role.SUPERADMIN)
                .collect(Collectors.toList());
        model.addAttribute("roles", assignableRoles);
        
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationForm") UserRegistrationDto registrationDto,
                               BindingResult result, Model model) {
        
        String email = registrationDto.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll().stream()
                    .filter(dept -> !"SYS".equals(dept.getShortCode()))
                    .collect(Collectors.toList()));
            List<Role> assignableRoles = Arrays.stream(Role.values())
                    .filter(role -> role != Role.SUPERADMIN)
                    .collect(Collectors.toList());
            model.addAttribute("roles", assignableRoles);
            return "register";
        }

        try {
            User user = new User();
            user.setName(registrationDto.getName());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setRole(registrationDto.getRole());

            Department department = departmentRepository.findById(registrationDto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + registrationDto.getDepartmentId()));
            user.setDepartment(department);

            userRepository.save(user);
            logger.info("New user registered: {}", email);
            return "redirect:/login?success=true";
        } catch (Exception e) {
            logger.error("Registration error", e);
            model.addAttribute("registrationError", "A system error occurred. Please try again.");
            return "register";
        }
    }
}
