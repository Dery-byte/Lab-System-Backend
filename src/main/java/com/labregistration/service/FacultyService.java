package com.labregistration.service;
import com.labregistration.dto.FacultyDTO;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.model.Faculty;
import com.labregistration.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public List<FacultyDTO> getAll() {
        return facultyRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FacultyDTO> getAllActive() {
        return facultyRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FacultyDTO getById(Long id) {
        return toDto(findOrThrow(id));
    }

    public FacultyDTO getByCode(String code) {
        Faculty faculty = facultyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "code", code));
        return toDto(faculty);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public FacultyDTO create(FacultyDTO dto) {
        if (facultyRepository.existsByCode(dto.getCode().trim().toUpperCase())) {
            throw new BadRequestException("Faculty with code '" + dto.getCode() + "' already exists");
        }
        if (facultyRepository.existsByName(dto.getName().trim())) {
            throw new BadRequestException("Faculty with name '" + dto.getName() + "' already exists");
        }

        Faculty faculty = Faculty.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .dean(dto.getDean())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        faculty = facultyRepository.save(faculty);
        log.info("Created faculty: {} ({})", faculty.getName(), faculty.getCode());
        return toDto(faculty);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public FacultyDTO update(Long id, FacultyDTO dto) {
        Faculty faculty = findOrThrow(id);

        String newCode = dto.getCode().trim().toUpperCase();
        String newName = dto.getName().trim();

        if (facultyRepository.existsByCodeAndIdNot(newCode, id)) {
            throw new BadRequestException("Faculty with code '" + newCode + "' already exists");
        }
        if (facultyRepository.existsByNameAndIdNot(newName, id)) {
            throw new BadRequestException("Faculty with name '" + newName + "' already exists");
        }

        faculty.setCode(newCode);
        faculty.setName(newName);
        faculty.setDescription(dto.getDescription());
        faculty.setDean(dto.getDean());
        if (dto.getActive() != null) {
            faculty.setActive(dto.getActive());
        }

        faculty = facultyRepository.save(faculty);
        log.info("Updated faculty: {} ({})", faculty.getName(), faculty.getCode());
        return toDto(faculty);
    }

    // ── Activate / Deactivate ─────────────────────────────────────────────────

    @Transactional
    public FacultyDTO setActive(Long id, boolean active) {
        Faculty faculty = findOrThrow(id);
        faculty.setActive(active);
        faculty = facultyRepository.save(faculty);
        log.info("Faculty {} ({}): active={}", faculty.getName(), faculty.getCode(), active);
        return toDto(faculty);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        Faculty faculty = findOrThrow(id);
        facultyRepository.delete(faculty);
        log.info("Deleted faculty: {} ({})", faculty.getName(), faculty.getCode());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Faculty findOrThrow(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));
    }

    private FacultyDTO toDto(Faculty f) {
        return FacultyDTO.builder()
                .id(f.getId())
                .code(f.getCode())
                .name(f.getName())
                .description(f.getDescription())
                .dean(f.getDean())
                .active(f.getActive())
                .createdAt(f.getCreatedAt())
                .build();
    }
}

