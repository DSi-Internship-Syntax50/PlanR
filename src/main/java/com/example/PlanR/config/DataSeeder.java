package com.example.PlanR.config;

import com.example.PlanR.model.*;
import com.example.PlanR.model.enums.*;
import com.example.PlanR.repository.*;
import com.example.PlanR.service.NotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                 DepartmentRepository departmentRepository, 
                                 CourseRepository courseRepository,
                                 RoomRepository roomRepository,
                                 MasterRoutineRepository routineRepository,
                                 PasswordEncoder passwordEncoder, 
                                 NotificationService notificationService) {
        return args -> {
            // Find or create admin user
            User adminUser = userRepository.findByEmail("admin@planr.com").orElse(null);
            
            if (adminUser == null) {
                // 1. Create a dummy department first
                Department cse = new Department("CSE", "Computer Science and Engineering");
                departmentRepository.save(cse);

                // 2. Create the test user
                adminUser = new User("System Admin", "admin@planr.com");
                adminUser.setPassword(passwordEncoder.encode("password123")); // Bcrypt Hash
                adminUser.setRole(Role.ADMIN);
                adminUser.setDepartment(cse);
                adminUser.setCurrentBatch("4.2"); // For testing
                userRepository.save(adminUser);

                // 3. Create a mock course
                Course math = new Course();
                math.setTitle("Discrete Mathematics");
                math.setCourseCode("CSE-1101");
                math.setBatch("4.2");
                math.setDepartment(cse);
                courseRepository.save(math);

                // 4. Create a mock room
                Room room = new Room();
                room.setRoomNumber("Room 305");
                room.setCapacity(50);
                roomRepository.save(room);

                // 5. Create a routine entry starting in 15 minutes
                MasterRoutine routine = new MasterRoutine();
                routine.setCourse(math);
                routine.setRoom(room);
                routine.setSection(Section.A);
                routine.setDayOfWeek(DayOfWeek.valueOf(java.time.LocalDate.now().getDayOfWeek().name()));
                routine.setStartTime(LocalTime.now().plusMinutes(15).withSecond(0).withNano(0));
                routine.setEndTime(routine.getStartTime().plusHours(1));
                routineRepository.save(routine);
                
                System.out.println("=========================================");
                System.out.println("TEST DATA CREATED: admin@planr.com / password123");
                System.out.println(">>> Mock Class: " + math.getTitle() + " at " + routine.getStartTime());
                System.out.println("=========================================");
            }

            // 3. Ensure EVERY user has at least one welcome notification
            userRepository.findAll().forEach(user -> {
                if (notificationService.getUnreadCount(user) == 0) {
                    notificationService.createNotification(user, "Welcome to PlanR", "System initialization complete. Explore your logic maps and seat plans!");
                    System.out.println(">>> Welcome notification seeded for: " + user.getEmail());
                }
            });
        };
    }
}
