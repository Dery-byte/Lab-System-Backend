package com.labregistration.dto.request;

import com.labregistration.model.Level;
import com.labregistration.model.Semester;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {
    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    private String courseName;

    private String description;

    @NotNull(message = "Department ID is required")
    private Long departmentId;


    @NotBlank(message = "Department is required")
    private String department;



    @NotNull(message = "Level is required")
    private Level level;

    private Semester semester;
    private String academicYear;
    private Integer creditHours;
    private Long instructorId;
    private Boolean hasLab;


    private List<String> allowedDepartments;
    private Boolean openToAllDepartments;
}
