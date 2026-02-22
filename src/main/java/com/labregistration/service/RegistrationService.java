package com.labregistration.service;

import com.labregistration.dto.RegistrationDTO;
import com.labregistration.dto.WeeklyNoteDTO;
import com.labregistration.dto.request.CreateRegistrationRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.mapper.RegistrationMapper;
import com.labregistration.model.*;
import com.labregistration.repository.RegistrationRepository;
import com.labregistration.repository.TimeSlotRepository;
import com.labregistration.repository.WeeklyNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WeeklyNoteRepository weeklyNoteRepository;
    private final LabSessionService labSessionService;
    private final UserService userService;
    private final RegistrationMapper registrationMapper;
    private final NotificationService notificationService;

    /**
     * Create a registration for a student for the ENTIRE duration of a lab session.
     * The student will be assigned to ONE time slot that they attend every week.
     */
//    @Transactional
//    public RegistrationDTO createRegistration(CreateRegistrationRequest request, String studentEmail) {
//        User student = userService.getUserEntityByEmail(studentEmail);
//        LabSession session = labSessionService.getLabSessionEntityById(request.getLabSessionId());
//
//        // Validate session is open
//        if (!session.isOpen()) {
//            throw new BadRequestException("Session is not open for registration");
//        }
//
//        // Check registration deadline
//        if (session.getRegistrationDeadline() != null &&
//            session.getRegistrationDeadline().isBefore(java.time.LocalDateTime.now())) {
//            throw new BadRequestException("Registration deadline has passed");
//        }
//
//        // Check if already registered for this session
//        boolean alreadyRegistered = registrationRepository.existsByStudentIdAndLabSessionIdAndStatusIn(
//                student.getId(),
//                session.getId(),
//                Arrays.asList(RegistrationStatus.PENDING, RegistrationStatus.CONFIRMED, RegistrationStatus.WAITLISTED)
//        );
//        if (alreadyRegistered) {
//            throw new BadRequestException("You are already registered for this session");
//        }
//
//        // Check program access
//        if (!session.isOpenToProgram(student.getProgram())) {
//            throw new BadRequestException("This lab session is not available for your program (" +
//                    student.getProgramName() + ")");
//        }
//
//        // Calculate total sessions (number of weeks the session runs)
//        int totalSessions = session.getDurationWeeks() * session.getSessionDaysSet().size();
//
//        Registration registration = Registration.builder()
//                .student(student)
//                .labSession(session)
//                .studentNotes(request.getNotes())
//                .totalSessions(totalSessions)
//                .build();
//
//        // Assign to time slot
//        TimeSlot assignedSlot = null;
//
//        if (request.getTimeSlotId() != null) {
//            // Student requested a specific slot number
//            assignedSlot = timeSlotRepository.findById(request.getTimeSlotId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Time Slot", "id", request.getTimeSlotId()));
//
//            if (assignedSlot.isFull()) {
//                // Slot is full, waitlist the student
//                registration.setStatus(RegistrationStatus.WAITLISTED);
//                int waitlistCount = registrationRepository.findWaitlistedBySessionId(session.getId()).size();
//                registration.setWaitlistPosition(waitlistCount + 1);
//                log.info("Student {} waitlisted for session {} (requested slot {} full)",
//                        student.getEmail(), session.getName(), assignedSlot.getGroupNumber());
//            } else {
//                // Slot available
//                registration.setTimeSlot(assignedSlot);
//                registration.confirm();
//                timeSlotRepository.incrementCount(assignedSlot.getId());
//                log.info("Student {} confirmed for session {} slot {}",
//                        student.getEmail(), session.getName(), assignedSlot.getGroupNumber());
//            }
//        } else {
//            // Auto-assign to first available slot
//            assignedSlot = timeSlotRepository.findFirstAvailableSlot(session.getId()).orElse(null);
//
//            if (assignedSlot != null) {
//                registration.setTimeSlot(assignedSlot);
//                registration.confirm();
//                timeSlotRepository.incrementCount(assignedSlot.getId());
//                log.info("Student {} auto-assigned to session {} slot {}",
//                        student.getEmail(), session.getName(), assignedSlot.getGroupNumber());
//            } else {
//                // All slots full, waitlist
//                registration.setStatus(RegistrationStatus.WAITLISTED);
//                int waitlistCount = registrationRepository.findWaitlistedBySessionId(session.getId()).size();
//                registration.setWaitlistPosition(waitlistCount + 1);
//                log.info("Student {} waitlisted for session {} (all slots full)",
//                        student.getEmail(), session.getName());
//            }
//        }
//
//        registration = registrationRepository.save(registration);
//
//        // Send notification - don't reveal slot number, just confirmation
//        String message = registration.getStatus() == RegistrationStatus.CONFIRMED
//                ? String.format("You have been registered for %s. Check your registrations for weekly session details.",
//                        session.getName())
//                : String.format("You have been added to the waitlist for %s. Position: %d",
//                        session.getName(), registration.getWaitlistPosition());
//
//        notificationService.createNotification(student, "Lab Registration", message, "INFO");
//
//        return registrationMapper.toDTO(registration);
//    }


    @Transactional
    public RegistrationDTO createRegistration(CreateRegistrationRequest request, String studentEmail) {
        User student = userService.getUserEntityByEmail(studentEmail);
        LabSession session = labSessionService.getLabSessionEntityById(request.getLabSessionId());

        // Validate session is open
        if (!session.isOpen()) {
            throw new BadRequestException("Session is not open for registration");
        }

        // Check registration deadline
        if (session.getRegistrationDeadline() != null &&
                session.getRegistrationDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequestException("Registration deadline has passed");
        }

        // ── Reactivate a previously cancelled registration instead of inserting a new row ──
        // This avoids the unique constraint violation on (student_id, lab_session_id)
        Optional<Registration> cancelledReg = registrationRepository
                .findByStudentIdAndLabSessionIdAndStatus(
                        student.getId(), session.getId(), RegistrationStatus.CANCELLED);

        if (cancelledReg.isPresent()) {
            Registration reg = cancelledReg.get();
            reg.setActive(true);
            reg.setCancelledAt(null);
            reg.setRegisteredAt(java.time.LocalDateTime.now());
            reg.setStudentNotes(request.getNotes());
            reg.setWaitlistPosition(null);
            reg.setAdminNotes(null);

            // Re-run slot assignment (same logic as below)
            reg = assignSlot(reg, session, request);
            reg = registrationRepository.save(reg);

            int totalSessions = session.getDurationWeeks() * session.getSessionDaysSet().size();
            reg.setTotalSessions(totalSessions);

            String message = reg.getStatus() == RegistrationStatus.CONFIRMED
                    ? String.format("You have been re-registered for %s.", session.getName())
                    : String.format("You have been re-added to the waitlist for %s. Position: %d",
                    session.getName(), reg.getWaitlistPosition());
            notificationService.createNotification(student, "Lab Registration", message, "INFO");

            return registrationMapper.toDTO(reg);
        }

        // Check if already actively registered
        boolean alreadyRegistered = registrationRepository.existsByStudentIdAndLabSessionIdAndStatusIn(
                student.getId(),
                session.getId(),
                Arrays.asList(RegistrationStatus.PENDING, RegistrationStatus.CONFIRMED, RegistrationStatus.WAITLISTED)
        );
        if (alreadyRegistered) {
            throw new BadRequestException("You are already registered for this session");
        }

        // Check program access
        if (!session.isOpenToProgram(student.getProgram())) {
            throw new BadRequestException("This lab session is not available for your program (" +
                    student.getProgramName() + ")");
        }

        int totalSessions = session.getDurationWeeks() * session.getSessionDaysSet().size();

        Registration registration = Registration.builder()
                .student(student)
                .labSession(session)
                .studentNotes(request.getNotes())
                .totalSessions(totalSessions)
                .build();

        registration = assignSlot(registration, session, request);
        registration = registrationRepository.save(registration);

        String message = registration.getStatus() == RegistrationStatus.CONFIRMED
                ? String.format("You have been registered for %s. Check your registrations for weekly session details.",
                session.getName())
                : String.format("You have been added to the waitlist for %s. Position: %d",
                session.getName(), registration.getWaitlistPosition());

        notificationService.createNotification(student, "Lab Registration", message, "INFO");
        return registrationMapper.toDTO(registration);
    }




    public RegistrationDTO getRegistrationById(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));
        return registrationMapper.toDTO(registration);
    }

    /**
     * Get student registrations with weekly notes for student view
     */
    public List<RegistrationDTO> getStudentRegistrations(String email) {
        User student = userService.getUserEntityByEmail(email);
        List<Registration> registrations = registrationRepository.findByStudentId(student.getId());
        
        return registrations.stream()
                .map(reg -> {
                    RegistrationDTO dto = registrationMapper.toDTO(reg);
                    // Add weekly notes for student view (only published)
                    List<WeeklyNoteDTO> weeklyNotes = getWeeklyNotesForStudent(reg.getLabSession());
                    dto.setWeeklyNotes(weeklyNotes);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get active student registrations with weekly notes
     */
    public List<RegistrationDTO> getActiveStudentRegistrations(String email) {
        User student = userService.getUserEntityByEmail(email);
        List<Registration> registrations = registrationRepository.findActiveByStudentId(student.getId());
        
        return registrations.stream()
                .map(reg -> {
                    RegistrationDTO dto = registrationMapper.toDTO(reg);
                    List<WeeklyNoteDTO> weeklyNotes = getWeeklyNotesForStudent(reg.getLabSession());
                    dto.setWeeklyNotes(weeklyNotes);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get weekly notes for a student (only published ones with week info)
     */
    private List<WeeklyNoteDTO> getWeeklyNotesForStudent(LabSession session) {
        List<WeeklyNote> existingNotes = weeklyNoteRepository.findByLabSessionIdOrderByWeekNumber(session.getId());
        Map<Integer, WeeklyNote> noteMap = existingNotes.stream()
                .collect(Collectors.toMap(WeeklyNote::getWeekNumber, n -> n));

        List<WeeklyNoteDTO> result = new ArrayList<>();
        int totalWeeks = session.getDurationWeeks();
        LocalDate currentWeekStart = getFirstSessionDay(session);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        LocalDate today = LocalDate.now();

        for (int week = 1; week <= totalWeeks; week++) {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            boolean isCurrentWeek = !today.isBefore(currentWeekStart) && !today.isAfter(weekEnd);
            boolean isPastWeek = today.isAfter(weekEnd);
            boolean isFutureWeek = today.isBefore(currentWeekStart);
            String dateRange = currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter);

            WeeklyNote note = noteMap.get(week);
            
            WeeklyNoteDTO.WeeklyNoteDTOBuilder builder = WeeklyNoteDTO.builder()
                    .labSessionId(session.getId())
                    .labSessionName(session.getName())
                    .weekNumber(week)
                    .weekStartDate(currentWeekStart)
                    .weekEndDate(weekEnd)
                    .displayName("Week " + week)
                    .dateRange(dateRange)
                    .isCurrentWeek(isCurrentWeek)
                    .isPastWeek(isPastWeek)
                    .isFutureWeek(isFutureWeek);

            // Only include content if note exists and is published
            if (note != null && Boolean.TRUE.equals(note.getIsPublished())) {
                builder.id(note.getId())
                        .title(note.getTitle())
                        .content(note.getContent())
                        .learningObjectives(note.getLearningObjectives())
                        .materialsNeeded(note.getMaterialsNeeded())
                        .isPublished(true);
                if (note.getTitle() != null) {
                    builder.displayName("Week " + week + ": " + note.getTitle());
                }
            } else {
                builder.isPublished(false);
            }

            result.add(builder.build());
            currentWeekStart = currentWeekStart.plusWeeks(1);
        }

        return result;
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

    public List<RegistrationDTO> getRegistrationsBySessionId(Long sessionId) {
        return registrationRepository.findByLabSessionId(sessionId).stream()
                .map(registrationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RegistrationDTO> getRegistrationsBySlotNumber(Long sessionId, Integer slotNumber) {
        List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdAndGroupNumber(sessionId, slotNumber);
        return slots.stream()
                .flatMap(slot -> registrationRepository.findConfirmedBySlotId(slot.getId()).stream())
                .map(registrationMapper::toDTO)
                .collect(Collectors.toList());
    }










    @Transactional
    public RegistrationDTO cancelRegistration(Long id, String email) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));

        if (!registration.getStudent().getEmail().equals(email)) {
            throw new BadRequestException("You can only cancel your own registrations");
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Registration is already cancelled");
        }
        if (registration.getStatus() == RegistrationStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed registration");
        }
        // Decrement slot count if was confirmed
        if (registration.getTimeSlot() != null && registration.getStatus() == RegistrationStatus.CONFIRMED) {
            timeSlotRepository.decrementCount(registration.getTimeSlot().getId());
        }
        registration.cancel();
        registration = registrationRepository.save(registration);
        // Promote waitlisted student
        promoteFromWaitlist(registration.getLabSession().getId());
        // Send notification
        notificationService.createNotification(
                registration.getStudent(),
                "Registration Cancelled",
                "Your registration for " + registration.getLabSession().getName() + " has been cancelled.",
                "INFO"
        );

        return registrationMapper.toDTO(registration);
    }



    private Registration assignSlot(Registration registration, LabSession session,
                                    CreateRegistrationRequest request) {
        TimeSlot assignedSlot;

        if (request.getTimeSlotId() != null) {
            assignedSlot = timeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Time Slot", "id", request.getTimeSlotId()));

            if (assignedSlot.isFull()) {
                registration.setStatus(RegistrationStatus.WAITLISTED);
                registration.setTimeSlot(null);
                // ← replace .size() with count query
                int waitlistCount = registrationRepository.countByLabSessionIdAndStatus(
                        session.getId(), RegistrationStatus.WAITLISTED);
                registration.setWaitlistPosition(waitlistCount + 1);
            } else {
                registration.setTimeSlot(assignedSlot);
                registration.confirm();
                timeSlotRepository.incrementCount(assignedSlot.getId());
            }
        } else {
            assignedSlot = timeSlotRepository.findFirstAvailableSlot(session.getId()).orElse(null);

            if (assignedSlot != null) {
                registration.setTimeSlot(assignedSlot);
                registration.confirm();
                timeSlotRepository.incrementCount(assignedSlot.getId());
            } else {
                registration.setStatus(RegistrationStatus.WAITLISTED);
                registration.setTimeSlot(null);
                // ← replace .size() with count query here too
                int waitlistCount = registrationRepository.countByLabSessionIdAndStatus(
                        session.getId(), RegistrationStatus.WAITLISTED);
                registration.setWaitlistPosition(waitlistCount + 1);
            }
        }

        return registration;
    }







    @Transactional
    public RegistrationDTO changeSlot(Long registrationId, Integer newSlotNumber, String adminEmail) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));
        if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new BadRequestException("Can only change slot for confirmed registrations");
        }

        // Find an available slot with the new slot number
        List<TimeSlot> newSlots = timeSlotRepository.findByLabSessionIdAndGroupNumber(
                registration.getLabSession().getId(), newSlotNumber);
        TimeSlot availableSlot = newSlots.stream()
                .filter(slot -> !slot.isFull())
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No available slot with number " + newSlotNumber));
        // Decrement old slot count
        if (registration.getTimeSlot() != null) {
            timeSlotRepository.decrementCount(registration.getTimeSlot().getId());
        }
        // Assign new slot
        registration.setTimeSlot(availableSlot);
        timeSlotRepository.incrementCount(availableSlot.getId());
        registration = registrationRepository.save(registration);
        return registrationMapper.toDTO(registration);
    }







    private void promoteFromWaitlist(Long sessionId) {
        List<Registration> waitlisted = registrationRepository.findWaitlistedBySessionId(sessionId);
        if (waitlisted.isEmpty()) return;

        TimeSlot availableSlot = timeSlotRepository.findFirstAvailableSlot(sessionId).orElse(null);
        if (availableSlot == null) return;

        Registration toPromote = waitlisted.get(0);
        toPromote.setTimeSlot(availableSlot);
        toPromote.confirm();
        timeSlotRepository.incrementCount(availableSlot.getId());
        registrationRepository.save(toPromote);

        // Update remaining waitlist positions
        for (int i = 1; i < waitlisted.size(); i++) {
            Registration r = waitlisted.get(i);
            r.setWaitlistPosition(i);
            registrationRepository.save(r);
        }

        // Notify promoted student
        notificationService.createNotification(
                toPromote.getStudent(),
                "Promoted from Waitlist",
                "You have been promoted from the waitlist for " + toPromote.getLabSession().getName() + 
                        ". Check your registrations for session details.",
                "SUCCESS"
        );

        log.info("Promoted {} from waitlist for session {}", 
                toPromote.getStudent().getEmail(), toPromote.getLabSession().getName());
    }

    @Transactional
    public RegistrationDTO updateStatus(Long id, RegistrationStatus status) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));

        if (status == RegistrationStatus.CONFIRMED && registration.getTimeSlot() == null) {
            throw new BadRequestException("Cannot confirm registration without assigned time slot");
        }

        registration.setStatus(status);
        if (status == RegistrationStatus.CONFIRMED) {
            registration.confirm();
        } else if (status == RegistrationStatus.CANCELLED) {
            registration.cancel();
            if (registration.getTimeSlot() != null) {
                timeSlotRepository.decrementCount(registration.getTimeSlot().getId());
            }
        } else if (status == RegistrationStatus.COMPLETED) {
            registration.complete();
        }

        registration = registrationRepository.save(registration);
        return registrationMapper.toDTO(registration);
    }

    public long countByStatus(RegistrationStatus status) {
        return registrationRepository.countByStatus(status);
    }











}
