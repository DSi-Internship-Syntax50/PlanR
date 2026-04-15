package com.example.PlanR.controller;

import com.example.PlanR.exception.SlotConflictException;
import com.example.PlanR.model.User;
import com.example.PlanR.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("user")
    public User globalUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            return optionalUser.orElse(null);
        }
        return null;
    }

    // --- New Exception Handler for REST API Conflicts ---
    @ExceptionHandler(SlotConflictException.class)
    public ResponseEntity<Map<String, Object>> handleSlotConflict(SlotConflictException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Batch Slot Conflict");
        response.put("message", ex.getMessage());

        // Return 409 Conflict status so the frontend knows it was a scheduling issue
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}