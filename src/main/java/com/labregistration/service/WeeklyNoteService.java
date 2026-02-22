package com.labregistration.service;

import com.labregistration.dto.WeeklyNoteDTO;
import com.labregistration.dto.request.UpdateWeeklyNoteRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.model.LabSession;
import com.labregistration.model.User;
import com.labregistration.model.WeeklyNote;
import com.labregistration.repository.LabSessionRepository;
import com.labregistration.repository.UserRepository;
import com.labregistration.repository.WeeklyNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyNoteService {

    private final WeeklyNoteRepository weeklyNoteRepository;
    private final LabSessionRepository labSessionRepository;
    private final UserRepository userRepository;

    /**
     * Get all weekly notes for a lab session (for admin/lab manager)
     */
    public List<WeeklyNoteDTO> getAllWeeklyNotes(Long labSessionId) {
        LabSession session = labSessionRepository.findById(labSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", labSessionId));

        List<WeeklyNote> existingNotes = weeklyNoteRepository.findByLabSessionIdOrderByWeekNumber(labSessionId);
        Map<Integer, WeeklyNote> noteMap = existingNotes.stream()
                .collect(Collectors.toMap(WeeklyNote::getWeekNumber, n -> n));

        // Generate all weeks and merge with existing notes
        List<WeeklyNoteDTO> result = new ArrayList<>();
        int totalWeeks = session.getDurationWeeks();
        LocalDate currentWeekStart = getFirstSessionDay(session);

        for (int week = 1; week <= totalWeeks; week++) {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            
            WeeklyNote note = noteMap.get(week);
            WeeklyNoteDTO dto = toDTO(note, session, week, currentWeekStart, weekEnd);
            result.add(dto);

            currentWeekStart = currentWeekStart.plusWeeks(1);
        }

        return result;
    }

    /**
     * Get published weekly notes for a lab session (for students)
     */
    public List<WeeklyNoteDTO> getPublishedWeeklyNotes(Long labSessionId) {
        LabSession session = labSessionRepository.findById(labSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", labSessionId));

        List<WeeklyNote> existingNotes = weeklyNoteRepository.findByLabSessionIdOrderByWeekNumber(labSessionId);
        Map<Integer, WeeklyNote> noteMap = existingNotes.stream()
                .collect(Collectors.toMap(WeeklyNote::getWeekNumber, n -> n));

        List<WeeklyNoteDTO> result = new ArrayList<>();
        int totalWeeks = session.getDurationWeeks();
        LocalDate currentWeekStart = getFirstSessionDay(session);

        for (int week = 1; week <= totalWeeks; week++) {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            
            WeeklyNote note = noteMap.get(week);
            WeeklyNoteDTO dto = toDTO(note, session, week, currentWeekStart, weekEnd);
            
            // For students, only show published notes or basic week info
            if (note == null || !Boolean.TRUE.equals(note.getIsPublished())) {
                dto.setContent(null);
                dto.setLearningObjectives(null);
                dto.setMaterialsNeeded(null);
            }
            
            result.add(dto);
            currentWeekStart = currentWeekStart.plusWeeks(1);
        }

        return result;
    }

    /**
     * Get a single weekly note
     */
    public WeeklyNoteDTO getWeeklyNote(Long labSessionId, Integer weekNumber) {
        LabSession session = labSessionRepository.findById(labSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", labSessionId));

        if (weekNumber < 1 || weekNumber > session.getDurationWeeks()) {
            throw new BadRequestException("Invalid week number");
        }

        LocalDate weekStart = getFirstSessionDay(session).plusWeeks(weekNumber - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        WeeklyNote note = weeklyNoteRepository.findByLabSessionIdAndWeekNumber(labSessionId, weekNumber)
                .orElse(null);

        return toDTO(note, session, weekNumber, weekStart, weekEnd);
    }

    /**
     * Update or create a weekly note (for admin/lab manager)
     */
    @Transactional
    public WeeklyNoteDTO updateWeeklyNote(UpdateWeeklyNoteRequest request, String userEmail) {
        LabSession session = labSessionRepository.findById(request.getLabSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", request.getLabSessionId()));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (request.getWeekNumber() < 1 || request.getWeekNumber() > session.getDurationWeeks()) {
            throw new BadRequestException("Invalid week number. Session has " + session.getDurationWeeks() + " weeks.");
        }

        LocalDate weekStart = getFirstSessionDay(session).plusWeeks(request.getWeekNumber() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        WeeklyNote note = weeklyNoteRepository.findByLabSessionIdAndWeekNumber(
                request.getLabSessionId(), request.getWeekNumber())
                .orElse(WeeklyNote.builder()
                        .labSession(session)
                        .weekNumber(request.getWeekNumber())
                        .weekStartDate(weekStart)
                        .weekEndDate(weekEnd)
                        .createdBy(user)
                        .build());

        // Update fields
        if (request.getTitle() != null) note.setTitle(request.getTitle());
        if (request.getContent() != null) note.setContent(request.getContent());
        if (request.getLearningObjectives() != null) note.setLearningObjectives(request.getLearningObjectives());
        if (request.getMaterialsNeeded() != null) note.setMaterialsNeeded(request.getMaterialsNeeded());
        if (request.getPublish() != null) note.setIsPublished(request.getPublish());

        note.setUpdatedBy(user);
        note.setWeekStartDate(weekStart);
        note.setWeekEndDate(weekEnd);

        note = weeklyNoteRepository.save(note);
        log.info("Updated weekly note for session {} week {} by {}", session.getName(), request.getWeekNumber(), userEmail);

        return toDTO(note, session, request.getWeekNumber(), weekStart, weekEnd);
    }

    /**
     * Publish a weekly note
     */
    @Transactional
    public WeeklyNoteDTO publishWeeklyNote(Long labSessionId, Integer weekNumber, String userEmail) {
        WeeklyNote note = weeklyNoteRepository.findByLabSessionIdAndWeekNumber(labSessionId, weekNumber)
                .orElseThrow(() -> new BadRequestException("Please add content before publishing"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        note.setIsPublished(true);
        note.setUpdatedBy(user);
        note = weeklyNoteRepository.save(note);

        log.info("Published weekly note for session {} week {}", labSessionId, weekNumber);

        LocalDate weekStart = getFirstSessionDay(note.getLabSession()).plusWeeks(weekNumber - 1);
        return toDTO(note, note.getLabSession(), weekNumber, weekStart, weekStart.plusDays(6));
    }

    /**
     * Unpublish a weekly note
     */
    @Transactional
    public WeeklyNoteDTO unpublishWeeklyNote(Long labSessionId, Integer weekNumber, String userEmail) {
        WeeklyNote note = weeklyNoteRepository.findByLabSessionIdAndWeekNumber(labSessionId, weekNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Weekly note not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        note.setIsPublished(false);
        note.setUpdatedBy(user);
        note = weeklyNoteRepository.save(note);

        log.info("Unpublished weekly note for session {} week {}", labSessionId, weekNumber);

        LocalDate weekStart = getFirstSessionDay(note.getLabSession()).plusWeeks(weekNumber - 1);
        return toDTO(note, note.getLabSession(), weekNumber, weekStart, weekStart.plusDays(6));
    }

    /**
     * Get the first session day based on session start date and session days
     */
    private LocalDate getFirstSessionDay(LabSession session) {
        LocalDate date = session.getStartDate();
        var sessionDays = session.getSessionDaysSet();
        
        if (sessionDays.isEmpty()) {
            return date;
        }

        // Find the first occurrence of a session day
        for (int i = 0; i < 7; i++) {
            LocalDate checkDate = date.plusDays(i);
            String dayName = checkDate.getDayOfWeek().toString();
            if (sessionDays.contains(dayName)) {
                return checkDate;
            }
        }
        
        return date;
    }

    /**
     * Convert WeeklyNote to DTO
     */
    private WeeklyNoteDTO toDTO(WeeklyNote note, LabSession session, Integer weekNumber, 
                                LocalDate weekStart, LocalDate weekEnd) {
        LocalDate today = LocalDate.now();
        boolean isCurrentWeek = !today.isBefore(weekStart) && !today.isAfter(weekEnd);
        boolean isPastWeek = today.isAfter(weekEnd);
        boolean isFutureWeek = today.isBefore(weekStart);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        String dateRange = weekStart.format(formatter) + " - " + weekEnd.format(formatter);

        if (note == null) {
            return WeeklyNoteDTO.builder()
                    .labSessionId(session.getId())
                    .labSessionName(session.getName())
                    .weekNumber(weekNumber)
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .isPublished(false)
                    .displayName("Week " + weekNumber)
                    .dateRange(dateRange)
                    .isCurrentWeek(isCurrentWeek)
                    .isPastWeek(isPastWeek)
                    .isFutureWeek(isFutureWeek)
                    .build();
        }

        return WeeklyNoteDTO.builder()
                .id(note.getId())
                .labSessionId(session.getId())
                .labSessionName(session.getName())
                .weekNumber(note.getWeekNumber())
                .weekStartDate(note.getWeekStartDate())
                .weekEndDate(note.getWeekEndDate())
                .title(note.getTitle())
                .content(note.getContent())
                .learningObjectives(note.getLearningObjectives())
                .materialsNeeded(note.getMaterialsNeeded())
                .isPublished(note.getIsPublished())
                .createdById(note.getCreatedBy() != null ? note.getCreatedBy().getId() : null)
                .createdByName(note.getCreatedBy() != null ? note.getCreatedBy().getFullName() : null)
                .updatedById(note.getUpdatedBy() != null ? note.getUpdatedBy().getId() : null)
                .updatedByName(note.getUpdatedBy() != null ? note.getUpdatedBy().getFullName() : null)
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .displayName("Week " + note.getWeekNumber() + (note.getTitle() != null ? ": " + note.getTitle() : ""))
                .dateRange(dateRange)
                .isCurrentWeek(isCurrentWeek)
                .isPastWeek(isPastWeek)
                .isFutureWeek(isFutureWeek)
                .build();
    }
}
