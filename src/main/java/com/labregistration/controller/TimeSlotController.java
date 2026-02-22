//package com.labregistration.controller;
//import com.labregistration.dto.TimeSlotDTO;
//import com.labregistration.dto.response.ApiResponse;
//import com.labregistration.service.TimeSlotService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class TimeSlotController {
//
//    private final TimeSlotService timeSlotService;
//
//    // ─── Student Endpoints ───────────────────────────────────────────────────────
//
//    // All slots for a session (for browsing)
//    @GetMapping("/lab-sessions/{sessionId}/slots")
//    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAllSlots(@PathVariable Long sessionId) {
//        return ResponseEntity.ok(ApiResponse.success("Time slots", timeSlotService.getSlotsBySession(sessionId)));
//    }
//
//    // Only available (not full) slots — used for registration slot picker
//    @GetMapping("/lab-sessions/{sessionId}/available-slots")
//    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAvailableSlots(@PathVariable Long sessionId) {
//        return ResponseEntity.ok(ApiResponse.success("Available slots", timeSlotService.getAvailableSlots(sessionId)));
//    }
//
//    // Upcoming slots only
//    @GetMapping("/lab-sessions/{sessionId}/upcoming-slots")
//    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getUpcomingSlots(@PathVariable Long sessionId) {
//        return ResponseEntity.ok(ApiResponse.success("Upcoming slots", timeSlotService.getUpcomingSlots(sessionId)));
//    }
//
//    // Slots for a specific date
//    @GetMapping("/lab-sessions/{sessionId}/slots/by-date")
//    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getSlotsByDate(
//            @PathVariable Long sessionId,
//            @RequestParam LocalDate date) {
//        return ResponseEntity.ok(ApiResponse.success("Slots for date", timeSlotService.getSlotsByDate(sessionId, date)));
//    }
//
//    // Slot capacity summary
//    @GetMapping("/lab-sessions/{sessionId}/slots/summary")
//    public ResponseEntity<ApiResponse<TimeSlotService.SlotSummary>> getSlotSummary(@PathVariable Long sessionId) {
//        return ResponseEntity.ok(ApiResponse.success("Slot summary", timeSlotService.getSlotSummary(sessionId)));
//    }
//
//    // Single slot by ID
//    @GetMapping("/slots/{slotId}")
//    public ResponseEntity<ApiResponse<TimeSlotDTO>> getSlotById(@PathVariable Long slotId) {
//        return ResponseEntity.ok(ApiResponse.success("Time slot", timeSlotService.getSlotById(slotId)));
//    }
//
//    // ─── Admin / Lab Manager Endpoints ──────────────────────────────────────────
//
//    @PostMapping("/lab-sessions/{sessionId}/slots")
//    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
//    public ResponseEntity<ApiResponse<TimeSlotDTO>> createSlot(
//            @PathVariable Long sessionId,
//            @RequestBody TimeSlotDTO request) {
//        return ResponseEntity.ok(ApiResponse.success("Slot created", timeSlotService.createSlot(sessionId, request)));
//    }
//
//    @PutMapping("/slots/{slotId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
//    public ResponseEntity<ApiResponse<TimeSlotDTO>> updateSlot(
//            @PathVariable Long slotId,
//            @RequestBody TimeSlotDTO request) {
//        return ResponseEntity.ok(ApiResponse.success("Slot updated", timeSlotService.updateSlot(slotId, request)));
//    }
//
//    @PatchMapping("/slots/{slotId}/deactivate")
//    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
//    public ResponseEntity<ApiResponse<Void>> deactivateSlot(@PathVariable Long slotId) {
//        timeSlotService.deactivateSlot(slotId);
//        return ResponseEntity.ok(ApiResponse.success("Slot deactivated", null));
//    }
//
//    @PatchMapping("/slots/{slotId}/activate")
//    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
//    public ResponseEntity<ApiResponse<Void>> activateSlot(@PathVariable Long slotId) {
//        timeSlotService.activateSlot(slotId);
//        return ResponseEntity.ok(ApiResponse.success("Slot activated", null));
//    }
//
//    @DeleteMapping("/slots/{slotId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
//    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable Long slotId) {
//        timeSlotService.deleteSlot(slotId);
//        return ResponseEntity.ok(ApiResponse.success("Slot deleted", null));
//    }
//}




