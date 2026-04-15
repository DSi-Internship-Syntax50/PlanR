package com.example.PlanR.dto;

public class SeatPlanResponseDto {
    private boolean success;
    private String[][] grid;
    private String message;

    public SeatPlanResponseDto() {
    }

    public SeatPlanResponseDto(boolean success, String[][] grid, String message) {
        this.success = success;
        this.grid = grid;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String[][] getGrid() {
        return grid;
    }

    public void setGrid(String[][] grid) {
        this.grid = grid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
