package com.labregistration.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyNoteDTO {
    private Long id;
    private Long labSessionId;
    private String labSessionName;
    private Integer weekNumber;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String title;
    private String content;
    private String learningObjectives;
    private String materialsNeeded;
    private Boolean isPublished;
    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For display purposes
    private String displayName;
    private String dateRange;
    private Boolean isCurrentWeek;
    private Boolean isPastWeek;
    private Boolean isFutureWeek;
}
