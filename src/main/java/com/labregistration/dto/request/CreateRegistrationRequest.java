package com.labregistration.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRegistrationRequest {
    @NotNull(message = "Lab session ID is required")
    private Long labSessionId;

    // Optional: specific time slot preference
    // If not provided, system will auto-assign
    private Long timeSlotId;

    // Optional notes from student
    private String notes;
}
