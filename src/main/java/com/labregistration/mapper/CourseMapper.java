package com.labregistration.mapper;

import com.labregistration.dto.CourseDTO;
import com.labregistration.model.Course;
import com.labregistration.repository.LabSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final UserMapper userMapper;
    private final LabSessionRepository labSessionRepository;

    public CourseDTO toDTO(Course course) {
        if (course == null) return null;

        return CourseDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .department(course.getDepartmentName())
                .departmentId(course.getDepartment() != null ? course.getDepartment().getId() : null)
                .level(course.getLevel())
                .levelDisplayName(course.getLevel() != null ? course.getLevel().getDisplayName() : null)
                .semester(course.getSemester())
                .creditHours(course.getCreditHours())
                .active(course.getActive())
                .instructor(userMapper.toDTO(course.getInstructor()))
                .labSessionsCount((int) labSessionRepository.findByCourseId(course.getId()).size())
                .createdAt(course.getCreatedAt())
                .allowedDepartments(course.getAllowedDepartments())
                .allowedDepartmentsList(parseAllowedDepartments(course.getAllowedDepartments()))
                .openToAllDepartments(course.getOpenToAllDepartments())
                .build();
    }

    private List<String> parseAllowedDepartments(String allowedDepartments) {
        if (allowedDepartments == null || allowedDepartments.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(allowedDepartments.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
