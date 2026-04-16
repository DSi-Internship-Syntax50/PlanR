package com.example.PlanR.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SeatPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private int gridRows;
    private int gridCols;
    private LocalDateTime generatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "seatPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatAllocation> allocations = new ArrayList<>();

    // Helper method to add allocations
    public void addAllocation(SeatAllocation allocation) {
        allocations.add(allocation);
        allocation.setSeatPlan(this);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public int getGridRows() { return gridRows; }
    public void setGridRows(int gridRows) { this.gridRows = gridRows; }
    public int getGridCols() { return gridCols; }
    public void setGridCols(int gridCols) { this.gridCols = gridCols; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public List<SeatAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<SeatAllocation> allocations) { this.allocations = allocations; }
}