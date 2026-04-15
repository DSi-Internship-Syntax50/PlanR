package com.example.PlanR.dto;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.enums.RoomType;

public class RoomCreateDto {
    private String roomNumber;
    private RoomType type;
    private Integer capacity;
    private Boolean hasComputers;
    private Boolean hasHardwareKits;
    private Integer floorNumber;
    private String block;
    private Department dept;

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

    public Department getDept() {
        return dept;
    }

    public void setDept(Department dept) {
        this.dept = dept;
    }
}
