package com.labregistration.controller;

import com.labregistration.dto.UserDTO;
import com.labregistration.dto.request.CreateLabManagerRequest;
import com.labregistration.dto.response.ApiResponse;
import com.labregistration.model.User;
import com.labregistration.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(user.getId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllStudents() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllStudents()));
    }

    @GetMapping("/students/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchStudents(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(userService.searchStudents(q)));
    }
}
