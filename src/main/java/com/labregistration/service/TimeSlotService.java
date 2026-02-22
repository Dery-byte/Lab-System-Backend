package com.labregistration.service;

import com.labregistration.dto.TimeSlotDTO;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.mapper.TimeSlotMapper;
import com.labregistration.model.LabSession;
import com.labregistration.model.TimeSlot;
import com.labregistration.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final LabSessionService labSessionService;
    private final TimeSlotMapper timeSlotMapper;

    // Get all slots for a session
    public List<TimeSlotDTO> getSlotsBySession(Long sessionId) {
        return timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId)
                .stream().map(timeSlotMapper::toDTO).collect(Collectors.toList());
    }

    // Get only available (not full) slots for a session
    public List<TimeSlotDTO> getAvailableSlots(Long sessionId) {
        return timeSlotRepository.findAvailableSlots(sessionId)
                .stream().map(timeSlotMapper::toDTO).collect(Collectors.toList());
    }

    // Get slots for a specific date
    public List<TimeSlotDTO> getSlotsByDate(Long sessionId, LocalDate date) {
        return timeSlotRepository.findByLabSessionIdAndDate(sessionId, date)
                .stream().map(timeSlotMapper::toDTO).collect(Collectors.toList());
    }

    // Get slots by slot number across all dates
    public List<TimeSlotDTO> getSlotsBySlotNumber(Long sessionId, Integer slotNumber) {
        return timeSlotRepository.findByLabSessionIdAndGroupNumber(sessionId, slotNumber)
                .stream().map(timeSlotMapper::toDTO).collect(Collectors.toList());
    }

    // Get upcoming slots only
    public List<TimeSlotDTO> getUpcomingSlots(Long sessionId) {
        return timeSlotRepository.findUpcomingSlots(sessionId, LocalDate.now())
                .stream().map(timeSlotMapper::toDTO).collect(Collectors.toList());
    }

    // Get a single slot by ID
    public TimeSlotDTO getSlotById(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));
        return timeSlotMapper.toDTO(slot);
    }

    // Create a single time slot
    @Transactional
    public TimeSlotDTO createSlot(Long sessionId, TimeSlotDTO request) {
        LabSession session = labSessionService.getLabSessionEntityById(sessionId);

        TimeSlot slot = TimeSlot.builder()
                .labSession(session)
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .groupNumber(request.getSlotNumber())
                .maxStudents(request.getMaxStudents())
                .currentCount(0)
                .active(true)
                .build();

        slot = timeSlotRepository.save(slot);
        log.info("Created time slot {} for session {}", slot.getId(), sessionId);
        return timeSlotMapper.toDTO(slot);
    }

    // Update a time slot
    @Transactional
    public TimeSlotDTO updateSlot(Long slotId, TimeSlotDTO request) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));

        if (request.getMaxStudents() != null && request.getMaxStudents() < slot.getCurrentCount()) {
            throw new BadRequestException("Max students cannot be less than current registrations (" + slot.getCurrentCount() + ")");
        }

        if (request.getSessionDate() != null) slot.setSessionDate(request.getSessionDate());
        if (request.getStartTime() != null) slot.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) slot.setEndTime(request.getEndTime());
        if (request.getSlotNumber() != null) slot.setGroupNumber(request.getSlotNumber());
        if (request.getMaxStudents() != null) slot.setMaxStudents(request.getMaxStudents());

        slot = timeSlotRepository.save(slot);
        log.info("Updated time slot {}", slotId);
        return timeSlotMapper.toDTO(slot);
    }

    // Activate / Deactivate a slot
    @Transactional
    public void deactivateSlot(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));
        slot.setActive(false);
        timeSlotRepository.save(slot);
        log.info("Deactivated time slot {}", slotId);
    }

    @Transactional
    public void activateSlot(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));
        slot.setActive(true);
        timeSlotRepository.save(slot);
        log.info("Activated time slot {}", slotId);
    }

    // Delete a slot (only if no registrations)
    @Transactional
    public void deleteSlot(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));

        if (slot.getCurrentCount() > 0) {
            throw new BadRequestException("Cannot delete a slot with active registrations (" + slot.getCurrentCount() + " students)");
        }

        timeSlotRepository.delete(slot);
        log.info("Deleted time slot {}", slotId);
    }

    // Get slot capacity summary for a session
    public SlotSummary getSlotSummary(Long sessionId) {
        List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId);
        int totalCapacity = slots.stream().mapToInt(TimeSlot::getMaxStudents).sum();
        int totalRegistered = slots.stream().mapToInt(TimeSlot::getCurrentCount).sum();
        int totalAvailable = slots.stream().mapToInt(TimeSlot::getAvailableSlots).sum();
        return new SlotSummary(slots.size(), totalCapacity, totalRegistered, totalAvailable);
    }

    public record SlotSummary(int totalSlots, int totalCapacity, int totalRegistered, int totalAvailable) {}



    // Add these to TimeSlotService

    public Map<String, List<TimeSlotDTO>> getGroupedByDate(Long sessionId) {
        return timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId)
                .stream()
                .map(timeSlotMapper::toDTO)
                .collect(Collectors.groupingBy(slot -> slot.getSessionDate().toString()));
    }

    public int getTotalRegistrations(Long sessionId) {
        return timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId)
                .stream()
                .mapToInt(TimeSlot::getCurrentCount)
                .sum();
    }

    public List<TimeSlotDTO> createBulk(Long sessionId, List<TimeSlotDTO> requests) {
        return requests.stream()
                .map(request -> createSlot(sessionId, request))
                .collect(Collectors.toList());
    }

    public TimeSlotDTO toggleActive(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));
        slot.setActive(!slot.getActive());
        slot = timeSlotRepository.save(slot);
        log.info("Toggled time slot {} active={}", slotId, slot.getActive());
        return timeSlotMapper.toDTO(slot);
    }





}