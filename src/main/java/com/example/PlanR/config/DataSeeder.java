package com.example.PlanR.config;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if test user already exists to prevent duplicate entries on restart
            if (userRepository.findByEmail("admin@planr.com").isEmpty()) {
                
                // 1. Create a dummy department first
                Department cse = new Department("CSE", "Computer Science and Engineering");
                departmentRepository.save(cse);

                // 2. Create the test user
                User adminUser = new User("System Admin", "admin@planr.com");
                adminUser.setPassword(passwordEncoder.encode("password123")); // Bcrypt Hash
                adminUser.setRole(Role.ADMIN);
                adminUser.setDepartment(cse);
                
                userRepository.save(adminUser);
                
                System.out.println("=========================================");
                System.out.println("TEST USER CREATED:");
                System.out.println("Email: admin@planr.com");
                System.out.println("Password: password123");
                System.out.println("=========================================");
            }
        };
    }
}
