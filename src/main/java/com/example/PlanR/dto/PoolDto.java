package com.example.PlanR.dto;

public class PoolDto {
    private String shortCode;
    private int count;

    public PoolDto() {
    }

    public PoolDto(String shortCode, int count) {
        this.shortCode = shortCode;
        this.count = count;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
