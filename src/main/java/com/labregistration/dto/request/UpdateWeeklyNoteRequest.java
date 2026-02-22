package com.labregistration.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWeeklyNoteRequest {
    @NotNull(message = "Lab session ID is required")
    private Long labSessionId;

    @NotNull(message = "Week number is required")
    private Integer weekNumber;

    private String title;

    private String content;

    private String learningObjectives;

    private String materialsNeeded;

    private Boolean publish;
}
