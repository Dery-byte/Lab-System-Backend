//package com.labregistration.mapper;
//
//import com.labregistration.dto.RegistrationDTO;
//import com.labregistration.dto.WeeklyNoteDTO;
//import com.labregistration.model.LabSession;
//import com.labregistration.model.Registration;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class RegistrationMapper {
//
//    private final UserMapper userMapper;
//    @Lazy
//    private final LabSessionMapper labSessionMapper;
//    @Lazy
//    private final LabGroupMapper labGroupMapper;
//
//    public RegistrationDTO toDTO(Registration registration) {
//        if (registration == null) return null;
//
//        LabSession session = registration.getLabSession();
//
//        return RegistrationDTO.builder()
//                .id(registration.getId())
//                .studentId(registration.getStudent().getId())
//                .studentName(registration.getStudent().getFullName())
//                .studentEmail(registration.getStudent().getEmail())
//                .studentIdNumber(registration.getStudent().getStudentId())
//                .programName(registration.getStudent().getProgramName())
//                .labSessionId(session.getId())
//                .labSessionName(session.getName())
//                .courseCode(session.getCourse() != null ? session.getCourse().getCourseCode() : null)
//                .courseName(session.getCourse() != null ? session.getCourse().getCourseName() : null)
//                .labRoom(session.getLabRoom())
//                .sessionStartDate(session.getStartDate())
//                .sessionEndDate(session.getEndDate())
//                .sessionStartTime(session.getStartTime())
//                .sessionEndTime(session.getEndTime())
//                .sessionDays(session.getSessionDaysSet())
//                .durationWeeks(session.getDurationWeeks())
//                .instructions(session.getInstructions())
//                .timeSlotId(registration.getTimeSlot() != null ? registration.getTimeSlot().getId() : null)
//                .timeSlotDisplayName(registration.getTimeSlot() != null ? registration.getTimeSlot().getDisplayName() : null)
//                .slotNumber(registration.getTimeSlot() != null ? registration.getTimeSlot().getSlotNumber() : null)
//                .status(registration.getStatus())
//                .waitlistPosition(registration.getWaitlistPosition())
//                .registeredAt(registration.getRegisteredAt())
//                .confirmedAt(registration.getConfirmedAt())
//                .cancelledAt(registration.getCancelledAt())
//                .completedAt(registration.getCompletedAt())
//                .studentNotes(registration.getStudentNotes())
//                .adminNotes(registration.getAdminNotes())
//                .attendedSessions(registration.getAttendedSessions())
//                .totalSessions(registration.getTotalSessions())
//                .attendancePercentage(registration.getAttendancePercentage())
//                .score(registration.getScore())
//                .grade(registration.getGrade())
//                .createdAt(registration.getCreatedAt())
//                .build();
//    }
//
//    public RegistrationDTO toDTOWithWeeklyNotes(Registration registration, List<WeeklyNoteDTO> weeklyNotes) {
//        RegistrationDTO dto = toDTO(registration);
//        if (dto != null) {
//            dto.setWeeklyNotes(weeklyNotes);
//        }
//        return dto;
//    }
//
//
//
//    public RegistrationDTO toBasicDTO(Registration registration) {
//        if (registration == null) return null;
//
//        return RegistrationDTO.builder()
//                .id(registration.getId())
//                .student(userMapper.toDTO(registration.getStudent()))
//                .status(registration.getStatus())
//                .registeredAt(registration.getRegisteredAt())
//                .build();
//    }
//}





package com.labregistration.mapper;

import com.labregistration.dto.RegistrationDTO;
import com.labregistration.dto.WeeklyNoteDTO;
import com.labregistration.model.LabSession;
import com.labregistration.model.Registration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrationMapper {

    private final UserMapper userMapper;

    @Lazy
    private final LabSessionMapper labSessionMapper;

    @Lazy
    private final LabGroupMapper labGroupMapper;

    /**
     * Full detailed mapping
     */
    public RegistrationDTO toDTO(Registration registration) {
        if (registration == null) return null;

        LabSession session = registration.getLabSession();

        return RegistrationDTO.builder()
                .id(registration.getId())

                // ========================
                // STUDENT DETAILS
                // ========================
                .student(userMapper.toDTO(registration.getStudent()))
                .studentId(registration.getStudent().getId())
                .studentName(registration.getStudent().getFullName())
                .studentEmail(registration.getStudent().getEmail())
                .studentIdNumber(registration.getStudent().getStudentId())
                .programName(registration.getStudent().getProgramName())

                // ========================
                // SESSION DETAILS
                // ========================
//                .labSession(labSessionMapper.toDTO(session))
                .labSessionId(session.getId())
                .labSessionName(session.getName())
                .courseCode(session.getCourse() != null ? session.getCourse().getCourseCode() : null)
                .courseName(session.getCourse() != null ? session.getCourse().getCourseName() : null)
                .labRoom(session.getLabRoom())
                .sessionStartDate(session.getStartDate())
                .sessionEndDate(session.getEndDate())
                .sessionStartTime(session.getStartTime())
                .sessionEndTime(session.getEndTime())
                .sessionDays(session.getSessionDaysSet())
                .durationWeeks(session.getDurationWeeks())
                .instructions(session.getInstructions())

                // ========================
                // GROUP (Optional)
                // ========================
//                .labGroup(registration.getLabGroup() != null
//                        ? labGroupMapper.toDTO(registration.getLabGroup())
//                        : null)
                // ========================
                // TIME SLOT
                // ========================
                .timeSlotId(registration.getTimeSlot() != null ? registration.getTimeSlot().getId() : null)
                .timeSlotDisplayName(registration.getTimeSlot() != null ? registration.getTimeSlot().getDisplayName() : null)
                .slotNumber(registration.getTimeSlot() != null ? registration.getTimeSlot().getGroupNumber() : null)

                // ========================
                // STATUS & WORKFLOW
                // ========================
                .status(registration.getStatus())
                .waitlistPosition(registration.getWaitlistPosition())
                .registeredAt(registration.getRegisteredAt())
                .confirmedAt(registration.getConfirmedAt())
                .cancelledAt(registration.getCancelledAt())
                .completedAt(registration.getCompletedAt())

                // ========================
                // NOTES
                // ========================
                .studentNotes(registration.getStudentNotes())
                .adminNotes(registration.getAdminNotes())

                // ========================
                // ATTENDANCE & GRADING
                // ========================
                .attendedSessions(registration.getAttendedSessions())
                .totalSessions(registration.getTotalSessions())
                .attendancePercentage(registration.getAttendancePercentage())
                .score(registration.getScore())
                .grade(registration.getGrade())

                // ========================
                // METADATA
                // ========================
//                .isFavorite(registration.getIsFavorite())
                .createdAt(registration.getCreatedAt())
                .build();
    }

    /**
     * Full mapping + weekly notes
     */
    public RegistrationDTO toDTOWithWeeklyNotes(
            Registration registration,
            List<WeeklyNoteDTO> weeklyNotes
    ) {
        RegistrationDTO dto = toDTO(registration);
        if (dto != null) {
            dto.setWeeklyNotes(weeklyNotes);
        }
        return dto;
    }

    /**
     * Lightweight version (for group member listing)
     */
    public RegistrationDTO toBasicDTO(Registration registration) {
        if (registration == null) return null;

        return RegistrationDTO.builder()
                .id(registration.getId())
                .student(userMapper.toDTO(registration.getStudent()))
                .status(registration.getStatus())
                .registeredAt(registration.getRegisteredAt())
                .build();
    }
}