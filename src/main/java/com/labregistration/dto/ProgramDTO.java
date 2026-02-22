package com.labregistration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
    private String facultyName;
    private Integer durationYears;
    private String degreeType;
    private Boolean active;
    private int studentCount;
    private LocalDateTime createdAt;
}
