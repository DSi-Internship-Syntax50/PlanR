package com.example.PlanR.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.PlanR.dto.UserRegistrationDto;
import com.example.PlanR.exception.ValidationException;
import com.example.PlanR.service.UserRegistrationService;

import jakarta.validation.Valid;

/**
 * Handles public-facing authentication (login, self-registration).
 * Registration logic is delegated to UserRegistrationService.
 */
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRegistrationService registrationService;

    public AuthController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
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
        model.addAttribute("departments", registrationService.getAssignableDepartments());
        model.addAttribute("roles", registrationService.getAssignableRoles());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationForm") UserRegistrationDto registrationDto,
            BindingResult result, Model model) {

        String email = registrationDto.getEmail().trim().toLowerCase();

        if (registrationService.isEmailTaken(email)) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (result.hasErrors()) {
            model.addAttribute("departments", registrationService.getAssignableDepartments());
            model.addAttribute("roles", registrationService.getAssignableRoles());
            return "register";
        }

        try {
            registrationService.registerUser(registrationDto);
            return "redirect:/login?success=true";
        } catch (ValidationException e) {
            result.rejectValue("email", null, e.getMessage());
            model.addAttribute("departments", registrationService.getAssignableDepartments());
            model.addAttribute("roles", registrationService.getAssignableRoles());
            return "register";
        } catch (Exception e) {
            logger.error("Registration error", e);
            model.addAttribute("registrationError", "A system error occurred. Please try again.");
            return "register";
        }
    }
}
