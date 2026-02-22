package com.labregistration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "lab_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private Integer groupNumber;

    @Column(nullable = false)
    private Integer maxSize;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_session_id", nullable = false)
    private LabSession labSession;

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

    @Override
    public String toString() {
        return "LabGroup{id=" + id + ", groupName='" + groupName + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabGroup)) return false;
        LabGroup labGroup = (LabGroup) o;
        return id != null && id.equals(labGroup.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
