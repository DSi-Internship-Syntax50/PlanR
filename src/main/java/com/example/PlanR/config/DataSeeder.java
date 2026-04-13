package com.example.PlanR.config;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.model.enums.RoomType;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, DepartmentRepository departmentRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            
            // Seed Rooms if empty to restore original hardcoded UI rooms
            if (roomRepository.count() == 0) {
                Room auditorium = new Room();
                auditorium.setRoomNumber("Main Auditorium");
                auditorium.setType(RoomType.SEMINAR); 
                
                Room seminarHall = new Room();
                seminarHall.setRoomNumber("Seminar Hall C");
                seminarHall.setType(RoomType.LAB); 
                
                Room stadium = new Room();
                stadium.setRoomNumber("Indoor Stadium");
                stadium.setType(RoomType.THEORY);

                roomRepository.saveAll(Arrays.asList(auditorium, seminarHall, stadium));

                // Add Room 201 to 215 like the old UI
                for (int i = 1; i <= 15; i++) {
                    Room r = new Room();
                    r.setRoomNumber("Room 20" + i);
                    r.setType(RoomType.THEORY);
                    roomRepository.save(r);
                }
                System.out.println("Seeded original hardcoded rooms.");
            }

            // Check if test user already exists
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
