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
public class FacultyDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String dean;
    private Boolean active;
    private int departmentCount;
    private LocalDateTime createdAt;
}
