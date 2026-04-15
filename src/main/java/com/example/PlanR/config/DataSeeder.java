package com.example.PlanR.config;

import com.example.PlanR.model.Course;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.model.enums.RoomType;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;

import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, CourseRepository courseRepository, DepartmentRepository departmentRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
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

            // ==========================================
            // 2. NEW LOGIC (Test Data for Routine Builder)
            // ==========================================

            // Only seed courses if they don't exist yet
            if (courseRepository.count() == 0) {
                System.out.println("🌱 Seeding Initial Test Data for Routine Builder...");

                // Fetch the CSE department created by the existing logic above
                Department cseDept = departmentRepository.findAll().stream()
                        .filter(d -> "CSE".equals(d.getShortCode()))
                        .findFirst()
                        .orElseGet(() -> departmentRepository
                                .save(new Department("CSE", "Computer Science and Engineering")));

                // Create a Teacher for testing
                User teacher = userRepository.findByEmail("teacher@planr.com").orElseGet(() -> {
                    User t = new User("John Doe", "teacher@planr.com");
                    t.setPassword(passwordEncoder.encode("password123"));
                    t.setRole(Role.TEACHER);
                    t.setDepartment(cseDept);
                    return userRepository.save(t);
                });

                // Create Test Courses for Batch L3T1
                Course c1 = new Course();
                c1.setCourseCode("CSE 3101");
                c1.setTitle("Database Systems");
                c1.setBatch("L3T1");
                c1.setSlotCount(1); // Theory takes 1 time slot
                c1.setIsLab(false);
                c1.setDepartment(cseDept);
                c1.setTeacher(teacher);
                courseRepository.save(c1);

                Course c2 = new Course();
                c2.setCourseCode("CSE 3102");
                c2.setTitle("Database Systems Lab");
                c2.setBatch("L3T1");
                c2.setSlotCount(2); // Lab takes 2 consecutive time slots!
                c2.setIsLab(true);
                c2.setDepartment(cseDept);
                c2.setTeacher(teacher);
                courseRepository.save(c2);

                Course c3 = new Course();
                c3.setCourseCode("CSE 3103");
                c3.setTitle("Computer Networks");
                c3.setBatch("L3T1");
                c3.setSlotCount(1);
                c3.setIsLab(false);
                c3.setDepartment(cseDept);
                c3.setTeacher(teacher);
                courseRepository.save(c3);

                System.out.println("✅ Routine Builder Test Data Seeded successfully!");
                System.out.println("Test Teacher ID: " + teacher.getId() + " | Email: teacher@planr.com");
            }
        };
    }
}