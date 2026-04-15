package com.example.PlanR.config;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.PlanR.model.Course;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.model.enums.RoomType;
import com.example.PlanR.model.enums.Section;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;

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
            JdbcTemplate jdbcTemplate) {

        return args -> {
            // 0. Migrate any legacy 'ADMIN' role to 'COORDINATOR' in the database (Ai-FAQ)
            int migrated = jdbcTemplate.update("UPDATE users SET role = 'COORDINATOR' WHERE role = 'ADMIN'");
            if (migrated > 0) {
                System.out.println("MIGRATION: Updated " + migrated + " user(s) from ADMIN to COORDINATOR role.");
            }

            // 2. Seed Departments if empty
            if (departmentRepository.count() == 0) {
                departmentRepository.save(new Department("CSE", "Computer Science and Engineering"));
                departmentRepository.save(new Department("EEE", "Electrical and Electronic Engineering"));
                departmentRepository.save(new Department("BBA", "Business Administration"));
                departmentRepository.save(new Department("CE", "Civil Engineering"));
                departmentRepository.save(new Department("ARCHI", "Architecture"));
            }

                Department cse = departmentRepository.findByShortCode("CSE").orElse(null);
                Department eee = departmentRepository.findByShortCode("EEE").orElse(null);
                Department bba = departmentRepository.findByShortCode("BBA").orElse(null);
                Department ce = departmentRepository.findByShortCode("CE").orElse(null);
                Department archi = departmentRepository.findByShortCode("ARCHI").orElse(null);


            // 1. Seed Rooms if empty
            if (roomRepository.count() == 0) {
                Room auditorium = new Room();
                auditorium.setRoomNumber("Main Auditorium");
                auditorium.setType(RoomType.SEMINAR);
                auditorium.setCapacity(500);
                auditorium.setBlock(" ");

                Room seminarHall = new Room();
                seminarHall.setRoomNumber("Seminar Hall C");
                seminarHall.setType(RoomType.LAB);
                seminarHall.setCapacity(500);
                seminarHall.setBlock(" ");

                Room stadium = new Room();
                stadium.setRoomNumber("Indoor Stadium");
                stadium.setType(RoomType.THEORY);
                stadium.setCapacity(500);
                stadium.setBlock(" ");
                roomRepository.saveAll(Arrays.asList(auditorium, seminarHall, stadium));
                // Add Room 201 to 215 like the old UI
                for (int fl = 1; fl < 5; fl++) {
                    for (int i = 1; i <= 7; i++) {
                        for (char ch = 'A'; ch <= 'C'; ch++) {
                            Room r = new Room();
                            r.setFloorNumber(fl);
                            String st = "";
                            st = st + ch;
                            r.setBlock(st);
                            r.setRoomNumber("0" + i);
                            r.setType(RoomType.THEORY);

                            switch (i % 5) {
                                case 0 -> r.setDept(cse);
                                case 1 -> r.setDept(eee);
                                case 2 -> r.setDept(bba);
                                case 3 -> r.setDept(ce);
                                default -> r.setDept(archi);
                            }
                            roomRepository.save(r);

                        }
                    }
                }
                System.out.println("Seeded original hardcoded rooms.");
            }

            Department sys = departmentRepository.findByShortCode("SYS").orElse(null);

            // 3. Seed Superadmin
            if (userRepository.findByEmail("superadmin@planr.com").isEmpty()) {
                User superAdmin = new User("System Administrator", "superadmin@planr.com");
                superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
                superAdmin.setRole(Role.SUPERADMIN);
                superAdmin.setDepartment(sys);
                userRepository.save(superAdmin);
                System.out.println("SUPERADMIN INITIALIZED: superadmin@planr.com / superadmin123");
            }

            // 4. Seed Admin and Mock Routine Data (Ai-FAQ branch logic)
            if (userRepository.findByEmail("admin@planr.com").isEmpty()) {
                User adminUser = new User("System Admin", "admin@planr.com");
                adminUser.setPassword(passwordEncoder.encode("password123"));
                adminUser.setRole(Role.COORDINATOR);
                adminUser.setDepartment(cse);
                adminUser.setStudentId("0000");
                adminUser.setIsCr(false);
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
                routine.setDayOfWeek(com.example.PlanR.model.enums.DayOfWeek
                        .valueOf(java.time.LocalDate.now().getDayOfWeek().name()));
                routine.setStartTime(LocalTime.now().plusMinutes(15).withSecond(0).withNano(0));
                routine.setEndTime(routine.getStartTime().plusHours(1));
                routineRepository.save(routine);

                System.out.println("ADMIN & MOCK DATA INITIALIZED: admin@planr.com / password123");
            }

            // ==========================================
            // 5. NEW LOGIC (Test Data for Routine Builder from dev)
            // ==========================================

            // Changed condition to check for the test teacher instead of course count,
            // since the admin block above creates a course and would cause this to be
            // skipped.
            if (userRepository.findByEmail("teacher@planr.com").isEmpty()) {
                System.out.println("🌱 Seeding Initial Test Data for Routine Builder...");

                Department cseDept = departmentRepository.findByShortCode("CSE").orElse(cse);

                // Create a Teacher for testing
                User teacher = new User("John Doe", "teacher@planr.com");
                teacher.setPassword(passwordEncoder.encode("password123"));
                teacher.setRole(Role.TEACHER);
                teacher.setDepartment(cseDept);
                userRepository.save(teacher);

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

            // ==========================================
            // 6. ANALYTICS MOCK DATA
            // ==========================================
            if (routineRepository.count() < 15) {
                System.out.println("📊 Seeding additional realistic data for Analytics...");

                List<Department> departments = departmentRepository.findAll();
                List<Room> allRooms = roomRepository.findAll();
                Random random = new Random(42); // deterministic random

                com.example.PlanR.model.enums.DayOfWeek[] days = {
                        com.example.PlanR.model.enums.DayOfWeek.MONDAY,
                        com.example.PlanR.model.enums.DayOfWeek.TUESDAY,
                        com.example.PlanR.model.enums.DayOfWeek.WEDNESDAY,
                        com.example.PlanR.model.enums.DayOfWeek.THURSDAY,
                        com.example.PlanR.model.enums.DayOfWeek.FRIDAY,
                        com.example.PlanR.model.enums.DayOfWeek.SATURDAY
                };

                for (Department dept : departments) {
                    if (dept.getShortCode().equals("SYS"))
                        continue; // Skip SYS

                    // Add some dummy courses for this department
                    for (int i = 1; i <= Math.max(3, random.nextInt(6)); i++) {
                        Course dummyCourse = new Course();
                        dummyCourse.setCourseCode(dept.getShortCode() + " " + (1000 + random.nextInt(4000)));
                        dummyCourse.setTitle("Dummy Course " + i + " " + dept.getShortCode());
                        dummyCourse.setDepartment(dept);
                        dummyCourse.setBatch("Batch " + random.nextInt(5));
                        dummyCourse.setSlotCount(random.nextInt(3) + 1); // 1, 2, or 3 slots
                        dummyCourse.setIsLab(random.nextBoolean());
                        courseRepository.save(dummyCourse);

                        // Assign 1 to 3 routines for this course across the week
                        int numRoutines = random.nextInt(3) + 1;
                        for (int j = 0; j < numRoutines; j++) {
                            MasterRoutine r = new MasterRoutine();
                            r.setCourse(dummyCourse);
                            r.setDayOfWeek(days[random.nextInt(days.length)]);

                            // 10% chance to be overbooked (room is null)
                            if (random.nextInt(100) > 10 && !allRooms.isEmpty()) {
                                r.setRoom(allRooms.get(random.nextInt(allRooms.size())));
                            }

                            r.setSection(Section.A);
                            r.setStartSlotIndex(random.nextInt(8) + 1); // slots 1 to 8
                            r.setStartTime(LocalTime.of(8 + r.getStartSlotIndex(), 0));
                            r.setEndTime(r.getStartTime().plusHours(1)); // Approx

                            routineRepository.save(r);
                        }
                    }
                }

                System.out.println("✅ Analytics Data Seeded successfully!");
            }
        };
    }
}