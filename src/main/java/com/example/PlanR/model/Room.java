package com.example.PlanR.model;

import java.util.List;

import com.example.PlanR.model.enums.RoomType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "rooms")
@SQLDelete(sql = "UPDATE rooms SET is_active = false WHERE id=?")
@SQLRestriction("is_active = true")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number")
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    private Integer capacity;

    @Column(name = "has_computers")
    private Boolean hasComputers;

    @Column(name = "has_hardware_kits")
    private Boolean hasHardwareKits;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "block_name")
    private String block;

    @Column(name = "dept_name")
    private String dept;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "room", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private List<MasterRoutine> routines;

    @OneToMany(mappedBy = "room", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private List<EventBooking> bookings;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getDept() {
        return dept;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Boolean getHasComputers() {
        return hasComputers;
    }

    public void setHasComputers(Boolean hasComputers) {
        this.hasComputers = hasComputers;
    }

    public Boolean getHasHardwareKits() {
        return hasHardwareKits;
    }

    public void setHasHardwareKits(Boolean hasHardwareKits) {
        this.hasHardwareKits = hasHardwareKits;
    }

    public List<MasterRoutine> getRoutines() {
        return routines;
    }

    public void setRoutines(List<MasterRoutine> routines) {
        this.routines = routines;
    }

    public List<EventBooking> getBookings() {
        return bookings;
    }

    public void setBookings(List<EventBooking> bookings) {
        this.bookings = bookings;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
