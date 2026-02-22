package com.labregistration.mapper;

import com.labregistration.dto.LabGroupDTO;
import com.labregistration.model.LabGroup;
import com.labregistration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LabGroupMapper {

    private final RegistrationRepository registrationRepository;

    public LabGroupDTO toDTO(LabGroup group) {
        if (group == null) return null;

        int currentSize = registrationRepository.countByLabSessionIdAndActiveTrue(group.getId());

        return LabGroupDTO.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .groupNumber(group.getGroupNumber())
                .maxSize(group.getMaxSize())
                .sessionDate(group.getSessionDate())
                .startTime(group.getStartTime())
                .endTime(group.getEndTime())
                .labSessionId(group.getLabSession().getId())
                .currentSize(currentSize)
                .availableSlots(group.getMaxSize() - currentSize)
                .isFull(currentSize >= group.getMaxSize())
                .build();
    }

    public LabGroupDTO toDTOWithMembers(LabGroup group, java.util.List<com.labregistration.dto.RegistrationDTO> members) {
        LabGroupDTO dto = toDTO(group);
        dto.setMembers(members);
        return dto;
    }
}
