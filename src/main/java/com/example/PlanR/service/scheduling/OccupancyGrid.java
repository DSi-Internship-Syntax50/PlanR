package com.example.PlanR.service.scheduling;

import com.example.PlanR.model.enums.DayOfWeek;

/**
 * Encapsulates the in-memory occupancy grid for batch schedule generation.
 * Replaces the raw boolean[7][13] array that was previously managed inline
 * in ScheduleService.autoGenerateRoutine().
 */
public class OccupancyGrid {

    private final boolean[][] grid;
    private static final int MAX_SLOTS = 13;

    public OccupancyGrid() {
        this.grid = new boolean[7][MAX_SLOTS];
    }

    /**
     * Checks if a range of consecutive slots is completely free for a given day.
     */
    public boolean isSlotFree(DayOfWeek day, int startSlot, int slotsNeeded) {
        int dayIndex = day.ordinal();
        for (int i = 0; i < slotsNeeded; i++) {
            if (startSlot + i >= MAX_SLOTS || grid[dayIndex][startSlot + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Marks a range of consecutive slots as occupied for a given day.
     */
    public void markOccupied(DayOfWeek day, int startSlot, int slotsNeeded) {
        int dayIndex = day.ordinal();
        for (int i = 0; i < slotsNeeded; i++) {
            if (startSlot + i < MAX_SLOTS) {
                grid[dayIndex][startSlot + i] = true;
            }
        }
    }

    /**
     * Returns the number of occupied slots for a given day index.
     */
    public int getDayLoad(int dayOrdinal) {
        int load = 0;
        for (int i = 1; i < MAX_SLOTS; i++) {
            if (grid[dayOrdinal][i]) load++;
        }
        return load;
    }

    /**
     * Returns the number of occupied slots for a given DayOfWeek.
     */
    public int getDayLoad(DayOfWeek day) {
        return getDayLoad(day.ordinal());
    }
}
