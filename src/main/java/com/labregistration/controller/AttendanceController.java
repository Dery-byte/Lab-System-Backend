package com.labregistration.controller;

import com.labregistration.dto.AttendanceDTO;
import com.labregistration.dto.request.MarkAttendanceRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request,
            Authentication authentication) {
        List<AttendanceDTO> results = attendanceService.markAttendance(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully", results));
    }

    @GetMapping("/registration/{registrationId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceByRegistration(
            @PathVariable Long registrationId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByRegistration(registrationId)));
    }

    @GetMapping("/session/{sessionId}/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceBySessionAndDate(
            @PathVariable Long sessionId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceBySessionAndDate(sessionId, date)));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAllAttendanceBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAllAttendanceBySession(sessionId)));
    }
}
