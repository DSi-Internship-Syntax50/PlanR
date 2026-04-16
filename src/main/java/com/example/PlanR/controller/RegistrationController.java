package com.example.PlanR.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.PlanR.dto.UserRegistrationDto;
import com.example.PlanR.exception.ValidationException;
import com.example.PlanR.service.UserRegistrationService;

import jakarta.validation.Valid;

/**
 * Admin-only user creation controller.
 * Registration logic is delegated to UserRegistrationService.
 */
@Controller
@RequestMapping("/admin/users")
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final UserRegistrationService registrationService;

    public RegistrationController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/create")
    public String showRegistrationForm(Model model) {
        logger.info("Displaying admin user creation form");
        model.addAttribute("registrationForm", new UserRegistrationDto());
        model.addAttribute("departments", registrationService.getAssignableDepartments());
        model.addAttribute("roles", registrationService.getAssignableRoles());
        return "register";
    }

    @PostMapping("/create")
    public String registerUserAccount(@Valid @ModelAttribute("registrationForm") UserRegistrationDto registrationDto,
            BindingResult result, Model model) {

        String email = registrationDto.getEmail().trim().toLowerCase();
        logger.info("Admin attempting to create account with email: {}", email);

        if (registrationService.isEmailTaken(email)) {
            logger.warn("Creation failed: Email {} already exists", email);
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (result.hasErrors()) {
            model.addAttribute("departments", registrationService.getAssignableDepartments());
            model.addAttribute("roles", registrationService.getAssignableRoles());
            return "register";
        }

        try {
            registrationService.registerUser(registrationDto);
            logger.info("User {} created successfully by admin", email);
            return "redirect:/admin/users/create?success=true";
        } catch (ValidationException e) {
            result.rejectValue("email", null, e.getMessage());
            model.addAttribute("departments", registrationService.getAssignableDepartments());
            model.addAttribute("roles", registrationService.getAssignableRoles());
            return "register";
        } catch (Exception e) {
            logger.error("Critical error during user creation for {}: {}", email, e.getMessage(), e);
            model.addAttribute("registrationError", "A system error occurred. Please try again.");
            model.addAttribute("departments", registrationService.getAssignableDepartments());
            model.addAttribute("roles", registrationService.getAssignableRoles());
            return "register";
        }
    }
}