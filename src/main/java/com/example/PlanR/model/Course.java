package com.example.PlanR.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.List;

@Entity
@Table(name = "courses")
@SQLDelete(sql = "UPDATE courses SET is_active = false WHERE id=?")
@SQLRestriction("is_active = true")
public class Course {

    private Integer year;

    private Integer semester;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;

    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode;

    private String title;

    @Column(name = "is_lab")
    private Boolean isLab;

    private String batch;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "student_capacity")
    private Integer studentCapacity;

    @Column(name = "slot_count")
    private Integer slotCount;

    @Column(name = "required_slots")
    private Integer requiredSlots;

    @Column(name = "weekly_classes")
    private Integer weeklyClasses;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private List<MasterRoutine> routines;

    // Constructors
    public Course() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsLab() {
        return isLab;
    }

    public void setIsLab(Boolean isLab) {
        this.isLab = isLab;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public List<MasterRoutine> getRoutines() {
        return routines;
    }

    public void setRoutines(List<MasterRoutine> routines) {
        this.routines = routines;
    }

    public Integer getStudentCapacity() {
        return studentCapacity;
    }

    public void setStudentCapacity(Integer studentCapacity) {
        this.studentCapacity = studentCapacity;
    }

    public Integer getSlotCount() {
        if (slotCount != null) return slotCount;
        if (requiredSlots != null) return requiredSlots;
        return (Boolean.TRUE.equals(isLab) ? 3 : 1);
    }

    public void setSlotCount(Integer slotCount) {
        this.slotCount = slotCount;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Integer getRequiredSlots() { return requiredSlots; }
    public void setRequiredSlots(Integer requiredSlots) { this.requiredSlots = requiredSlots; }

    public Integer getWeeklyClasses() { return weeklyClasses; }
    public void setWeeklyClasses(Integer weeklyClasses) { this.weeklyClasses = weeklyClasses; }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
