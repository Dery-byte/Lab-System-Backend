package com.labregistration.service;

import com.labregistration.dto.AttendanceDTO;
import com.labregistration.dto.request.MarkAttendanceRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.model.Attendance;
import com.labregistration.model.Registration;
import com.labregistration.model.User;
import com.labregistration.repository.AttendanceRepository;
import com.labregistration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final RegistrationRepository registrationRepository;
    private final UserService userService;

    @Transactional
    public List<AttendanceDTO> markAttendance(MarkAttendanceRequest request, String markerEmail) {
        User marker = userService.getUserEntityByEmail(markerEmail);
        List<AttendanceDTO> results = new ArrayList<>();

        for (MarkAttendanceRequest.AttendanceRecord record : request.getAttendances()) {
            Registration registration = registrationRepository.findById(record.getRegistrationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", record.getRegistrationId()));

            // Verify this registration belongs to the specified session
            if (!registration.getLabSession().getId().equals(request.getLabSessionId())) {
                throw new BadRequestException("Registration " + record.getRegistrationId() + 
                        " does not belong to session " + request.getLabSessionId());
            }

            // Check if attendance already exists for this date
            Attendance attendance = attendanceRepository
                    .findByRegistrationIdAndDate(record.getRegistrationId(), request.getSessionDate())
                    .orElse(null);

            if (attendance == null) {
                attendance = Attendance.builder()
                        .registration(registration)
                        .sessionDate(request.getSessionDate())
                        .build();
            }

            attendance.setPresent(record.getPresent());
            attendance.setNotes(record.getNotes());
            attendance.setMarkedBy(marker);
            
            if (record.getPresent()) {
                attendance.setCheckInTime(LocalDateTime.now());
            }

            attendance = attendanceRepository.save(attendance);

            // Update registration attendance count
            updateAttendanceCount(registration);

            results.add(toDTO(attendance));
        }

        log.info("Marked attendance for {} students on {} for session {}", 
                results.size(), request.getSessionDate(), request.getLabSessionId());
        return results;
    }

    private void updateAttendanceCount(Registration registration) {
        int presentCount = attendanceRepository.countPresentByRegistrationId(registration.getId());
        int totalCount = attendanceRepository.countTotalByRegistrationId(registration.getId());
        
        registration.setAttendedSessions(presentCount);
        registration.setTotalSessions(Math.max(registration.getTotalSessions(), totalCount));
        registrationRepository.save(registration);
    }

    public List<AttendanceDTO> getAttendanceByRegistration(Long registrationId) {
        return attendanceRepository.findByRegistrationId(registrationId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAttendanceBySessionAndDate(Long sessionId, LocalDate date) {
        return attendanceRepository.findBySessionIdAndDate(sessionId, date).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAllAttendanceBySession(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private AttendanceDTO toDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .id(attendance.getId())
                .registrationId(attendance.getRegistration().getId())
                .studentId(attendance.getRegistration().getStudent().getId())
                .studentName(attendance.getRegistration().getStudent().getFullName())
                .studentIdNumber(attendance.getRegistration().getStudent().getStudentId())
                .sessionDate(attendance.getSessionDate())
                .present(attendance.getPresent())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .notes(attendance.getNotes())
                .markedByName(attendance.getMarkedBy() != null ? attendance.getMarkedBy().getFullName() : null)
                .createdAt(attendance.getCreatedAt())
                .build();
    }
}
