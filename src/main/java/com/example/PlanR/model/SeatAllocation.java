package com.example.PlanR.model;

import jakarta.persistence.*;

@Entity
public class SeatAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_plan_id")
    private SeatPlan seatPlan;

    private int rowIndex;
    private int colIndex;
    private String departmentCode; // Null if seat is empty

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SeatPlan getSeatPlan() { return seatPlan; }
    public void setSeatPlan(SeatPlan seatPlan) { this.seatPlan = seatPlan; }
    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    public int getColIndex() { return colIndex; }
    public void setColIndex(int colIndex) { this.colIndex = colIndex; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
}