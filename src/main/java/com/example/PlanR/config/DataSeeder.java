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
import java.time.DayOfWeek;
import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            RoomRepository roomRepository,
            MasterRoutineRepository routineRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        
        return args -> {
            // 1. Seed Rooms if empty (from dev branch)
            if (roomRepository.count() == 0) {
                Room auditorium = new Room();
                auditorium.setRoomNumber("Main Auditorium");
                auditorium.setType(RoomType.SEMINAR);
                auditorium.setCapacity(200);

                Room seminarHall = new Room();
                seminarHall.setRoomNumber("Seminar Hall C");
                seminarHall.setType(RoomType.LAB);
                seminarHall.setCapacity(40);

                roomRepository.saveAll(Arrays.asList(auditorium, seminarHall));

                // Add Room 201 to 215
                for (int i = 1; i <= 15; i++) {
                    Room r = new Room();
                    r.setRoomNumber("Room 20" + i);
                    r.setType(RoomType.THEORY);
                    r.setCapacity(50);
                    roomRepository.save(r);
                }
                System.out.println("Seeded original hardcoded rooms.");
            }

            // 2. Seed Departments if empty
            if (departmentRepository.count() == 0) {
                departmentRepository.save(new Department("SYS", "System Administration"));
                departmentRepository.save(new Department("CSE", "Computer Science and Engineering"));
                departmentRepository.save(new Department("EEE", "Electrical and Electronic Engineering"));
                departmentRepository.save(new Department("BBA", "Business Administration"));
                departmentRepository.save(new Department("CE", "Civil Engineering"));
            }

            Department cse = departmentRepository.findByShortName("CSE").orElse(null);
            Department sys = departmentRepository.findByShortName("SYS").orElse(null);

            // 3. Seed Superadmin (from dev branch)
            if (userRepository.findByEmail("superadmin@planr.com").isEmpty()) {
                User superAdmin = new User("System Administrator", "superadmin@planr.com");
                superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
                superAdmin.setRole(Role.SUPERADMIN);
                superAdmin.setDepartment(sys);
                userRepository.save(superAdmin);
                System.out.println("SUPERADMIN INITIALIZED: superadmin@planr.com / superadmin123");
            }

            // 4. Seed Admin and Mock Routine Data (from Ai-FAQ branch)
            if (userRepository.findByEmail("admin@planr.com").isEmpty()) {
                User adminUser = new User("System Admin", "admin@planr.com");
                adminUser.setPassword(passwordEncoder.encode("password123"));
                adminUser.setRole(Role.ADMIN);
                adminUser.setDepartment(cse);
                adminUser.setCurrentBatch("4.2");
                userRepository.save(adminUser);

                // Create a mock course
                Course math = new Course();
                math.setTitle("Discrete Mathematics");
                math.setCourseCode("CSE-1101");
                math.setBatch("4.2");
                math.setDepartment(cse);
                courseRepository.save(math);

                // Get a room for the routine
                Room room = roomRepository.findAll().get(0);

                // Create a routine entry starting in 15 minutes
                MasterRoutine routine = new MasterRoutine();
                routine.setCourse(math);
                routine.setRoom(room);
                routine.setSection(Section.A);
                routine.setDayOfWeek(java.time.LocalDate.now().getDayOfWeek());
                routine.setStartTime(LocalTime.now().plusMinutes(15).withSecond(0).withNano(0));
                routine.setEndTime(routine.getStartTime().plusHours(1));
                routineRepository.save(routine);

                System.out.println("ADMIN & MOCK DATA INITIALIZED: admin@planr.com / password123");
            }

            // 5. Ensure EVERY user has a welcome notification
            userRepository.findAll().forEach(user -> {
                if (notificationService.getUnreadCount(user) == 0) {
                    notificationService.createNotification(
                        user, 
                        "Welcome to PlanR", 
                        "System initialization complete. Explore your logic maps and seat plans!"
                    );
                }
            });
        };
    }
}