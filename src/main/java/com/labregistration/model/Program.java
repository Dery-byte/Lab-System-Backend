package com.labregistration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Duration in years (e.g., 4 for BSc, 2 for MSc)
    @Builder.Default
    private Integer durationYears = 4;

    // Degree type (BSc, MSc, PhD, etc.)
    private String degreeType;

    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : null;
    }

    public String getFacultyName() {
        return department != null && department.getFaculty() != null 
            ? department.getFaculty().getName() : null;
    }

    @Override
    public String toString() {
        return "Program{id=" + id + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Program)) return false;
        Program program = (Program) o;
        return id != null && id.equals(program.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
