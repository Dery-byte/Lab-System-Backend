package com.labregistration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {
    private Long id;
    private Long labSessionId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotNumber;
    private Integer maxStudents;
    private Integer currentCount;
    private int availableSlots;
    private boolean isFull;
    private Boolean active;
    
    // Display info
    private String displayName;
    private String dayOfWeek;
    private int weekNumber;
    
    // Registered students (optional, for admin view)
    private List<UserDTO> registeredStudents;
}
