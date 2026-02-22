package com.labregistration.controller;
import com.labregistration.dto.FacultyDTO;
import com.labregistration.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    // ── Public / student-accessible ───────────────────────────────────────────

    /** All active faculties — available to any authenticated user */
    @GetMapping("/active")
    public ResponseEntity<List<FacultyDTO>> getActive() {
        return ResponseEntity.ok(facultyService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<FacultyDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(facultyService.getByCode(code));
    }

    // ── Admin-only ────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('LAB_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<List<FacultyDTO>> getAll() {
        return ResponseEntity.ok(facultyService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<FacultyDTO> create(@Valid @RequestBody FacultyDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultyService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<FacultyDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FacultyDTO dto) {
        return ResponseEntity.ok(facultyService.update(id, dto));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('LAB_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<FacultyDTO> activate(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.setActive(id, true));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('LAB_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<FacultyDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.setActive(id, false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}