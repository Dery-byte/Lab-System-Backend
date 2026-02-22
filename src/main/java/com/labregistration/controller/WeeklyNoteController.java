package com.labregistration.controller;

import com.labregistration.dto.WeeklyNoteDTO;
import com.labregistration.dto.request.UpdateWeeklyNoteRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.service.WeeklyNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weekly-notes")
@RequiredArgsConstructor
public class WeeklyNoteController {

    private final WeeklyNoteService weeklyNoteService;

    /**
     * Get all weekly notes for a session (Admin/Lab Manager - includes unpublished)
     */
    @GetMapping("/session/{sessionId}/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<List<WeeklyNoteDTO>>> getAllWeeklyNotes(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(weeklyNoteService.getAllWeeklyNotes(sessionId)));
    }

    /**
     * Get published weekly notes for a session (Students - only published content)
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<WeeklyNoteDTO>>> getPublishedWeeklyNotes(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(weeklyNoteService.getPublishedWeeklyNotes(sessionId)));
    }

    /**
     * Get a specific weekly note
     */
    @GetMapping("/session/{sessionId}/week/{weekNumber}")
    public ResponseEntity<ApiResponse<WeeklyNoteDTO>> getWeeklyNote(
            @PathVariable Long sessionId,
            @PathVariable Integer weekNumber) {
        return ResponseEntity.ok(ApiResponse.success(weeklyNoteService.getWeeklyNote(sessionId, weekNumber)));
    }

    /**
     * Update or create a weekly note (Admin/Lab Manager)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<WeeklyNoteDTO>> updateWeeklyNote(
            @Valid @RequestBody UpdateWeeklyNoteRequest request,
            Authentication authentication) {
        WeeklyNoteDTO result = weeklyNoteService.updateWeeklyNote(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Weekly note updated successfully", result));
    }

    /**
     * Publish a weekly note
     */
    @PatchMapping("/session/{sessionId}/week/{weekNumber}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<WeeklyNoteDTO>> publishWeeklyNote(
            @PathVariable Long sessionId,
            @PathVariable Integer weekNumber,
            Authentication authentication) {
        WeeklyNoteDTO result = weeklyNoteService.publishWeeklyNote(sessionId, weekNumber, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Weekly note published", result));
    }

    /**
     * Unpublish a weekly note
     */
    @PatchMapping("/session/{sessionId}/week/{weekNumber}/unpublish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<WeeklyNoteDTO>> unpublishWeeklyNote(
            @PathVariable Long sessionId,
            @PathVariable Integer weekNumber,
            Authentication authentication) {
        WeeklyNoteDTO result = weeklyNoteService.unpublishWeeklyNote(sessionId, weekNumber, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Weekly note unpublished", result));
    }
}
