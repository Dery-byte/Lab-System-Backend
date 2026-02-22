package com.labregistration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"registration_id", "session_date"}),
    indexes = {
        @Index(name = "idx_attendance_reg", columnList = "registration_id"),
        @Index(name = "idx_attendance_date", columnList = "session_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Builder.Default
    private Boolean present = false;

    // Check-in time
    private LocalDateTime checkInTime;

    // Check-out time
    private LocalDateTime checkOutTime;

    // Notes about the attendance
    @Column(length = 500)
    private String notes;

    // Marked by (lab manager or system)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Attendance{id=" + id + ", date=" + sessionDate + ", present=" + present + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attendance)) return false;
        Attendance that = (Attendance) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
