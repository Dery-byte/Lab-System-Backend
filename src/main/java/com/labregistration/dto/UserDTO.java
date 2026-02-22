package com.labregistration.dto;

import com.labregistration.model.Level;
import com.labregistration.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String studentId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private Long programId;
    private String programName;
    private String departmentName;
    private String facultyName;
    private Level level;
    private String levelDisplayName;
    private Role role;
    private Boolean enabled;
    private Boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
