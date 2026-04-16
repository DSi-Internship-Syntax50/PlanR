package com.example.PlanR.dto;

import com.example.PlanR.model.Course;

/**
 * Centralized utility for computing effective slot counts.
 * Eliminates the duplicated ternary formula that was previously
 * copy-pasted in 4+ locations across the codebase.
 */
public final class SlotCalculator {

    private SlotCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Returns the effective number of time slots a course occupies.
     * Priority: requiredSlots → isLab (3 slots) → default (1 slot).
     */
    public static int getEffectiveSlotCount(Course course) {
        if (course == null) return 1;
        if (course.getRequiredSlots() != null) return course.getRequiredSlots();
        return Boolean.TRUE.equals(course.getIsLab()) ? 3 : 1;
    }
}
