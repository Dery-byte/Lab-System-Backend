package com.labregistration.controller;

import com.labregistration.dto.DepartmentDTO;
import com.labregistration.dto.request.CreateDepartmentRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllDepartments()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getActiveDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getActiveDepartments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartmentById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartmentByCode(code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentDTO dept = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Department created", dept));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(@PathVariable Long id, @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Department updated", departmentService.updateDepartment(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated", null));
    }




    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateDepartment(@PathVariable Long id) {
        departmentService.deactivateDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated", null));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateDepartment(@PathVariable Long id) {
        departmentService.activateDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department activated", null));
    }
}
