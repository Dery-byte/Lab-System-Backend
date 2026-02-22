package com.labregistration.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFacultyRequest {
    @NotBlank(message = "Faculty code is required")
    private String code;

    @NotBlank(message = "Faculty name is required")
    private String name;

    private String description;
    private String dean;
}
