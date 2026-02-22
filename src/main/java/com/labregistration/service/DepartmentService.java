package com.labregistration.service;

import com.labregistration.dto.DepartmentDTO;
import com.labregistration.dto.request.CreateDepartmentRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.model.Department;
import com.labregistration.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<DepartmentDTO> getActiveDepartments() {
        return departmentRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return toDTO(dept);
    }

    public DepartmentDTO getDepartmentByCode(String code) {
        Department dept = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "code", code));
        return toDTO(dept);
    }

    @Transactional
    public DepartmentDTO createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Department code already exists");
        }
        if (departmentRepository.existsByName(request.getName())) {
            throw new BadRequestException("Department name already exists");
        }

        Department dept = Department.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .faculty(request.getFaculty())
                .headOfDepartment(request.getHeadOfDepartment())
                .active(true)
                .build();

        dept = departmentRepository.save(dept);
        log.info("Department created: {}", dept.getCode());
        return toDTO(dept);
    }

    @Transactional
    public DepartmentDTO updateDepartment(Long id, CreateDepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        dept.setCode(request.getCode().toUpperCase());
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        dept.setFaculty(request.getFaculty());
        dept.setHeadOfDepartment(request.getHeadOfDepartment());

        dept = departmentRepository.save(dept);
        return toDTO(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setActive(false);
        departmentRepository.save(dept);
    }

    private DepartmentDTO toDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .description(dept.getDescription())
                .facultyName(dept.getFaculty().getName())
                .headOfDepartment(dept.getHeadOfDepartment())
                .active(dept.getActive())
                .build();
    }






    @Transactional
    public void deactivateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        department.setActive(false);
        departmentRepository.save(department);
        log.info("Deactivated department: {}", department.getName());
    }

    @Transactional
    public void activateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        department.setActive(true);
        departmentRepository.save(department);
        log.info("Activated department: {}", department.getName());
    }
}
