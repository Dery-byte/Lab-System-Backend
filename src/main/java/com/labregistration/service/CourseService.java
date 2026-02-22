package com.labregistration.service;

import com.labregistration.dto.CourseDTO;
import com.labregistration.dto.request.CreateCourseRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.mapper.CourseMapper;
import com.labregistration.model.Course;
import com.labregistration.model.Department;
import com.labregistration.model.User;
import com.labregistration.repository.CourseRepository;
import com.labregistration.repository.DepartmentRepository;
import com.labregistration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseMapper courseMapper;

    @Transactional
    public CourseDTO createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new BadRequestException("Course code already exists");
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .departmentName(request.getDepartment())
                .level(request.getLevel())
                .semester(request.getSemester())
                .creditHours(request.getCreditHours())
                .active(true)
                .openToAllDepartments(request.getOpenToAllDepartments() != null ? request.getOpenToAllDepartments() : false)
                .build();

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            course.setDepartment(dept);
            course.setDepartmentName(dept.getName());
        }

        if (request.getAllowedDepartments() != null && !request.getAllowedDepartments().isEmpty()) {
            course.setAllowedDepartments(String.join(",", request.getAllowedDepartments()));
        }

        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getInstructorId()));
            course.setInstructor(instructor);
        }

        course = courseRepository.save(course);
        log.info("Course created: {}", course.getCourseCode());
        return courseMapper.toDTO(course);
    }

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return courseMapper.toDTO(course);
    }

    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getActiveCourses() {
        return courseRepository.findByActiveTrue().stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDTO updateCourse(Long id, CreateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setDepartmentName(request.getDepartment());
        course.setLevel(request.getLevel());
        course.setSemester(request.getSemester());
        course.setCreditHours(request.getCreditHours());

        if (request.getOpenToAllDepartments() != null) {
            course.setOpenToAllDepartments(request.getOpenToAllDepartments());
        }

        if (request.getAllowedDepartments() != null) {
            course.setAllowedDepartments(String.join(",", request.getAllowedDepartments()));
        }

        course = courseRepository.save(course);
        return courseMapper.toDTO(course);
    }

    @Transactional
    public void toggleCourseStatus(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        course.setActive(!course.getActive());
        courseRepository.save(course);
    }
}
