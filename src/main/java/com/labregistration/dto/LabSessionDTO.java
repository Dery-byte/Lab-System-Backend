package com.labregistration.dto;

import com.labregistration.model.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabSessionDTO {
    private Long id;
    private String name;
    private String description;
    private String labRoom;
    
    // Recurring session dates
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Session days
    private Set<String> sessionDays;
    private int durationWeeks;
    
    // Capacity
    private Integer maxStudentsPerSlot;
    private Integer slotsPerDay;
    private int totalCapacity;
    private int currentRegistrations;
    private int availableSlots;

    private Integer maxGroupSize;
    private Integer maxGroups;


    private SessionStatus status;
    
    // Course info
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String departmentName;
    
    // Created by
    private Long createdById;
    private String createdByName;
    
    // Program access
    private List<ProgramDTO> allowedPrograms;
    private Boolean openToAllPrograms;
    
    // Additional info
    private LocalDateTime registrationDeadline;
    private String instructions;
    
    // Time slots (grouped by date)
    private List<TimeSlotDTO> timeSlots;
    
    private LocalDateTime createdAt;
}
