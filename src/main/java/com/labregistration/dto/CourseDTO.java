package com.labregistration.dto;

import com.labregistration.model.Level;
import com.labregistration.model.Semester;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Long departmentId;
    private String departmentName;
    private String department;
    private Level level;
    private String levelDisplayName;
    private Semester semester;
    private String semesterDisplayName;
    private String academicYear;
    private Integer creditHours;
    private Long instructorId;
    private String instructorName;
    private Boolean active;
    private Boolean hasLab;
    private int labSessionsCount;
    private LocalDateTime createdAt;


    private UserDTO instructor;
    private String allowedDepartments;
    private List<String> allowedDepartmentsList;
    private Boolean openToAllDepartments;
}
