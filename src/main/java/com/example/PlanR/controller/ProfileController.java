package com.example.PlanR.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.PlanR.dto.ProfileUpdateDto;
import com.example.PlanR.exception.EntityNotFoundException;
import com.example.PlanR.model.User;
import com.example.PlanR.repository.UserRepository;
import com.example.PlanR.service.UserProfileService;

/**
 * Handles user profile viewing and editing.
 * Business logic delegated to UserProfileService.
 */
@Controller
public class ProfileController {

    private final UserProfileService profileService;
    private final UserRepository userRepository;

    public ProfileController(UserProfileService profileService, UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-profile")
    public String myProfile(Model model) {
        User user = profileService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("isSuperAdmin", profileService.isCurrentUserSuperAdmin());
        model.addAttribute("isViewingSelf", true);
        return "profile";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String listUsers(Model model) {
        if (!profileService.isCurrentUserSuperAdmin()) {
            return "redirect:/dashboard";
        }

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @GetMapping("/admin/profile/{userId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String viewUserProfile(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        if (!profileService.isCurrentUserSuperAdmin()) {
            return "redirect:/dashboard";
        }

        try {
            User targetUser = profileService.getUserById(userId);
            model.addAttribute("user", targetUser);
            model.addAttribute("isSuperAdmin", true);
            model.addAttribute("isViewingSelf", false);
            return "profile";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/my-profile/update/{userId}")
    public String updateProfile(
            @PathVariable Long userId,
            @ModelAttribute ProfileUpdateDto dto,
            RedirectAttributes redirectAttributes) {

        if (!profileService.isCurrentUserSuperAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Only SUPERADMIN can edit profiles.");
            return "redirect:/my-profile";
        }

        try {
            profileService.updateProfile(userId, dto);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");

            User targetUser = profileService.getUserById(userId);
            if (profileService.isViewingSelf(targetUser)) {
                return "redirect:/my-profile";
            }
            return "redirect:/admin/users";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
    }
}
