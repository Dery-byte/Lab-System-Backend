package com.labregistration.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {
    @NotNull(message = "Session ID is required")
    private Long labSessionId;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotEmpty(message = "At least one attendance record is required")
    private List<AttendanceRecord> attendances;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRecord {
        @NotNull(message = "Registration ID is required")
        private Long registrationId;

        @NotNull(message = "Present status is required")
        private Boolean present;

        private String notes;
    }
}
