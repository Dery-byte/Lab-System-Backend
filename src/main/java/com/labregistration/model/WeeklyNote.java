package com.labregistration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_notes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lab_session_id", "week_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_session_id", nullable = false)
    private LabSession labSession;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "title")
    private String title;

    @Column(name = "content", length = 4000)
    private String content;

    @Column(name = "learning_objectives", length = 2000)
    private String learningObjectives;

    @Column(name = "materials_needed", length = 1000)
    private String materialsNeeded;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

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

    // Helper to get a display name for the week
    public String getDisplayName() {
        return "Week " + weekNumber + ": " + (title != null ? title : "");
    }

    @Override
    public String toString() {
        return "WeeklyNote{id=" + id + ", labSession=" + (labSession != null ? labSession.getId() : null) + ", week=" + weekNumber + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeeklyNote)) return false;
        WeeklyNote that = (WeeklyNote) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
