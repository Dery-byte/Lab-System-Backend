package com.labregistration.service;

import com.labregistration.dto.ProgramDTO;
import com.labregistration.dto.request.CreateProgramRequest;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.model.Department;
import com.labregistration.model.Program;
import com.labregistration.repository.DepartmentRepository;
import com.labregistration.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;

    public ProgramDTO createProgram(CreateProgramRequest request) {

        if (programRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Program code already exists.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Program program = Program.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .department(department)
                .durationYears(
                        request.getDurationYears() != null
                                ? request.getDurationYears()
                                : 4
                )
                .degreeType(request.getDegreeType())
                .active(true)
                .build();

        Program saved = programRepository.save(program);

        return mapToDTO(saved);
    }

    public ProgramDTO getById(Long id) {

        Program program = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Program", "id", id));

        return mapToDTO(program);
    }

    public List<ProgramDTO> getAll() {
        return programRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ProgramDTO> getByDepartment(Long departmentId) {

        return programRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteProgram(Long id) {

        Program program = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Program", "id", id));

        programRepository.delete(program);
    }

    public ProgramDTO deactivateProgram(Long id) {

        Program program = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Program", "id", id));

        program.setActive(false);

        return mapToDTO(programRepository.save(program));
    }


    public ProgramDTO activateProgram(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Program", "id", id));
        program.setActive(true);
        return mapToDTO(programRepository.save(program));
    }

    private ProgramDTO mapToDTO(Program program) {

        return ProgramDTO.builder()
                .id(program.getId())
                .code(program.getCode())
                .name(program.getName())
                .description(program.getDescription())
                .departmentName(program.getDepartmentName())
                .facultyName(program.getFacultyName())
                .durationYears(program.getDurationYears())
                .degreeType(program.getDegreeType())
                .active(program.getActive())
                .build();
    }







    public ProgramDTO updateProgram(Long id, CreateProgramRequest request) {

        Program program = programRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Program", "id", id));

        // Optional: check if updating code would conflict
        if (!program.getCode().equals(request.getCode()) &&
                programRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Program code already exists.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        program.setCode(request.getCode());
        program.setName(request.getName());
        program.setDescription(request.getDescription());
        program.setDepartment(department);
        program.setDurationYears(request.getDurationYears() != null ? request.getDurationYears() : 4);
        program.setDegreeType(request.getDegreeType());

        Program updated = programRepository.save(program);

        return mapToDTO(updated);
    }





    @Transactional
    public void toggleProgramStatus(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));
        program.setActive(!program.getActive());
        programRepository.save(program);
    }
}