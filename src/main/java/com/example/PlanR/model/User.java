package com.example.PlanR.model;

import com.example.PlanR.model.enums.Role;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "current_batch")
    private String currentBatch;

    @Column(name = "seniority_rank")
    private Integer seniorityRank;

    @Column(name = "is_cr")
    private Boolean isCr;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<MasterRoutine> taughtRoutines;

    @OneToMany(mappedBy = "requestedBy", cascade = CascadeType.ALL)
    private List<EventBooking> requestedEvents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    // Constructors
    public User() {}

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

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
}
