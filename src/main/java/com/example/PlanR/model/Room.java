package com.example.PlanR.model;

import com.example.PlanR.model.enums.RoomType;
import jakarta.persistence.*;
import java.util.List;

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

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<MasterRoutine> routines;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<EventBooking> bookings;

  
    public Room() {}


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
}
