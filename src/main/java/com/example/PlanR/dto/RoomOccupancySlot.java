package com.example.PlanR.dto;

/**
 * Represents one hourly slot (e.g. 8:00–9:00) for a room on a specific date.
 * Merges both EventBooking and MasterRoutine (class schedule) occupancy.
 */
public class RoomOccupancySlot {

    private int hour;            // 8..19 (8 = 8AM, 14 = 2PM, etc.)
    private String slotLabel;    // "8 AM", "2 PM" etc.
    private String occupancyType; // "FREE", "CLASS", "EVENT_PENDING", "EVENT_APPROVED"
    private String label;        // course code for CLASS, event title for EVENT_*
    private String bookingStatus; // mirrors occupancyType for frontend compat

    public RoomOccupancySlot(int hour) {
        this.hour = hour;
        this.slotLabel = formatHour(hour);
        this.occupancyType = "FREE";
        this.label = "Free to book (click)";
        this.bookingStatus = "FREE";
    }

    private String formatHour(int h) {
        if (h == 12) return "12 PM";
        if (h < 12) return h + " AM";
        return (h - 12) + " PM";
    }

    public void markAsClass(String courseCode) {
        this.occupancyType = "CLASS";
        this.label = courseCode != null ? courseCode : "Class";
        this.bookingStatus = "CLASS";
    }

    public void markAsEvent(String title, String status) {
        this.occupancyType = "PENDING".equals(status) ? "EVENT_PENDING" : "EVENT_APPROVED";
        this.label = title != null ? title : "Event";
        this.bookingStatus = status;
    }

    // Getters
    public int getHour() { return hour; }
    public String getSlotLabel() { return slotLabel; }
    public String getOccupancyType() { return occupancyType; }
    public String getLabel() { return label; }
    public String getBookingStatus() { return bookingStatus; }
}
