package com.example.PlanR.exception;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.enums.DayOfWeek;
import java.util.List;

public class SlotConflictException extends RuntimeException {
    private final String batch;
    private final DayOfWeek day;
    private final int requestedStartSlot;
    private final int requestedSlotCount;
    private final List<MasterRoutine> conflictingRoutines;

    public SlotConflictException(String batch, DayOfWeek day, int requestedStartSlot, int requestedSlotCount,
            List<MasterRoutine> conflictingRoutines) {
        super(String.format("Batch %s already has %d class(es) overlapping slots %d to %d on %s",
                batch, conflictingRoutines.size(), requestedStartSlot, (requestedStartSlot + requestedSlotCount - 1),
                day));
        this.batch = batch;
        this.day = day;
        this.requestedStartSlot = requestedStartSlot;
        this.requestedSlotCount = requestedSlotCount;
        this.conflictingRoutines = List.copyOf(conflictingRoutines);
    }

    public String getBatch() {
        return batch;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public int getRequestedStartSlot() {
        return requestedStartSlot;
    }

    public int getRequestedSlotCount() {
        return requestedSlotCount;
    }

    public List<MasterRoutine> getConflictingRoutines() {
        return conflictingRoutines;
    }
}