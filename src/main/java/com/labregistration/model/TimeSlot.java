package com.labregistration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots", indexes = {
    @Index(name = "idx_timeslot_session_date", columnList = "lab_session_id, sessionDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_session_id", nullable = false)
    private LabSession labSession;

    // The specific date for this time slot
    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Slot number (1, 2, 3, etc.) for ordering
//    private Integer slotNumber;

    private Integer groupNumber;
    // Max students for this specific slot
    private Integer maxStudents;

    // Current number of confirmed registrations (denormalized for performance)
    @Builder.Default
    private Integer currentCount = 0;

    @Builder.Default
    private Boolean active = true;

    // Calculated week number within the session
    @Transient
    public int getWeekNumber() {
        if (labSession == null || labSession.getStartDate() == null) return 1;
        long days = java.time.temporal.ChronoUnit.DAYS.between(labSession.getStartDate(), sessionDate);
        return (int) (days / 7) + 1;
    }

    public String getDisplayName() {
        String dayName = sessionDate.getDayOfWeek().toString().substring(0, 3);
        return String.format("Week %d - %s - Slot %d (%s - %s)", 
            getWeekNumber(), dayName, groupNumber, startTime, endTime);
    }

    public boolean isFull() {
        return currentCount >= maxStudents;
    }

    public int getAvailableSlots() {
        return Math.max(0, maxStudents - currentCount);
    }

    @Override
    public String toString() {
        return "TimeSlot{id=" + id + ", date=" + sessionDate + ", slot=" + groupNumber + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return id != null && id.equals(timeSlot.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
