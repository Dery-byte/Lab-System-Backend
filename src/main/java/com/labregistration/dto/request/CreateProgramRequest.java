package com.labregistration.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProgramRequest {
    @NotBlank(message = "Program code is required")
    private String code;

    @NotBlank(message = "Program name is required")
    private String name;

    private String description;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Integer durationYears;
    private String degreeType;
}
