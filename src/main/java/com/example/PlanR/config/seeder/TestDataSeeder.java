package com.example.PlanR.config.seeder;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.PlanR.model.Course;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.model.enums.Section;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;

/**
 * Seeder #5: Seeds test users, courses, and routines for development/testing.
 * ONLY runs when the "dev" profile is active.
 */
@Component
@Profile("dev")
public class TestDataSeeder implements DataSeederBase {

    private static final Logger log = LoggerFactory.getLogger(TestDataSeeder.class);
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final MasterRoutineRepository routineRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataSeeder(UserRepository userRepository, DepartmentRepository departmentRepository,
                          CourseRepository courseRepository, RoomRepository roomRepository,
                          MasterRoutineRepository routineRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.roomRepository = roomRepository;
        this.routineRepository = routineRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void seed() {
        Department cseDept = departmentRepository.findByShortCode("CSE").orElse(null);

        seedAdminAndMockData(cseDept);
        seedStudentUser(cseDept);
        seedTeacherAndCourses(cseDept);
    }

    private void seedAdminAndMockData(Department cseDept) {
        if (userRepository.findByEmail("admin@planr.com").isPresent()) return;

        User adminUser = new User("System Admin", "admin@planr.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setRole(Role.COORDINATOR);
        adminUser.setDepartment(cseDept);
        adminUser.setStudentId("20210204000");
        adminUser.setIsCr(false);
        adminUser.setCurrentBatch("3/2");
        userRepository.save(adminUser);

        Course math = new Course();
        math.setTitle("Discrete Mathematics");
        math.setCourseCode("CSE-1101");
        math.setBatch("4.2");
        math.setDepartment(cseDept);
        courseRepository.save(math);

        Room room = roomRepository.findAll().get(0);

        MasterRoutine routine = new MasterRoutine();
        routine.setCourse(math);
        routine.setRoom(room);
        routine.setSection(Section.A);
        routine.setDayOfWeek(com.example.PlanR.model.enums.DayOfWeek
                .valueOf(java.time.LocalDate.now().getDayOfWeek().name()));
        routine.setStartTime(LocalTime.now().plusMinutes(15).withSecond(0).withNano(0));
        routine.setEndTime(routine.getStartTime().plusHours(1));
        routineRepository.save(routine);

        log.info("ADMIN & MOCK DATA INITIALIZED: admin@planr.com / password123");
    }

    private void seedStudentUser(Department cseDept) {
        if (userRepository.findByEmail("student@planr.com").isPresent()) return;

        User student = new User("Jane Doe", "student@planr.com");
        student.setPassword(passwordEncoder.encode("password123"));
        student.setRole(Role.STUDENT);
        student.setDepartment(cseDept);
        student.setStudentId("20210204001");
        student.setCurrentBatch("2/1");
        userRepository.save(student);
        log.info("STUDENT INITIALIZED: student@planr.com / password123");
    }

    private void seedTeacherAndCourses(Department cseDept) {
        if (userRepository.findByEmail("teacher@planr.com").isPresent()) return;

        log.info("Seeding Initial Test Data for Routine Builder...");

        User teacher = new User("John Doe", "teacher@planr.com");
        teacher.setPassword(passwordEncoder.encode("password123"));
        teacher.setRole(Role.TEACHER);
        teacher.setDepartment(cseDept);
        userRepository.save(teacher);

        // Theory Course 1
        Course c1 = new Course();
        c1.setCourseCode("CSE 3101");
        c1.setTitle("Database Systems");
        c1.setBatch("L3T1");
        c1.setYear(3);
        c1.setSemester(1);
        c1.setRequiredSlots(1);
        c1.setSlotCount(1);
        c1.setWeeklyClasses(3);
        c1.setIsLab(false);
        c1.setDepartment(cseDept);
        c1.setTeacher(teacher);
        courseRepository.save(c1);

        // Lab Course 1
        Course c2 = new Course();
        c2.setCourseCode("CSE 3102");
        c2.setTitle("Database Systems Lab");
        c2.setBatch("L3T2");
        c2.setYear(3);
        c2.setSemester(2);
        c2.setRequiredSlots(3);
        c2.setSlotCount(3);
        c2.setWeeklyClasses(1);
        c2.setIsLab(true);
        c2.setDepartment(cseDept);
        c2.setTeacher(teacher);
        courseRepository.save(c2);

        // Theory Course 2
        Course c3 = new Course();
        c3.setCourseCode("CSE 3103");
        c3.setTitle("Computer Networks");
        c3.setBatch("L3T2");
        c3.setYear(3);
        c3.setSemester(2);
        c3.setRequiredSlots(1);
        c3.setSlotCount(1);
        c3.setWeeklyClasses(2);
        c3.setIsLab(false);
        c3.setDepartment(cseDept);
        c3.setTeacher(teacher);
        courseRepository.save(c3);

        // Theory Course 3
        Course c4 = new Course();
        c4.setCourseCode("CSE 3105");
        c4.setTitle("Software Engineering");
        c4.setBatch("L3T1");
        c4.setYear(3);
        c4.setSemester(1);
        c4.setRequiredSlots(1);
        c4.setSlotCount(1);
        c4.setWeeklyClasses(3);
        c4.setIsLab(false);
        c4.setDepartment(cseDept);
        c4.setTeacher(teacher);
        courseRepository.save(c4);

        log.info("Routine Builder Test Data Seeded. Teacher ID: {} | Email: teacher@planr.com", teacher.getId());
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
