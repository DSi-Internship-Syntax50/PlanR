package com.example.PlanR.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.model.enums.RoomType;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;

import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, DepartmentRepository departmentRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
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

            // Check if superadmin already exists
            if (userRepository.findByEmail("superadmin@planr.com").isEmpty()) {

                // Create a dummy system department
                Department systemDept = departmentRepository.save(new Department("SYS", "System Administration"));

                // Add default academic departments
                if (departmentRepository.count() <= 1) {
                    departmentRepository.save(new Department("CSE", "Computer Science and Engineering"));
                    departmentRepository.save(new Department("EEE", "Electrical and Electronic Engineering"));
                    departmentRepository.save(new Department("BBA", "Business Administration"));
                    departmentRepository.save(new Department("CE", "Civil Engineering"));
                }

                // Create the singular Superadmin
                User superAdmin = new User("System Administrator", "superadmin@planr.com");
                superAdmin.setPassword(passwordEncoder.encode("superadmin123")); // Remember to change this in production
                superAdmin.setRole(Role.SUPERADMIN);
                superAdmin.setDepartment(systemDept);

                userRepository.save(superAdmin);

                System.out.println("=========================================");
                System.out.println("SUPERADMIN INITIALIZED:");
                System.out.println("Email: superadmin@planr.com");
                System.out.println("Password: superadmin123");
                System.out.println("=========================================");
            }
        };
    }
}