//package com.labregistration.service;
//
//import com.labregistration.dto.LabSessionDTO;
//import com.labregistration.dto.request.CreateLabSessionRequest;
//import com.labregistration.exception.BadRequestException;
//import com.labregistration.exception.ResourceNotFoundException;
//import com.labregistration.mapper.LabSessionMapper;
//import com.labregistration.model.*;
//import com.labregistration.repository.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class LabSessionService {
//
//    private final LabSessionRepository labSessionRepository;
//    private final TimeSlotRepository timeSlotRepository;
//    private final CourseRepository courseRepository;
//    private final ProgramRepository programRepository;
//    private final UserRepository userRepository;
//    private final LabSessionMapper labSessionMapper;
//    private final LabGroupRepository labGroupRepository;
//
//    private final RegistrationRepository registrationRepository;
//
//    @Transactional
//    public LabSessionDTO createLabSession(CreateLabSessionRequest request, String creatorEmail) {
//        Course course = courseRepository.findById(request.getCourseId())
//                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));
//
//        User creator = userRepository.findByEmail(creatorEmail)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "email", creatorEmail));
//
//        if (request.getEndDate().isBefore(request.getStartDate())) {
//            throw new BadRequestException("End date must be after start date");
//        }
//
//        // Build allowed programs set
//        Set<Program> allowedPrograms = new HashSet<>();
//        if (request.getAllowedProgramIds() != null && !request.getAllowedProgramIds().isEmpty()) {
//            for (Long programId : request.getAllowedProgramIds()) {
//                Program program = programRepository.findById(programId)
//                        .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));
//                allowedPrograms.add(program);
//            }
//        }
//
//        LabSession session = LabSession.builder()
//                .name(request.getName())
//                .description(request.getDescription())
//                .labRoom(request.getLabRoom())
//                .startDate(request.getStartDate())
//                .endDate(request.getEndDate())
//                .startTime(request.getStartTime())
//                .endTime(request.getEndTime())
//                .maxGroupSize(request.getMaxGroupSize())
//                .maxGroups(request.getMaxGroups())
//                .maxStudentsPerSlot(request.getMaxStudentsPerSlot())
//                .slotsPerDay(request.getSlotsPerDay())
//                .status(SessionStatus.DRAFT)
//                .course(course)
//                .createdBy(creator)
//                .allowedPrograms(allowedPrograms)
//                .openToAllPrograms(request.getOpenToAllPrograms() != null ? request.getOpenToAllPrograms() : false)
//                .registrationDeadline(request.getRegistrationDeadline())
//                .instructions(request.getInstructions())
//                .build();
//
//        // Set session days
//        if (request.getSessionDays() != null) {
//            session.setSessionDaysFromSet(request.getSessionDays());
//        }
//
//        session = labSessionRepository.save(session);
//
//        // Create time slots for each session day across all weeks
//        createTimeSlots(session, request);
//
//        // Auto-create groups if requested
////        if (request.isAutoCreateGroups()) {
////            createGroupsForSession(session);
////        }
//
//        log.info("Lab session created: {} (Duration: {} weeks)", session.getName(), session.getDurationWeeks());
//        int currentRegistrations = registrationRepository.countByLabSessionIdAndActiveTrue(session.getId());
//
//        List<TimeSlot> slots = timeSlotRepository.findByLabSession(session);
//
//        return labSessionMapper.toDTOWithSlots(session, currentRegistrations, slots);    }
//
//
//
////
////    private void createGroupsForSession(LabSession labSession) {
////        for (int i = 1; i <= labSession.getMaxGroups(); i++) {
////            LabGroup group = LabGroup.builder()
////                    .groupName("Group " + i)
////                    .groupNumber(i)
////                    .maxSize(labSession.getMaxGroupSize())
////                    .labSession(labSession)
////                    .members(new ArrayList<>())
////                    .sessionDate(labSession.getSessionDate())
////                    .startTime(labSession.getStartTime())
////                    .endTime(labSession.getEndTime())
////                    .build();
////
////            labGroupRepository.save(group);
////            labSession.getGroups().add(group);
////        }
////        log.info("Created {} groups for session: {}", labSession.getMaxGroups(), labSession.getName());
////    }
//
//    private void createTimeSlots(LabSession session, CreateLabSessionRequest request) {
//        Set<DayOfWeek> sessionDays = new HashSet<>();
//        for (String day : request.getSessionDays()) {
//            try {
//                sessionDays.add(DayOfWeek.valueOf(day.toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid day of week: {}", day);
//            }
//        }
//
//        LocalDate currentDate = request.getStartDate();
//        int slotCounter = 1;
//
//        // Calculate time slot duration
//        long totalMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
//        long slotDuration = totalMinutes / request.getSlotsPerDay();
//
//        while (!currentDate.isAfter(request.getEndDate())) {
//            if (sessionDays.contains(currentDate.getDayOfWeek())) {
//                // Create slots for this day
//                LocalTime slotStart = request.getStartTime();
//
//                for (int i = 1; i <= request.getSlotsPerDay(); i++) {
//                    LocalTime slotEnd = slotStart.plusMinutes(slotDuration);
//                    TimeSlot slot = TimeSlot.builder()
//                            .labSession(session)
//                            .sessionDate(currentDate)
//                            .startTime(slotStart)
//                            .endTime(slotEnd)
//                            .slotNumber(i)
//                            .maxStudents(request.getMaxStudentsPerSlot())
//                            .currentCount(0)
//                            .active(true)
//                            .build();
//
//                    timeSlotRepository.save(slot);
//                    slotStart = slotEnd;
//                }
//            }
//            currentDate = currentDate.plusDays(1);
//        }
//    }
//
//
//
//
//
//
//
//
////    public LabSessionDTO getLabSessionById(Long id) {
////        LabSession session = labSessionRepository.findById(id)
////                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
////        return labSessionMapper.toDTOWithSlots(session);
////    }
//
//    public LabSessionDTO getLabSessionById(Long id) {
//
//        LabSession session = labSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
//        int currentRegistrations = registrationRepository.countByLabSession(session);
//        List<TimeSlot> slots = timeSlotRepository.findByLabSession(session);
//        return labSessionMapper.toDTOWithSlots(session, currentRegistrations, slots);
//    }
//
//    public LabSession getLabSessionEntityById(Long id) {
//        return labSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
//    }
//
////    public List<LabSessionDTO> getAllLabSessions() {
////        return labSessionRepository.findAll().stream()
////                .map(labSessionMapper::toDTO)
////                .collect(Collectors.toList());
////    }
//
//
//    public List<LabSessionDTO> getAllLabSessions() {
//        return labSessionRepository.findAll().stream()
//                .map(session -> {
//                    int count = registrationRepository.countByLabSession(session);
//                    return labSessionMapper.toDTO(session, count);
//                })
//                .collect(Collectors.toList());
//    }
//
//
////
////    public List<LabSessionDTO> getAvailableLabSessions() {
////        return labSessionRepository.findAvailableSessions(LocalDate.now()).stream()
////                .map(labSessionMapper::toDTO)
////                .collect(Collectors.toList());
////    }
//
//    public List<LabSessionDTO> getAvailableLabSessions() {
//        return labSessionRepository.findAvailableSessions(LocalDate.now()).stream()
//                .map(session -> {
//                    int count = registrationRepository.countByLabSession(session);
//                    return labSessionMapper.toDTO(session, count);
//                })
//                .collect(Collectors.toList());
//    }
//
//
//
//
////    public List<LabSessionDTO> getAvailableSessionsForProgram(Long programId) {
////        return labSessionRepository.findAvailableSessionsForProgram(programId, LocalDate.now()).stream()
////                .map(labSessionMapper::toDTO)
////                .collect(Collectors.toList());
////    }
//
//
//
//    public List<LabSessionDTO> getAvailableSessionsForProgram(Long programId) {
//        return labSessionRepository
//                .findAvailableSessionsForProgram(programId, LocalDate.now())
//                .stream()
//                .map(session -> {
//                    int count = registrationRepository.countByLabSession(session);
//                    return labSessionMapper.toDTO(session, count);
//                })
//                .collect(Collectors.toList());
//    }
//
////    public List<LabSessionDTO> getLabSessionsByStatus(SessionStatus status) {
////        return labSessionRepository.findByStatus(status).stream()
////                .map(labSessionMapper::toDTO)
////                .collect(Collectors.toList());
////    }
//
//
//    public List<LabSessionDTO> getLabSessionsByStatus(SessionStatus status) {
//        return labSessionRepository.findByStatus(status).stream()
//                .map(session -> {
//                    int count = registrationRepository.countByLabSession(session);
//                    return labSessionMapper.toDTO(session, count);
//                })
//                .collect(Collectors.toList());
//    }
//
//
//
////    @Transactional
////    public LabSessionDTO updateLabSession(Long id, CreateLabSessionRequest request) {
////        LabSession session = labSessionRepository.findById(id)
////                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
////
////        session.setName(request.getName());
////        session.setDescription(request.getDescription());
////        session.setLabRoom(request.getLabRoom());
////        session.setMaxStudentsPerSlot(request.getMaxStudentsPerSlot());
////        session.setSlotsPerDay(request.getSlotsPerDay());
////        session.setInstructions(request.getInstructions());
////        session.setRegistrationDeadline(request.getRegistrationDeadline());
////
////        if (request.getSessionDays() != null) {
////            session.setSessionDaysFromSet(request.getSessionDays());
////        }
////
////        if (request.getAllowedProgramIds() != null) {
////            Set<Program> programs = new HashSet<>();
////            for (Long programId : request.getAllowedProgramIds()) {
////                programs.add(programRepository.findById(programId)
////                        .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId)));
////            }
////            session.setAllowedPrograms(programs);
////        }
////
////        if (request.getOpenToAllPrograms() != null) {
////            session.setOpenToAllPrograms(request.getOpenToAllPrograms());
////        }
////
////        session = labSessionRepository.save(session);
////        return labSessionMapper.toDTOWithSlots(session);
////    }
//
//
//    @Transactional
//    public LabSessionDTO updateLabSession(Long id, CreateLabSessionRequest request) {
//        LabSession session = labSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
//        // Update fields
//        session.setName(request.getName());
//        session.setDescription(request.getDescription());
//        session.setLabRoom(request.getLabRoom());
//        session.setMaxStudentsPerSlot(request.getMaxStudentsPerSlot());
//        session.setSlotsPerDay(request.getSlotsPerDay());
//        session.setInstructions(request.getInstructions());
//        session.setRegistrationDeadline(request.getRegistrationDeadline());
//        if (request.getSessionDays() != null) {
//            session.setSessionDaysFromSet(request.getSessionDays());
//        }
//        if (request.getAllowedProgramIds() != null) {
//            Set<Program> programs = new HashSet<>();
//            for (Long programId : request.getAllowedProgramIds()) {
//                programs.add(programRepository.findById(programId)
//                        .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId)));
//            }
//            session.setAllowedPrograms(programs);
//        }
//        if (request.getOpenToAllPrograms() != null) {
//            session.setOpenToAllPrograms(request.getOpenToAllPrograms());
//        }
//        session = labSessionRepository.save(session);
//        // Compute additional info for mapper
//        int currentRegistrations = registrationRepository.countByLabSession(session);
//        List<TimeSlot> slots = timeSlotRepository.findByLabSession(session);
//        return labSessionMapper.toDTOWithSlots(session, currentRegistrations, slots);
//    }
//
//
//    @Transactional
//    public LabSessionDTO updateStatus(Long id, SessionStatus status) {
//        LabSession session = labSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
//
//        validateStatusTransition(session.getStatus(), status);
//        session.setStatus(status);
//        session = labSessionRepository.save(session);
//
//        int currentRegistrations = registrationRepository.countByLabSession(session);
//
//        log.info("Lab session {} status changed to {}", session.getName(), status);
//        return labSessionMapper.toDTO(session, currentRegistrations);
//    }
//
////    @Transactional
////    public LabSessionDTO updateStatus(Long id, SessionStatus status) {
////        LabSession session = labSessionRepository.findById(id)
////                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
////
////        validateStatusTransition(session.getStatus(), status);
////        session.setStatus(status);
////        session = labSessionRepository.save(session);
////
////        log.info("Lab session {} status changed to {}", session.getName(), status);
////        return labSessionMapper.toDTO(session);
////    }
//
//    private void validateStatusTransition(SessionStatus current, SessionStatus target) {
//        if (current == SessionStatus.CANCELLED) {
//            throw new BadRequestException("Cannot change status of cancelled session");
//        }
//        if (current == SessionStatus.COMPLETED) {
//            throw new BadRequestException("Cannot change status of completed session");
//        }
//    }
//
//    @Transactional
//    public void deleteLabSession(Long id) {
//        LabSession session = labSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
//
//        if (session.getStatus() == SessionStatus.OPEN) {
//            throw new BadRequestException("Cannot delete an open session. Close it first.");
//        }
//
//        timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(id).forEach(timeSlotRepository::delete);
//        labSessionRepository.delete(session);
//        log.info("Lab session deleted: {}", session.getName());
//    }
//
//    public long countByStatus(SessionStatus status) {
//        return labSessionRepository.countByStatus(status);
//    }
//}



