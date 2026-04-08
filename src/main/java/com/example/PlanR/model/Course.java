package com.example.PlanR.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "course_code")
    private String courseCode;

    private String title;

    @Column(name = "is_lab")
    private Boolean isLab;

    private String batch;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<MasterRoutine> routines;

    // Constructors
    public Course() {}

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
}
