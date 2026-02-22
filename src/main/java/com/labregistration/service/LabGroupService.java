package com.labregistration.service;

import com.labregistration.dto.LabGroupDTO;
import com.labregistration.dto.RegistrationDTO;
import com.labregistration.dto.request.CreateLabGroupRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.mapper.LabGroupMapper;
import com.labregistration.mapper.RegistrationMapper;
import com.labregistration.model.LabGroup;
import com.labregistration.model.LabSession;
import com.labregistration.repository.LabGroupRepository;
import com.labregistration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabGroupService {

    private final LabGroupRepository labGroupRepository;
    private final RegistrationRepository registrationRepository;
    private final LabSessionService labSessionService;
    private final LabGroupMapper labGroupMapper;
    private final RegistrationMapper registrationMapper;

    @Transactional
    public LabGroupDTO createGroup(Long sessionId, CreateLabGroupRequest request) {
        LabSession session = labSessionService.getLabSessionEntityById(sessionId);

        Integer maxGroupNumber = labGroupRepository.findMaxGroupNumberBySessionId(sessionId);
        int nextGroupNumber = (maxGroupNumber != null ? maxGroupNumber : 0) + 1;

        LabGroup group = LabGroup.builder()
                .groupName(request.getGroupName())
                .groupNumber(nextGroupNumber)
                .maxSize(request.getMaxSize())
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .labSession(session)
                .build();

        group = labGroupRepository.save(group);
        log.info("Group created: {} for session {}", group.getGroupName(), session.getName());
        return labGroupMapper.toDTO(group);
    }

    public LabGroupDTO getGroupById(Long id) {
        LabGroup group = labGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Group", "id", id));
        return labGroupMapper.toDTO(group);
    }

    public LabGroup getGroupEntityById(Long id) {
        return labGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Group", "id", id));
    }

    public List<LabGroupDTO> getGroupsBySessionId(Long sessionId) {
        return labGroupRepository.findByLabSessionIdOrderByGroupNumber(sessionId).stream()
                .map(labGroupMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<LabGroupDTO> getGroupsBySessionIdWithMembers(Long sessionId) {
        return labGroupRepository.findByLabSessionIdOrderByGroupNumber(sessionId).stream()
                .map(group -> {
                    List<RegistrationDTO> members = registrationRepository.findByLabSessionIdAndActiveTrue(group.getId())
                            .stream()
                            .map(registrationMapper::toBasicDTO)
                            .collect(Collectors.toList());
                    return labGroupMapper.toDTOWithMembers(group, members);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public LabGroupDTO updateGroup(Long id, CreateLabGroupRequest request) {
        LabGroup group = labGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Group", "id", id));

        group.setGroupName(request.getGroupName());
        group.setMaxSize(request.getMaxSize());
        group.setSessionDate(request.getSessionDate());
        group.setStartTime(request.getStartTime());
        group.setEndTime(request.getEndTime());

        group = labGroupRepository.save(group);
        return labGroupMapper.toDTO(group);
    }

    @Transactional
    public void deleteGroup(Long id) {
        LabGroup group = labGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Group", "id", id));

        int memberCount = registrationRepository.countByLabSessionIdAndActiveTrue(id);
        if (memberCount > 0) {
            throw new BadRequestException("Cannot delete group with active registrations");
        }

        labGroupRepository.delete(group);
    }
}
