package com.example.medcare.model;

public class TimeSlot {
    public final String time; // e.g., "09:30"
    public final boolean isAvailable;

    public TimeSlot(String time, boolean isAvailable) {
        this.time = time;
        this.isAvailable = isAvailable;
    }

}