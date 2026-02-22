package com.labregistration.dto;

import com.labregistration.model.RegistrationStatus;
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
public class RegistrationDTO {
    private Long id;
    private UserDTO student;
    // Student info
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentIdNumber;
    private String programName;
    
    // Lab Session info
    private Long labSessionId;
    private String labSessionName;
    private String courseCode;
    private String courseName;
    private String labRoom;
    private LocalDate sessionStartDate;
    private LocalDate sessionEndDate;
    private LocalTime sessionStartTime;
    private LocalTime sessionEndTime;
    private Set<String> sessionDays;
    private Integer durationWeeks;
    private String instructions;
    
    // Assigned time slot info (hidden from student display, used internally)
    private Long timeSlotId;
    private String timeSlotDisplayName;
    private Integer slotNumber;
    private RegistrationStatus status;
    private Integer waitlistPosition;
    
    // Timestamps
    private LocalDateTime registeredAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    
    // Notes
    private String studentNotes;
    private String adminNotes;
    
    // Attendance tracking
    private Integer attendedSessions;
    private Integer totalSessions;
    private Double attendancePercentage;
    
    // Grade
    private Double score;
    private String grade;
    private LocalDateTime createdAt;
    
    // Weekly notes for the session (populated when fetching for student view)
    private List<WeeklyNoteDTO> weeklyNotes;
}
