package com.labregistration.mapper;

import com.labregistration.dto.LabSessionDTO;
import com.labregistration.dto.TimeSlotDTO;
import com.labregistration.model.LabSession;
import com.labregistration.model.TimeSlot;
import com.labregistration.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LabSessionMapper {

    private final TimeSlotRepository timeSlotRepository;
    private final ProgramMapper programMapper;


    public LabSessionDTO toDTO(LabSession session, int currentRegistrations) {
        if (session == null) return null;
        return LabSessionDTO.builder()
                .id(session.getId())
                .name(session.getName())
                .description(session.getDescription())
                .labRoom(session.getLabRoom())
                .startDate(LocalDate.parse(session.getStartDate().toString()))
                .endDate(LocalDate.parse(session.getEndDate().toString()))
                .startTime(LocalTime.parse(session.getStartTime().toString()))
                .endTime(LocalTime.parse(session.getEndTime().toString()))
                .sessionDays(Collections.singleton(session.getSessionDays()))
                .durationWeeks(session.getDurationWeeks())
                .maxStudentsPerSlot(session.getMaxStudentsPerSlot())
                .slotsPerDay(session.getSlotsPerDay())
                .totalCapacity(session.getTotalCapacity())
                .currentRegistrations(currentRegistrations)
                .availableSlots(Math.max(0, session.getTotalCapacity() - currentRegistrations))
                .status(session.getStatus())
                .courseId(session.getCourse() != null ? session.getCourse().getId() : null)
                .courseCode(session.getCourse() != null ? session.getCourse().getCourseCode() : null)
                .courseName(session.getCourse() != null ? session.getCourse().getCourseName() : null)
                .departmentName(session.getCourse() != null ? session.getCourse().getDepartmentName() : null)
                .createdById(session.getCreatedBy() != null ? session.getCreatedBy().getId() : null)
                .createdByName(session.getCreatedBy() != null ? session.getCreatedBy().getFullName() : null)
                .allowedPrograms(session.getAllowedPrograms().stream()
                        .map(programMapper::toDTO)
                        .collect(Collectors.toList()))
                .openToAllPrograms(session.getOpenToAllPrograms())
                .registrationDeadline(session.getRegistrationDeadline())
                .instructions(session.getInstructions())
                .timeSlots(timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId())
                        .stream().map(this::toTimeSlotDTO).toList())
                .createdAt(session.getCreatedAt())
                .build();
    }
//    public LabSessionDTO toDTO(LabSession session,int currentRegistrations) {
//        if (session == null) return null;
//        return LabSessionDTO.builder()
//                .maxStudentsPerSlot(session.getMaxStudentsPerSlot())
//                .slotsPerDay(session.getSlotsPerDay())
//                .totalCapacity(session.getTotalCapacity())
//                .currentRegistrations(currentRegistrations)
//                .availableSlots(session.getTotalCapacity() - currentRegistrations)
//                .status(session.getStatus())
//                .courseId(session.getCourse() != null ? session.getCourse().getId() : null)
//                .courseCode(session.getCourse() != null ? session.getCourse().getCourseCode() : null)
//                .courseName(session.getCourse() != null ? session.getCourse().getCourseName() : null)
//                .departmentName(session.getCourse() != null ? session.getCourse().getDepartmentName() : null)
//                .createdById(session.getCreatedBy() != null ? session.getCreatedBy().getId() : null)
//                .createdByName(session.getCreatedBy() != null ? session.getCreatedBy().getFullName() : null)
//                .allowedPrograms(session.getAllowedPrograms().stream()
//                        .map(programMapper::toDTO)
//                        .collect(Collectors.toList()))
//                .openToAllPrograms(session.getOpenToAllPrograms())
//                .registrationDeadline(session.getRegistrationDeadline())
//                .instructions(session.getInstructions())
//                .createdAt(session.getCreatedAt())
//                .build();
//    }

//    public LabSessionDTO toDTOWithSlots(LabSession session) {
//        LabSessionDTO dto = toDTO(session);
//        List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId());
//        dto.setTimeSlots(slots.stream().map(this::toTimeSlotDTO).collect(Collectors.toList()));
//        return dto;
//    }


    public LabSessionDTO toDTOWithSlots(
            LabSession session,
            int currentRegistrations,
            List<TimeSlot> slots
    ) {
        if (session == null) return null;
        LabSessionDTO dto = toDTO(session, currentRegistrations);
        dto.setTimeSlots(
                slots.stream()
                        .map(this::toTimeSlotDTO)
                        .toList()
        );
        return dto;
    }


    public TimeSlotDTO toTimeSlotDTO(TimeSlot slot) {
        if (slot == null) return null;

        return TimeSlotDTO.builder()
                .id(slot.getId())
                .labSessionId(slot.getLabSession().getId())
                .sessionDate(slot.getSessionDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .slotNumber(slot.getGroupNumber())
                .maxStudents(slot.getMaxStudents())
                .currentCount(slot.getCurrentCount())
                .availableSlots(Math.max(0, slot.getMaxStudents() - slot.getCurrentCount()))
                .isFull(slot.getCurrentCount() >= slot.getMaxStudents())
                .active(slot.getActive())
                .displayName(slot.getDisplayName())
                .dayOfWeek(slot.getSessionDate().getDayOfWeek().toString())
                .weekNumber(slot.getWeekNumber())
//                .registeredStudents(slot.getRegisteredStudents())
                .build();
    }

//    public TimeSlotDTO toTimeSlotDTO(TimeSlot slot) {
//        if (slot == null) return null;
//
//        return TimeSlotDTO.builder()
//                .id(slot.getId())
//                .labSessionId(slot.getLabSession().getId())
//                .sessionDate(slot.getSessionDate())
//                .startTime(slot.getStartTime())
//                .endTime(slot.getEndTime())
//                .slotNumber(slot.getSlotNumber())
//                .maxStudents(slot.getMaxStudents())
//                .currentCount(slot.getCurrentCount())
//                .availableSlots(slot.getAvailableSlots())
//                .isFull(slot.isFull())
//                .active(slot.getActive())
//                .displayName(slot.getDisplayName())
//                .dayOfWeek(slot.getSessionDate().getDayOfWeek().toString())
//                .weekNumber(slot.getWeekNumber())
//                .build();
//    }


}