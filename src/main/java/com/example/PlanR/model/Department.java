package com.example.PlanR.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code")
    private String shortCode;

    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Course> courses;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<User> users;

    // Constructors
    public Department() {
    }

    public Department(String shortCode, String name) {
        this.shortCode = shortCode;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
