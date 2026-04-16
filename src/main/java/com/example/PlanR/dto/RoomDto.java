package com.example.PlanR.dto;

import com.example.PlanR.model.Room;

public class RoomDto {
    private Long id;
    private String roomNumber;
    private String block;
    private Integer floorNumber;
    private String displayName;
    private String roomType; // String for frontend safety (avoids enum serialization quirks)

    public RoomDto(Room room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.block = room.getBlock();
        this.floorNumber = room.getFloorNumber();
        this.roomType = room.getType() != null ? room.getType().name() : null;

        // Compose full display name e.g. "1A01" or "7C05"
        // DB structure: floorNumber=1, block="A", roomNumber="01" → "1A01"
        String blockVal = room.getBlock();
        boolean hasBlock = blockVal != null && !blockVal.isBlank();
        boolean hasFloor = room.getFloorNumber() != null;

        if (hasFloor && hasBlock) {
            // Full composite: floor + block + roomNumber → "1A01"
            this.displayName = room.getFloorNumber() + blockVal + room.getRoomNumber();
        } else if (hasBlock) {
            // block + roomNumber → "A01"
            this.displayName = blockVal + room.getRoomNumber();
        } else if (hasFloor) {
            // floor + roomNumber → "1C05"
            this.displayName = room.getFloorNumber() + room.getRoomNumber();
        } else {
            this.displayName = room.getRoomNumber();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
}