package com.labregistration.service;

import com.labregistration.dto.LabSessionDTO;
import com.labregistration.dto.request.CreateLabSessionRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.mapper.LabSessionMapper;
import com.labregistration.model.*;
import com.labregistration.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabSessionService {

    private final LabSessionRepository labSessionRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final LabSessionMapper labSessionMapper;
    private final LabGroupRepository labGroupRepository;
    private final RegistrationRepository registrationRepository;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Transactional
    public LabSessionDTO createLabSession(CreateLabSessionRequest request, String creatorEmail) {
        // Validate time range
        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().equals(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Resolve course and creator
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", creatorEmail));

        // Check for scheduling conflicts on each session day
        if (request.getSessionDays() != null) {
            Set<DayOfWeek> sessionDays = parseDaysOfWeek(request.getSessionDays());
            LocalDate current = request.getStartDate();
            while (!current.isAfter(request.getEndDate())) {
                if (sessionDays.contains(current.getDayOfWeek())) {
                    checkRoomConflicts(request.getLabRoom(), current,
                            request.getStartTime(), request.getEndTime(), null);
                }
                current = current.plusDays(1);
            }
        }

        // Build allowed programs
        Set<Program> allowedPrograms = new HashSet<>();
        if (request.getAllowedProgramIds() != null && !request.getAllowedProgramIds().isEmpty()) {
            for (Long programId : request.getAllowedProgramIds()) {
                Program program = programRepository.findById(programId)
                        .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));
                allowedPrograms.add(program);
            }
        }

        // Build and save session
        LabSession session = LabSession.builder()
                .name(request.getName())
                .description(request.getDescription())
                .labRoom(request.getLabRoom())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxGroupSize(request.getMaxGroupSize())
                .maxGroups(request.getMaxGroups())
                .maxStudentsPerSlot(request.getMaxStudentsPerSlot())
                .slotsPerDay(request.getSlotsPerDay())
                .status(request.getStatus() != null ? request.getStatus() : SessionStatus.DRAFT)
                .course(course)
                .createdBy(creator)
                .allowedPrograms(allowedPrograms)
                .openToAllPrograms(request.getOpenToAllPrograms() != null ? request.getOpenToAllPrograms() : false)
                .registrationDeadline(request.getRegistrationDeadline())
                .instructions(request.getInstructions())
                .build();

        if (request.getSessionDays() != null) {
            session.setSessionDaysFromSet(request.getSessionDays());
        }

        session = labSessionRepository.save(session);

        // Create time slots across the date range
        createTimeSlots(session, request);

        log.info("Lab session created: {} by {} (Duration: {} to {})",
                session.getName(), creatorEmail, session.getStartDate(), session.getEndDate());

        int currentRegistrations = registrationRepository.countByLabSessionIdAndActiveTrue(session.getId());
        List<TimeSlot> slots = timeSlotRepository.findByLabSession(session);
        return labSessionMapper.toDTOWithSlots(session, currentRegistrations, slots);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public LabSessionDTO getLabSessionById(Long id) {
        LabSession session = labSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
        int currentRegistrations = registrationRepository.countByLabSession(session);
        List<TimeSlot> slots = timeSlotRepository.findByLabSession(session);
        return labSessionMapper.toDTOWithSlots(session, currentRegistrations, slots);
    }

    public LabSession getLabSessionEntityById(Long id) {
        return labSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));
    }

    public List<LabSessionDTO> getAllLabSessions() {
        return labSessionRepository.findAll().stream()
                .map(session -> {
                    int count = registrationRepository.countByLabSession(session);
                    return labSessionMapper.toDTO(session, count);
                })
                .collect(Collectors.toList());
    }

    public List<LabSessionDTO> getAvailableLabSessions() {
        return labSessionRepository.findAvailableSessions(LocalDate.now()).stream()
                .map(session -> {
                    int count = registrationRepository.countByLabSession(session);
                    return labSessionMapper.toDTO(session, count);
                })
                .collect(Collectors.toList());
    }

    public List<LabSessionDTO> getAvailableSessionsForProgram(Long programId) {
        return labSessionRepository.findAvailableSessionsForProgram(programId, LocalDate.now()).stream()
                .map(session -> {
                    int count = registrationRepository.countByLabSession(session);
                    return labSessionMapper.toDTO(session, count);
                })
                .collect(Collectors.toList());
    }

    public List<LabSessionDTO> getAvailableSessionsByCourse(Long courseId) {
        return labSessionRepository.findAvailableSessionsByCourse(courseId, LocalDate.now()).stream()
                .map(session -> {
                    int count = registrationRepository.countByLabSession(session);
                    return labSessionMapper.toDTO(session, count);
                })
                .collect(Collectors.toList());
    }

    public List<LabSessionDTO> getLabSessionsByStatus(SessionStatus status) {
        return labSessionRepository.findByStatus(status).stream()
                .map(session -> {
                    int count = registrationRepository.countByLabSession(session);
                    return labSessionMapper.toDTO(session, count);
                })
                .collect(Collectors.toList());
    }

    public List<LabSessionDTO> getSessionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return labSessionRepository.findSessionsBetweenDates(startDate, endDate).stream()
                .map(session -> {
                    int count = registrationRepository.countByLabSession(session);
                    return labSessionMapper.toDTO(session, count);
                })
                .collect(Collectors.toList());
    }

    public List<LabSessionDTO> getSessionsWithAvailableSlots() {
        return labSessionRepository.findSessionsWithAvailableSlots(LocalDate.now()).stream()
                .map(session -> {
                    int count = registrationRepository.countByLabSession(session);
                    return labSessionMapper.toDTO(session, count);
                })
                .collect(Collectors.toList());
    }

    public int getActiveRegistrationCount(Long sessionId) {
        return registrationRepository.countByLabSessionIdAndActiveTrue(sessionId);
    }

    public long countByStatus(SessionStatus status) {
        return labSessionRepository.countByStatus(status);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Transactional
    public LabSessionDTO updateLabSession(Long id, CreateLabSessionRequest request) {
        LabSession session = labSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));

        // Guard against reducing capacity below current active registrations
        int currentRegistrations = registrationRepository.countByLabSession(session);
        int newCapacity = request.getMaxStudentsPerSlot() * request.getSlotsPerDay();
        if (newCapacity < currentRegistrations) {
            throw new BadRequestException(String.format(
                    "Cannot reduce capacity below current active registrations (%d)", currentRegistrations));
        }

        // Update scalar fields
        session.setName(request.getName());
        session.setDescription(request.getDescription());
        session.setLabRoom(request.getLabRoom());
        session.setMaxStudentsPerSlot(request.getMaxStudentsPerSlot());
        session.setSlotsPerDay(request.getSlotsPerDay());
        session.setMaxGroupSize(request.getMaxGroupSize());
        session.setMaxGroups(request.getMaxGroups());
        session.setInstructions(request.getInstructions());
        session.setRegistrationDeadline(request.getRegistrationDeadline());

        // Update session days
        if (request.getSessionDays() != null) {
            session.setSessionDaysFromSet(request.getSessionDays());
        }

        // Update allowed programs
        if (request.getAllowedProgramIds() != null) {
            Set<Program> programs = new HashSet<>();
            for (Long programId : request.getAllowedProgramIds()) {
                programs.add(programRepository.findById(programId)
                        .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId)));
            }
            session.setAllowedPrograms(programs);
        }

        if (request.getOpenToAllPrograms() != null) {
            session.setOpenToAllPrograms(request.getOpenToAllPrograms());
        }

        session = labSessionRepository.save(session);
        log.info("Lab session updated: {}", session.getName());

        List<TimeSlot> slots = timeSlotRepository.findByLabSession(session);
        return labSessionMapper.toDTOWithSlots(session, currentRegistrations, slots);
    }

    @Transactional
    public LabSessionDTO updateStatus(Long id, SessionStatus status) {
        LabSession session = labSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));

        validateStatusTransition(session.getStatus(), status);
        session.setStatus(status);
        session = labSessionRepository.save(session);

        int currentRegistrations = registrationRepository.countByLabSession(session);
        log.info("Lab session '{}' status changed to {}", session.getName(), status);
        return labSessionMapper.toDTO(session, currentRegistrations);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteLabSession(Long id) {
        LabSession session = labSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", id));

        if (session.getStatus() == SessionStatus.OPEN) {
            throw new BadRequestException("Cannot delete an open session. Close or cancel it first.");
        }

        int activeRegistrations = registrationRepository.countByLabSessionIdAndActiveTrue(id);
        if (activeRegistrations > 0) {
            throw new BadRequestException(
                    "Cannot delete a session with active registrations. Cancel the session instead.");
        }

        timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(id).forEach(timeSlotRepository::delete);
        labSessionRepository.delete(session);
        log.info("Lab session deleted: {}", session.getName());
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------------

    /**
     * Creates time slots for every matching day-of-week between startDate and endDate.
     */
    private void createTimeSlots(LabSession session, CreateLabSessionRequest request) {
        Set<DayOfWeek> sessionDays = parseDaysOfWeek(request.getSessionDays());

        long totalMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        long slotDuration = totalMinutes / request.getSlotsPerDay();

        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            if (sessionDays.contains(currentDate.getDayOfWeek())) {
                LocalTime slotStart = request.getStartTime();
                for (int i = 1; i <= request.getSlotsPerDay(); i++) {
                    LocalTime slotEnd = slotStart.plusMinutes(slotDuration);
                    TimeSlot slot = TimeSlot.builder()
                            .labSession(session)
                            .sessionDate(currentDate)
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .groupNumber(i)
                            .maxStudents(request.getMaxStudentsPerSlot())
                            .currentCount(0)
                            .active(true)
                            .build();
                    timeSlotRepository.save(slot);
                    slotStart = slotEnd;
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    /**
     * Parses a collection of day-name strings into a Set of DayOfWeek, logging warnings for invalid values.
     */
    private Set<DayOfWeek> parseDaysOfWeek(Iterable<String> days) {
        Set<DayOfWeek> result = new HashSet<>();
        for (String day : days) {
            try {
                result.add(DayOfWeek.valueOf(day.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid day of week ignored: {}", day);
            }
        }
        return result;
    }

    /**
     * Checks if any existing session in the same room on the same date overlaps the given time range.
     * Pass excludeId to skip a specific session (useful during updates).
     */
    private void checkRoomConflicts(String labRoom, LocalDate date,
                                    LocalTime startTime, LocalTime endTime, Long excludeId) {
        List<LabSession> existingSessions = labSessionRepository.findByLabRoomAndDate(labRoom, date);
        for (LabSession existing : existingSessions) {
            if (excludeId != null && existing.getId().equals(excludeId)) continue;
            if (timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                throw new BadRequestException(String.format(
                        "Room conflict with existing session '%s' on %s (%s - %s)",
                        existing.getName(), date, existing.getStartTime(), existing.getEndTime()));
            }
        }
    }





//    private void checkRoomConflicts(String labRoom, LocalDate date,
//                                    LocalTime startTime, LocalTime endTime, Long excludeId) {
//        List<LabSession> existingSessions = labSessionRepository.findByLabRoomAndDate(labRoom, date);
//        String dayName = date.getDayOfWeek().name(); // e.g. "MONDAY"
//
//        for (LabSession existing : existingSessions) {
//            if (excludeId != null && existing.getId().equals(excludeId)) continue;
//            // Skip if this session doesn't actually run on this day of the week
//            if (!existing.getSessionDaysSet().contains(dayName)) continue;
//            if (timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
//                throw new BadRequestException(String.format(
//                        "Room conflict with existing session '%s' on %s (%s - %s)",
//                        existing.getName(), date, existing.getStartTime(), existing.getEndTime()));
//            }
//        }
//    }






    private boolean timesOverlap(LocalTime start1, LocalTime end1,
                                 LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Enforces valid status-transition rules.
     * DRAFT  → OPEN | CANCELLED
     * OPEN   → CLOSED | CANCELLED
     * CLOSED → COMPLETED | OPEN
     * COMPLETED / CANCELLED → (terminal, no transitions allowed)
     */
    private void validateStatusTransition(SessionStatus current, SessionStatus target) {
        switch (current) {
            case DRAFT:
                if (target != SessionStatus.OPEN && target != SessionStatus.CANCELLED)
                    throw new BadRequestException("Draft sessions can only be opened or cancelled.");
                break;
            case OPEN:
                if (target != SessionStatus.CLOSED && target != SessionStatus.CANCELLED)
                    throw new BadRequestException("Open sessions can only be closed or cancelled.");
                break;
            case CLOSED:
                if (target != SessionStatus.COMPLETED && target != SessionStatus.OPEN)
                    throw new BadRequestException("Closed sessions can only be marked complete or reopened.");
                break;
            case CANCELLED:
            case COMPLETED:
                throw new BadRequestException("Cannot change the status of a cancelled or completed session.");
        }
    }
}