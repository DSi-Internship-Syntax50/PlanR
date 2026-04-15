package com.example.PlanR.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.UserRepository;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-profile")
    @Transactional(readOnly = true)
    public String myProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        boolean isSuperAdmin = false;

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                isSuperAdmin = user.getRole() == Role.SUPERADMIN;
                // Force-initialize lazy department
                if (user.getDepartment() != null) {
                    user.getDepartment().getName();
                }
            }
        }

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("isSuperAdmin", isSuperAdmin);
        model.addAttribute("isViewingSelf", true);
        return "profile";
    }

    // --- Admin User Management ---

    @GetMapping("/admin/users")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String listUsers(Model model) {
        // Double check role manually just in case, though SecurityConfig handles it
        if (!isCurrentUserSuperAdmin()) {
            return "redirect:/dashboard";
        }

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @GetMapping("/admin/profile/{userId}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String viewUserProfile(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        if (!isCurrentUserSuperAdmin()) {
            return "redirect:/dashboard";
        }

        Optional<User> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        User targetUser = targetUserOpt.get();
        // Force-initialize lazy department
        if (targetUser.getDepartment() != null) {
            targetUser.getDepartment().getName();
        }

        model.addAttribute("user", targetUser);
        model.addAttribute("isSuperAdmin", true); // The viewer IS a superadmin
        model.addAttribute("isViewingSelf", false);
        return "profile";
    }

    @PostMapping("/my-profile/update/{userId}")
    @Transactional
    public String updateProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String currentBatch,
            @RequestParam(required = false) String admittedSemester,
            @RequestParam(required = false) String enrollmentStatus,
            @RequestParam(required = false) String labClearanceStatus,
            @RequestParam(required = false) Integer seniorityRank,
            @RequestParam(required = false) Boolean isCr,
            @RequestParam(required = false) Role role,
            RedirectAttributes redirectAttributes) {

        // Security check: only SUPERADMIN can update
        if (!isCurrentUserSuperAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Only SUPERADMIN can edit profiles.");
            return "redirect:/my-profile";
        }

        // Update the target user
        Optional<User> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        User targetUser = targetUserOpt.get();
        if (name != null && !name.isBlank()) targetUser.setName(name.trim());
        if (studentId != null) targetUser.setStudentId(studentId.trim());
        if (currentBatch != null) targetUser.setCurrentBatch(currentBatch.trim());
        if (admittedSemester != null) targetUser.setAdmittedSemester(admittedSemester.trim());
        if (enrollmentStatus != null) targetUser.setEnrollmentStatus(enrollmentStatus.trim());
        if (labClearanceStatus != null) targetUser.setLabClearanceStatus(labClearanceStatus.trim());
        if (seniorityRank != null) targetUser.setSeniorityRank(seniorityRank);
        targetUser.setIsCr(isCr != null && isCr);
        
        // Admins can change roles too
        if (role != null) targetUser.setRole(role);

        userRepository.save(targetUser);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        
        // If updating self, stay on my-profile, otherwise go back to user list
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (targetUser.getEmail().equals(auth.getName())) {
            return "redirect:/my-profile";
        }
        return "redirect:/admin/users";
    }

    private boolean isCurrentUserSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            return userOpt.isPresent() && userOpt.get().getRole() == Role.SUPERADMIN;
        }
        return false;
    }
}
