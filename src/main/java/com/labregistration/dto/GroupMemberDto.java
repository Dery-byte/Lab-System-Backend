package com.labregistration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDto {
    private Long   id;
    private String fullName;
    private String studentId;
    private String username;
    private String profilePictureUrl;
}