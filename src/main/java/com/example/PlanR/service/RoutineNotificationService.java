package com.example.PlanR.service;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RoutineNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(RoutineNotificationService.class);
    private final MasterRoutineRepository masterRoutineRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public RoutineNotificationService(MasterRoutineRepository masterRoutineRepository, 
                                      UserRepository userRepository, 
                                      NotificationService notificationService) {
        this.masterRoutineRepository = masterRoutineRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Runs every minute to check for upcoming classes starting in 15 minutes.
     */
    @Scheduled(fixedRate = 60000)
    public void checkForUpcomingClasses() {
        LocalTime now = LocalTime.now();
        LocalTime targetTime = now.plusMinutes(15).withSecond(0).withNano(0);
        DayOfWeek currentDay = getCurrentDayEnum();

        logger.debug("Checking for classes starting at {} on {}", targetTime, currentDay);

        // Find classes starting within that specific minute
        List<MasterRoutine> upcomingClasses = masterRoutineRepository.findByDayOfWeekAndStartTimeBetween(
                currentDay, targetTime, targetTime.plusSeconds(59));

        for (MasterRoutine routine : upcomingClasses) {
            sendClassAlerts(routine);
        }
    }

    private void sendClassAlerts(MasterRoutine routine) {
        String batch = routine.getCourse().getBatch();
        String courseTitle = routine.getCourse().getTitle();
        String roomName = routine.getRoom().getRoomNumber();
        String startTime = routine.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

        logger.info("Sending upcoming class alerts for {} - Batch: {}", courseTitle, batch);

        List<User> students = userRepository.findAll().stream()
                .filter(u -> batch.equals(u.getCurrentBatch()))
                .toList();

        String message = String.format("Upcoming Class: %s is starting at %s in Room %s. Don't be late!", 
                courseTitle, startTime, roomName);

        for (User student : students) {
            notificationService.createNotification(student, "Next Class Alert", message);
        }
    }

    private DayOfWeek getCurrentDayEnum() {
        java.time.DayOfWeek day = java.time.LocalDate.now().getDayOfWeek();
        return DayOfWeek.valueOf(day.name());
    }
}
