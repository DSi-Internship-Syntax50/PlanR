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

@Entity
@Table(name = "rooms")
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

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department dept;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<MasterRoutine> routines;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
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

    public void setDept(Department dept) {
        this.dept = dept;
    }

    public Department getDept() {
        return dept;
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
}
