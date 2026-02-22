package com.labregistration.dto.request;

import com.labregistration.model.Faculty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequest {
    @NotBlank(message = "Department code is required")
    private String code;

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;

//    @NotNull(message = "Faculty ID is required")
    private Long facultyId;


//
//    private Faculty faculty;

    private String headOfDepartment;
}
