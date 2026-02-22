package com.labregistration.controller;

import com.labregistration.dto.DepartmentDTO;
import com.labregistration.dto.FacultyDTO;
import com.labregistration.dto.ProgramDTO;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.model.Department;
import com.labregistration.model.Faculty;
import com.labregistration.model.Program;
import com.labregistration.repository.DepartmentRepository;
import com.labregistration.repository.FacultyRepository;
import com.labregistration.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;

    @GetMapping("/faculties")
    public ResponseEntity<ApiResponse<List<FacultyDTO>>> getActiveFaculties() {
        List<FacultyDTO> faculties = facultyRepository.findByActiveTrue().stream()
                .map(this::toFacultyDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(faculties));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getActiveDepartments() {
        List<DepartmentDTO> departments = departmentRepository.findByActiveTrue().stream()
                .map(this::toDepartmentDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/departments/faculty/{facultyId}")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getDepartmentsByFaculty(@PathVariable Long facultyId) {
        List<DepartmentDTO> departments = departmentRepository.findActiveByFacultyId(facultyId).stream()
                .map(this::toDepartmentDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/programs")
    public ResponseEntity<ApiResponse<List<ProgramDTO>>> getActivePrograms() {
        List<ProgramDTO> programs = programRepository.findByActiveTrue().stream()
                .map(this::toProgramDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(programs));
    }

    @GetMapping("/programs/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<ProgramDTO>>> getProgramsByDepartment(@PathVariable Long departmentId) {
        List<ProgramDTO> programs = programRepository.findActiveByDepartmentId(departmentId).stream()
                .map(this::toProgramDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(programs));
    }

    private FacultyDTO toFacultyDTO(Faculty faculty) {
        return FacultyDTO.builder()
                .id(faculty.getId())
                .code(faculty.getCode())
                .name(faculty.getName())
                .description(faculty.getDescription())
                .dean(faculty.getDean())
                .active(faculty.getActive())
                .departmentCount(departmentRepository.findByFacultyId(faculty.getId()).size())
                .createdAt(faculty.getCreatedAt())
                .build();
    }

    private DepartmentDTO toDepartmentDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .description(dept.getDescription())
                .facultyId(dept.getFaculty() != null ? dept.getFaculty().getId() : null)
                .facultyName(dept.getFacultyName())
                .headOfDepartment(dept.getHeadOfDepartment())
                .active(dept.getActive())
                .programCount(programRepository.findByDepartmentId(dept.getId()).size())
                .createdAt(dept.getCreatedAt())
                .build();
    }

    private ProgramDTO toProgramDTO(Program program) {
        return ProgramDTO.builder()
                .id(program.getId())
                .code(program.getCode())
                .name(program.getName())
                .description(program.getDescription())
                .departmentId(program.getDepartment() != null ? program.getDepartment().getId() : null)
                .departmentName(program.getDepartmentName())
                .facultyName(program.getFacultyName())
                .durationYears(program.getDurationYears())
                .degreeType(program.getDegreeType())
                .active(program.getActive())
                .createdAt(program.getCreatedAt())
                .build();
    }
}
