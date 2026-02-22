package com.labregistration.mapper;

import com.labregistration.dto.UserDTO;
import com.labregistration.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .programId(user.getProgram() != null ? user.getProgram().getId() : null)
                .programName(user.getProgramName())
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .facultyName(user.getProgram() != null ? user.getProgram().getFacultyName() : null)
                .level(user.getLevel())
                .levelDisplayName(user.getLevel() != null ? user.getLevel().getDisplayName() : null)
                .role(user.getRole())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
