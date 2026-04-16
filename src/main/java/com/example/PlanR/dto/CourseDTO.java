package com.example.PlanR.dto;

import com.example.PlanR.model.Course;

/**
 * DTO for course data returned by schedule API endpoints.
 * Replaces inline HashMap<String, Object> construction in controllers.
 */
public class CourseDTO {

    public Long id;
    public String courseCode;
    public String title;
    public int slotCount;

    public CourseDTO() {}

    public CourseDTO(Course course) {
        this.id = course.getId();
        this.courseCode = course.getCourseCode();
        this.title = course.getTitle();
        this.slotCount = SlotCalculator.getEffectiveSlotCount(course);
    }
}
