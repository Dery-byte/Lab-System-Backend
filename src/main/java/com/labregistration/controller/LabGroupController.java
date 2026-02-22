package com.labregistration.controller;

import com.labregistration.dto.LabGroupDTO;
import com.labregistration.dto.request.CreateLabGroupRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.service.LabGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class LabGroupController {

    private final LabGroupService labGroupService;

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<LabGroupDTO>>> getGroupsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(labGroupService.getGroupsBySessionId(sessionId)));
    }

    @GetMapping("/session/{sessionId}/with-members")
    public ResponseEntity<ApiResponse<List<LabGroupDTO>>> getGroupsBySessionWithMembers(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(labGroupService.getGroupsBySessionIdWithMembers(sessionId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabGroupDTO>> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(labGroupService.getGroupById(id)));
    }

    @PostMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<LabGroupDTO>> createGroup(
            @PathVariable Long sessionId,
            @Valid @RequestBody CreateLabGroupRequest request) {
        LabGroupDTO group = labGroupService.createGroup(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Group created", group));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<LabGroupDTO>> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody CreateLabGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Group updated", labGroupService.updateGroup(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        labGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Group deleted", null));
    }
}
