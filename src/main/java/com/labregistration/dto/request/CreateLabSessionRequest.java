package com.labregistration.dto.request;

import com.labregistration.model.SessionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLabSessionRequest {
    @NotBlank(message = "Session name is required")
    private String name;

    private String description;

    @NotBlank(message = "Lab room is required")
    private String labRoom;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    // Days of week when session occurs
    @NotEmpty(message = "At least one session day is required")
    private Set<String> sessionDays;

//    @NotNull(message = "Max students per slot is required")
//    @Min(value = 1, message = "Must have at least 1 student per slot")
    private Integer maxStudentsPerSlot;

//    @NotNull(message = "Slots per day is required")
//    @Min(value = 1, message = "Must have at least 1 slot per day")
    private Integer slotsPerDay;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    // Programs allowed to register
    private Set<Long> allowedProgramIds;

    // If true, all programs can register
    private Boolean openToAllPrograms;

    // Optional registration deadline
    private LocalDateTime registrationDeadline;

    // Instructions for students
    private String instructions;


    private SessionStatus status;




    @NotNull(message = "Maximum group size is required")
    @Min(value = 1, message = "Group size must be at least 1")
    @Max(value = 50, message = "Group size cannot exceed 50")
    private Integer maxGroupSize;

    @NotNull(message = "Maximum number of groups is required")
    @Min(value = 1, message = "Must have at least 1 group")
    @Max(value = 20, message = "Cannot exceed 20 groups")
    private Integer maxGroups;

    private boolean autoCreateGroups = true; // Automatically create groups based on maxGroups
}
