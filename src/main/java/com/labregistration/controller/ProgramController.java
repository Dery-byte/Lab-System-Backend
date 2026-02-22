package com.labregistration.controller;

import com.labregistration.dto.ProgramDTO;
import com.labregistration.dto.request.CreateProgramRequest;

import com.labregistration.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @PostMapping
    public ResponseEntity<ProgramDTO> createProgram(
            @RequestBody CreateProgramRequest request) {
        return ResponseEntity.ok(programService.createProgram(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProgramDTO>> getAll() {
        return ResponseEntity.ok(programService.getAll());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<ProgramDTO>> getByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(programService.getByDepartment(departmentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }

//    @PatchMapping("/{id}/deactivate")
//    public ResponseEntity<ProgramDTO> deactivateProgram(@PathVariable Long id) {
//        return ResponseEntity.ok(programService.deactivateProgram(id));
//    }
//
//
//    @PatchMapping("/{id}/activate")
//    public ResponseEntity<ProgramDTO> activateProgram(@PathVariable Long id) {
//        return ResponseEntity.ok(programService.activateProgram(id));
//    }


    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        programService.toggleProgramStatus(id);
        return ResponseEntity.noContent().build();
    }

    // PUT update program
    @PutMapping("/{id}")
    public ResponseEntity<ProgramDTO> update(
            @PathVariable Long id,
            @RequestBody CreateProgramRequest request
    ) {
        return ResponseEntity.ok(programService.updateProgram(id, request));
    }

}