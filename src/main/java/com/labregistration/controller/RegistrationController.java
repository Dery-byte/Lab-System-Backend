package com.labregistration.controller;

import com.labregistration.dto.RegistrationDTO;
import com.labregistration.dto.request.CreateRegistrationRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.model.RegistrationStatus;
import com.labregistration.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RegistrationDTO>> createRegistration(
            @Valid @RequestBody CreateRegistrationRequest request,
            Authentication authentication) {
        RegistrationDTO registration = registrationService.createRegistration(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", registration));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<RegistrationDTO>>> getMyRegistrations(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                registrationService.getStudentRegistrations(authentication.getName())));
    }

    @GetMapping("/my/active")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<RegistrationDTO>>> getMyActiveRegistrations(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                registrationService.getActiveStudentRegistrations(authentication.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistrationDTO>> getRegistrationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(registrationService.getRegistrationById(id)));
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<List<RegistrationDTO>>> getRegistrationsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(registrationService.getRegistrationsBySessionId(sessionId)));
    }

    @GetMapping("/session/{sessionId}/slot/{slotNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<List<RegistrationDTO>>> getRegistrationsBySlot(
            @PathVariable Long sessionId, @PathVariable Integer slotNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                registrationService.getRegistrationsBySlotNumber(sessionId, slotNumber)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RegistrationDTO>> cancelRegistration(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Registration cancelled", 
                registrationService.cancelRegistration(id, authentication.getName())));
    }

    @PatchMapping("/{id}/change-slot")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<RegistrationDTO>> changeSlot(
            @PathVariable Long id, 
            @RequestParam Integer newSlotNumber,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Time slot changed", 
                registrationService.changeSlot(id, newSlotNumber, authentication.getName())));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<RegistrationDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam RegistrationStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", 
                registrationService.updateStatus(id, status)));
    }
}
