package com.labregistration.controller;

import com.labregistration.dto.LabSessionDTO;
import com.labregistration.dto.request.CreateLabSessionRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.model.SessionStatus;
import com.labregistration.model.User;
import com.labregistration.service.LabSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab-sessions")
@RequiredArgsConstructor
public class LabSessionController {

    private final LabSessionService labSessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LabSessionDTO>>> getAllLabSessions() {
        return ResponseEntity.ok(ApiResponse.success(labSessionService.getAllLabSessions()));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<LabSessionDTO>>> getAvailableLabSessions() {
        return ResponseEntity.ok(ApiResponse.success(labSessionService.getAvailableLabSessions()));
    }

    @GetMapping("/available-for-me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<LabSessionDTO>>> getAvailableSessionsForStudent(Authentication auth) {
        User user = (User) auth.getPrincipal();
        Long programId = user.getProgram() != null ? user.getProgram().getId() : null;
        if (programId == null) {
            return ResponseEntity.ok(ApiResponse.success(labSessionService.getAvailableLabSessions()));
        }
        return ResponseEntity.ok(ApiResponse.success(labSessionService.getAvailableSessionsForProgram(programId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<LabSessionDTO>>> getLabSessionsByStatus(@PathVariable SessionStatus status) {
        return ResponseEntity.ok(ApiResponse.success(labSessionService.getLabSessionsByStatus(status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabSessionDTO>> getLabSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(labSessionService.getLabSessionById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<LabSessionDTO>> createLabSession(
            @Valid @RequestBody CreateLabSessionRequest request,
            Authentication authentication) {
        LabSessionDTO session = labSessionService.createLabSession(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lab session created successfully", session));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<LabSessionDTO>> updateLabSession(
            @PathVariable Long id,
            @Valid @RequestBody CreateLabSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Lab session updated", 
                labSessionService.updateLabSession(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<LabSessionDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam SessionStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", 
                labSessionService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteLabSession(@PathVariable Long id) {
        labSessionService.deleteLabSession(id);
        return ResponseEntity.ok(ApiResponse.success("Lab session deleted", null));
    }
}
