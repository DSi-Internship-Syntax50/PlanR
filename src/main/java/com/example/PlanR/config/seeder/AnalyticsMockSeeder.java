package com.example.PlanR.config.seeder;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

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
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.model.enums.Section;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;

/**
 * Seeder #6: Seeds analytics mock data for development dashboards.
 * ONLY runs when the "dev" profile is active.
 */
@Component
@Profile("dev")
public class AnalyticsMockSeeder implements DataSeederBase {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsMockSeeder.class);
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final MasterRoutineRepository routineRepository;
    private final PasswordEncoder passwordEncoder;

    public AnalyticsMockSeeder(UserRepository userRepository, DepartmentRepository departmentRepository,
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
        if (routineRepository.count() >= 15) return;

        log.info("Seeding additional realistic data for Analytics...");

        User teacher = new User("Jon Doe", "teacher1@planr.com");
        teacher.setPassword(passwordEncoder.encode("password1234"));
        teacher.setRole(Role.TEACHER);
        Department cseDept = departmentRepository.findByShortCode("CSE").orElse(null);
        teacher.setDepartment(cseDept);
        userRepository.save(teacher);

        List<Department> departments = departmentRepository.findAll();
        List<Room> allRooms = roomRepository.findAll();
        Random random = new Random(42); // deterministic random

        DayOfWeek[] days = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        };

        for (Department dept : departments) {
            if (dept.getShortCode().equals("SYS")) continue;

            for (int i = 1; i <= 10; i++) {
                Course dummyCourse = new Course();
                dummyCourse.setCourseCode(dept.getShortCode() + " " + (1000 + random.nextInt(4000)));
                dummyCourse.setTitle("Dummy Course " + i + " " + dept.getShortCode());
                dummyCourse.setDepartment(dept);
                int sc = i % 3 + 1;
                dummyCourse.setRequiredSlots(sc);
                dummyCourse.setSlotCount(sc);
                dummyCourse.setWeeklyClasses(random.nextInt(3) + 3);
                dummyCourse.setIsLab(random.nextBoolean());
                dummyCourse.setSemester((i % 2) + 1);
                dummyCourse.setYear((i % 4) + 1);
                dummyCourse.setBatch("L" + dummyCourse.getYear() + "T" + dummyCourse.getSemester());
                dummyCourse.setTeacher(teacher);
                courseRepository.save(dummyCourse);

                int numRoutines = random.nextInt(3) + 1;
                for (int j = 0; j < numRoutines; j++) {
                    MasterRoutine r = new MasterRoutine();
                    r.setCourse(dummyCourse);
                    r.setDayOfWeek(days[random.nextInt(days.length)]);

                    if (random.nextInt(100) > 10 && !allRooms.isEmpty()) {
                        r.setRoom(allRooms.get(random.nextInt(allRooms.size())));
                    }

                    r.setSection(Section.A);
                    r.setStartSlotIndex(random.nextInt(8) + 1);
                    r.setStartTime(LocalTime.of(8 + r.getStartSlotIndex(), 0));
                    r.setEndTime(r.getStartTime().plusHours(1));

                    routineRepository.save(r);
                }
            }
        }

        log.info("Analytics Data Seeded successfully!");
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
