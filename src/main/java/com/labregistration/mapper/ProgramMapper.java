package com.labregistration.mapper;

import com.labregistration.dto.ProgramDTO;
import com.labregistration.model.Program;
import com.labregistration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgramMapper {

    private final UserRepository userRepository;

    public ProgramDTO toDTO(Program program) {
        if (program == null) return null;

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
                .studentCount((int) userRepository.findByProgramId(program.getId()).size())
                .createdAt(program.getCreatedAt())
                .build();
    }
}
