package com.labregistration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Long id;
    private Long registrationId;
    private Long studentId;
    private String studentName;
    private String studentIdNumber;
    private LocalDate sessionDate;
    private Boolean present;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String notes;
    private String markedByName;
    private LocalDateTime createdAt;
}
