package com.example.PlanR.dto;

import java.util.List;

public class SeatPlanRequestDto {
    private Long targetRoomId;
    private int rows;
    private int cols;
    private List<PoolDto> pools;

    public SeatPlanRequestDto() {
    }

    public Long getTargetRoomId() {
        return targetRoomId;
    }

    public void setTargetRoomId(Long targetRoomId) {
        this.targetRoomId = targetRoomId;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public List<PoolDto> getPools() {
        return pools;
    }

    public void setPools(List<PoolDto> pools) {
        this.pools = pools;
    }
}
