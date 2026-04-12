package com.example.PlanR.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.PlanR.model.User;
import com.example.PlanR.repository.UserRepository;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-profile")
    public String myProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }
        }
        
        if (user == null) {
            // Provide a dummy user for UI testing if unauthenticated
            user = new User();
            user.setName("UI Tester");
            user.setEmail("tester@planr.com");
            user.setRole(com.example.PlanR.model.enums.Role.STUDENT);
        }
        
        model.addAttribute("user", user);
        return "profile";
    }
}
