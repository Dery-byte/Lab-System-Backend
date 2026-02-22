package com.labregistration.controller;

import com.labregistration.dto.UpdateLabManagerRequest;
import com.labregistration.dto.UserDTO;
import com.labregistration.dto.request.CreateLabManagerRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.model.RegistrationStatus;
import com.labregistration.model.SessionStatus;
import com.labregistration.service.LabSessionService;
import com.labregistration.service.RegistrationService;
import com.labregistration.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final UserService userService;
    private final LabSessionService labSessionService;
    private final RegistrationService registrationService;

    // Lab Manager Management
    @GetMapping("/lab-managers")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllLabManagers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllLabManagers()));
    }

    @PostMapping("/create-lab-manager")
    public ResponseEntity<ApiResponse<UserDTO>> createLabManager(@Valid @RequestBody CreateLabManagerRequest request) {
        UserDTO labManager = userService.createLabManager(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lab Manager created successfully", labManager));
    }

    @PatchMapping("/lab-managers/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateLabManager(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Lab Manager deactivated", null));
    }


    @PutMapping("/lab-managers/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateLabManager(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLabManagerRequest request) {
        UserDTO labManager = userService.updateLabManager(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lab Manager updated successfully", labManager));
    }


    @PatchMapping("/lab-managers/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateLabManager(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Lab Manager activated", null));
    }

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalStudents", userService.countStudents());
        stats.put("totalLabManagers", userService.countLabManagers());
        stats.put("openSessions", labSessionService.countByStatus(SessionStatus.OPEN));
        stats.put("draftSessions", labSessionService.countByStatus(SessionStatus.DRAFT));
        stats.put("closedSessions", labSessionService.countByStatus(SessionStatus.CLOSED));
        stats.put("confirmedRegistrations", registrationService.countByStatus(RegistrationStatus.CONFIRMED));
        stats.put("waitlistedRegistrations", registrationService.countByStatus(RegistrationStatus.WAITLISTED));
        stats.put("completedRegistrations", registrationService.countByStatus(RegistrationStatus.COMPLETED));
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }





}
