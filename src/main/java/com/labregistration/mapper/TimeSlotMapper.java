package com.labregistration.mapper;

import com.labregistration.dto.TimeSlotDTO;
import com.labregistration.model.TimeSlot;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {
    public TimeSlotDTO toDTO(TimeSlot slot) {
        return TimeSlotDTO.builder()
                .id(slot.getId())
                .sessionDate(slot.getSessionDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .slotNumber(slot.getGroupNumber())
                .maxStudents(slot.getMaxStudents())
                .currentCount(slot.getCurrentCount())
                .availableSlots(slot.getAvailableSlots())
                .displayName(slot.getDisplayName())
                .active(slot.getActive())
                .weekNumber(slot.getWeekNumber())
                .build();
    }
}