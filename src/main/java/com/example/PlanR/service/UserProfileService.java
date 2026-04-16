package com.example.PlanR.service;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PlanR.dto.ProfileUpdateDto;
import com.example.PlanR.exception.EntityNotFoundException;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.UserRepository;

/**
 * Service encapsulating user profile management logic.
 * Extracted from ProfileController to enable unit testing and SRP compliance.
 */
@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the currently authenticated user, or null if unauthenticated.
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                // Force-initialize lazy department
                if (user.getDepartment() != null) {
                    user.getDepartment().getName();
                }
                return user;
            }
        }
        return null;
    }

    /**
     * Returns a user by ID with lazy fields initialized.
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        // Force-initialize lazy department
        if (user.getDepartment() != null) {
            user.getDepartment().getName();
        }
        return user;
    }

    /**
     * Updates a user's profile with the given DTO fields.
     */
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDto dto) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        if (dto.getName() != null && !dto.getName().isBlank()) targetUser.setName(dto.getName().trim());
        if (dto.getStudentId() != null) targetUser.setStudentId(dto.getStudentId().trim());
        if (dto.getCurrentBatch() != null) targetUser.setCurrentBatch(dto.getCurrentBatch().trim());
        if (dto.getAdmittedSemester() != null) targetUser.setAdmittedSemester(dto.getAdmittedSemester().trim());
        if (dto.getEnrollmentStatus() != null) targetUser.setEnrollmentStatus(dto.getEnrollmentStatus().trim());
        if (dto.getLabClearanceStatus() != null) targetUser.setLabClearanceStatus(dto.getLabClearanceStatus().trim());
        if (dto.getSeniorityRank() != null) targetUser.setSeniorityRank(dto.getSeniorityRank());
        targetUser.setIsCr(dto.getIsCr() != null && dto.getIsCr());

        // Admins can change roles too
        if (dto.getRole() != null) targetUser.setRole(dto.getRole());

        userRepository.save(targetUser);
    }

    /**
     * Checks if the currently authenticated user is a SUPERADMIN.
     */
    public boolean isCurrentUserSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            return userOpt.isPresent() && userOpt.get().getRole() == Role.SUPERADMIN;
        }
        return false;
    }

    /**
     * Checks if the given user is the currently authenticated user.
     */
    public boolean isViewingSelf(User targetUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && targetUser.getEmail().equals(auth.getName());
    }
}
