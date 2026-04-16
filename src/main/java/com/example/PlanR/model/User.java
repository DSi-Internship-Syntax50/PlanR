package com.example.PlanR.model;

import com.example.PlanR.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.List;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_active = false WHERE id=?")
@SQLRestriction("is_active = true")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "current_batch")
    private String currentBatch;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "seniority_rank")
    private Integer seniorityRank;

    @Column(name = "is_cr")
    private Boolean isCr;

    @Column(name = "admitted_semester")
    private String admittedSemester;

    @Column(name = "enrollment_status")
    private String enrollmentStatus;

    @Column(name = "lab_clearance_status")
    private String labClearanceStatus;
    
    @OneToMany(mappedBy = "teacher", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private List<MasterRoutine> taughtRoutines;

    @OneToMany(mappedBy = "requestedBy", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private List<EventBooking> requestedEvents;

    // Constructors
    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCurrentBatch() {
        return currentBatch;
    }

    public void setCurrentBatch(String currentBatch) {
        this.currentBatch = currentBatch;
    }

    public Integer getSeniorityRank() {
        return seniorityRank;
    }

    public void setSeniorityRank(Integer seniorityRank) {
        this.seniorityRank = seniorityRank;
    }

    public Boolean getIsCr() {
        return isCr;
    }

    public void setIsCr(Boolean isCr) {
        this.isCr = isCr;
    }

    public List<MasterRoutine> getTaughtRoutines() {
        return taughtRoutines;
    }

    public void setTaughtRoutines(List<MasterRoutine> taughtRoutines) {
        this.taughtRoutines = taughtRoutines;
    }

    public List<EventBooking> getRequestedEvents() {
        return requestedEvents;
    }

    public void setRequestedEvents(List<EventBooking> requestedEvents) {
        this.requestedEvents = requestedEvents;
    }

    public String getAdmittedSemester() {
        return admittedSemester;
    }

    public void setAdmittedSemester(String admittedSemester) {
        this.admittedSemester = admittedSemester;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getLabClearanceStatus() {
        return labClearanceStatus;
    }

    public void setLabClearanceStatus(String labClearanceStatus) {
        this.labClearanceStatus = labClearanceStatus;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}