package com.labregistration.controller;

import com.labregistration.dto.TimeSlotDTO;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    // ─── Student / Public Endpoints ──────────────────────────────────────────────

    // GET /api/sessions/{sessionId}/time-slots
    @GetMapping("/sessions/{sessionId}/time-slots")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAllSlots(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Time slots", timeSlotService.getSlotsBySession(sessionId)));
    }

    // GET /api/sessions/{sessionId}/time-slots/available
    @GetMapping("/sessions/{sessionId}/time-slots/available")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAvailableSlots(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Available slots", timeSlotService.getAvailableSlots(sessionId)));
    }

    // GET /api/sessions/{sessionId}/time-slots/upcoming
    @GetMapping("/sessions/{sessionId}/time-slots/upcoming")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getUpcomingSlots(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming slots", timeSlotService.getUpcomingSlots(sessionId)));
    }

    // GET /api/sessions/{sessionId}/time-slots/grouped
    @GetMapping("/sessions/{sessionId}/time-slots/grouped")
    public ResponseEntity<ApiResponse<Map<String, List<TimeSlotDTO>>>> getGroupedByDate(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Grouped slots", timeSlotService.getGroupedByDate(sessionId)));
    }

    // GET /api/sessions/{sessionId}/time-slots/by-date?date=2025-04-01
    @GetMapping("/sessions/{sessionId}/time-slots/by-date")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getSlotsByDate(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Slots for date", timeSlotService.getSlotsByDate(sessionId, date)));
    }

    // GET /api/sessions/{sessionId}/time-slots/registration-count
    @GetMapping("/sessions/{sessionId}/time-slots/registration-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getTotalRegistrations(@PathVariable Long sessionId) {
        int total = timeSlotService.getTotalRegistrations(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Registration count", Map.of("totalRegistrations", total)));
    }

    // GET /api/time-slots/{slotId}
    @GetMapping("/time-slots/{slotId}")
    public ResponseEntity<ApiResponse<TimeSlotDTO>> getSlotById(@PathVariable Long slotId) {
        return ResponseEntity.ok(ApiResponse.success("Time slot", timeSlotService.getSlotById(slotId)));
    }

    // ─── Admin / Lab Manager Endpoints ──────────────────────────────────────────

    // POST /api/sessions/{sessionId}/time-slots
    @PostMapping("/sessions/{sessionId}/time-slots")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<TimeSlotDTO>> createSlot(
            @PathVariable Long sessionId,
            @RequestBody TimeSlotDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Slot created", timeSlotService.createSlot(sessionId, request)));
    }

    // POST /api/sessions/{sessionId}/time-slots/bulk
    @PostMapping("/sessions/{sessionId}/time-slots/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> createBulk(
            @PathVariable Long sessionId,
            @RequestBody List<TimeSlotDTO> requests) {
        return ResponseEntity.ok(ApiResponse.success("Slots created", timeSlotService.createBulk(sessionId, requests)));
    }

    // PUT /api/time-slots/{slotId}
    @PutMapping("/time-slots/{slotId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<TimeSlotDTO>> updateSlot(
            @PathVariable Long slotId,
            @RequestBody TimeSlotDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Slot updated", timeSlotService.updateSlot(slotId, request)));
    }

    // PATCH /api/time-slots/{slotId}/toggle-active
    @PatchMapping("/time-slots/{slotId}/toggle-active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<TimeSlotDTO>> toggleActive(@PathVariable Long slotId) {
        return ResponseEntity.ok(ApiResponse.success("Slot toggled", timeSlotService.toggleActive(slotId)));
    }

    // DELETE /api/time-slots/{slotId}
    @DeleteMapping("/time-slots/{slotId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable Long slotId) {
        timeSlotService.deleteSlot(slotId);
        return ResponseEntity.ok(ApiResponse.success("Slot deleted", null));
    }
}