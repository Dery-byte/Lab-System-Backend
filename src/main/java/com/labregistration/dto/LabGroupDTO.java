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
public class LabGroupDTO {
    private Long id;
    private String groupName;
    private Integer groupNumber;
    private Integer maxSize;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long labSessionId;
    private int currentSize;
    private int availableSlots;
    private boolean isFull;
    private List<RegistrationDTO> members;
}
