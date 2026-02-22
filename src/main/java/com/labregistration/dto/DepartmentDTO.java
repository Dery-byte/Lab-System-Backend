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
public class DepartmentDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long facultyId;
    private String facultyName;
    private String headOfDepartment;
    private Boolean active;
    private int programCount;
    private LocalDateTime createdAt;
}
