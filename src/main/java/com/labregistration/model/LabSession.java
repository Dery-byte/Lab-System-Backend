package com.labregistration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lab_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String labRoom;

    // Recurring session - start and end dates define the duration
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // Time for each session
    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Days of week when session occurs (comma-separated: "MONDAY,WEDNESDAY,FRIDAY")
    @Column(name = "session_days")
    private String sessionDays;

    // Maximum students per time slot/group
    private Integer maxStudentsPerSlot;
    private Integer maxGroupSize; // Maximum students per group
    private Integer maxGroups; // Maximum number of groups for this session
    // Number of time slots available per session day
    private Integer slotsPerDay;






    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // Programs allowed to register (Many-to-Many)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "lab_session_programs",
        joinColumns = @JoinColumn(name = "lab_session_id"),
        inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    @Builder.Default
    private Set<Program> allowedPrograms = new HashSet<>();

    // If true, all programs can register (overrides allowedPrograms)
    @Column(name = "open_to_all_programs")
    @Builder.Default
    private Boolean openToAllPrograms = false;

    // Registration deadline
    private LocalDateTime registrationDeadline;

    // Notes for students
    @Column(length = 2000)
    private String instructions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Calculate total weeks the session runs
    public int getDurationWeeks() {
        if (startDate == null || endDate == null) return 1;
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return Math.max(1, (int) Math.ceil((days + 1) / 7.0));
    }

    // Calculate total capacity (slots per day × max students × number of session days in the period)
    public int getTotalCapacity() {
        return slotsPerDay * maxStudentsPerSlot;
    }

    public boolean isOpen() {
        return status == SessionStatus.OPEN;
    }

    // Check if a student's program can register
    public boolean isOpenToProgram(Program studentProgram) {
        if (Boolean.TRUE.equals(openToAllPrograms)) {
            return true;
        }
        if (studentProgram == null) {
            return false;
        }
        return allowedPrograms.contains(studentProgram);
    }

    // Get session days as a Set
    public Set<String> getSessionDaysSet() {
        if (sessionDays == null || sessionDays.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(sessionDays.split(",")));
    }

    // Set session days from a Set
    public void setSessionDaysFromSet(Set<String> days) {
        if (days == null || days.isEmpty()) {
            this.sessionDays = null;
        } else {
            this.sessionDays = String.join(",", days);
        }
    }

    @Override
    public String toString() {
        return "LabSession{id=" + id + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabSession)) return false;
        LabSession that = (LabSession) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
