package com.example.PlanR.model;

import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.Section;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "master_routine", indexes = {@Index(name = "idx_routine_teacher_day", columnList = "teacher_id, day_of_week")})
public class MasterRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private Section section;

    @Column(name = "start_slot_index")
    private Integer startSlotIndex;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ClassOverride> overrides;

    public MasterRoutine() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public List<ClassOverride> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<ClassOverride> overrides) {
        this.overrides = overrides;
    }

    public Integer getStartSlotIndex() {
        return startSlotIndex;
    }

    public void setStartSlotIndex(Integer startSlotIndex) {
        this.startSlotIndex = startSlotIndex;
    }
}
