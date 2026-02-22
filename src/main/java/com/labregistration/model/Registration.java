package com.labregistration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "lab_session_id"}),
    indexes = {
        @Index(name = "idx_reg_student", columnList = "student_id"),
        @Index(name = "idx_reg_session", columnList = "lab_session_id"),
        @Index(name = "idx_reg_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private boolean active; // <-- add th

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_session_id", nullable = false)
    private LabSession labSession;

    // The assigned time slot for ALL weeks of the session
    // Student attends this same slot every week
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_group_id")
    private LabGroup labGroup;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING;

    // Timestamps
    private LocalDateTime registeredAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    // If waitlisted, position in waitlist
    private Integer waitlistPosition;

    // Notes from student
    @Column(length = 500)
    private String studentNotes;

    // Notes from admin/manager
    @Column(length = 500)
    private String adminNotes;

    // Attendance tracking
    @Builder.Default
    private Integer attendedSessions = 0;

    @Builder.Default
    private Integer totalSessions = 0;

    // Score/Grade (optional)
    private Double score;
    private String grade;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = RegistrationStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.waitlistPosition = null;
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = RegistrationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void waitlist(int position) {
        this.status = RegistrationStatus.WAITLISTED;
        this.waitlistPosition = position;
    }

    public boolean isActive() {
        return status == RegistrationStatus.PENDING || 
               status == RegistrationStatus.CONFIRMED || 
               status == RegistrationStatus.WAITLISTED;
    }

    public double getAttendancePercentage() {
        if (totalSessions == 0) return 0;
        return (attendedSessions * 100.0) / totalSessions;
    }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", status=" + status + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registration)) return false;
        Registration that = (Registration) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
