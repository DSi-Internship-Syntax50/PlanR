package com.example.PlanR.security;

import com.example.PlanR.model.User;
import com.example.PlanR.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final String searchEmail = email.trim().toLowerCase();
        logger.info("Attempting to load user by email: {}", searchEmail);
        
        // Query the database for the user by email
        User user = userRepository.findByEmail(searchEmail)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", searchEmail);
                    return new UsernameNotFoundException("User not found with email: " + searchEmail);
                });

        // Map the custom Role enum (e.g., ADMIN) to Spring Security authority (e.g., ROLE_ADMIN)
        String roleName = "ROLE_" + user.getRole().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        
        logger.info("User found: {}. Role: {}", searchEmail, roleName);

        // Convert the PlanR User entity into a Spring Security UserDetails object
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}
