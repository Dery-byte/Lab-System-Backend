package com.labregistration.service;

import com.labregistration.dto.UpdateLabManagerRequest;
import com.labregistration.dto.UserDTO;
import com.labregistration.dto.request.CreateLabManagerRequest;
import com.labregistration.exception.BadRequestException;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.mapper.UserMapper;
import com.labregistration.model.Department;
import com.labregistration.model.Level;
import com.labregistration.model.Role;
import com.labregistration.model.User;
import com.labregistration.repository.DepartmentRepository;
import com.labregistration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user);
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public List<UserDTO> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllLabManagers() {
        return userRepository.findByRole(Role.LAB_MANAGER).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchStudents(String keyword) {
        return userRepository.searchByRoleAndKeyword(Role.STUDENT, keyword).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createLabManager(CreateLabManagerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new BadRequestException("Staff ID already registered");
        }

        User labManager = User.builder()
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .level(Level.LEVEL_100) // Default, not really applicable for staff
                .role(Role.LAB_MANAGER)
                .enabled(true)
                .emailVerified(true)
                .build();

        labManager = userRepository.save(labManager);
        log.info("Created lab manager: {}", labManager.getEmail());
        return userMapper.toDTO(labManager);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (updateDTO.getFirstName() != null) user.setFirstName(updateDTO.getFirstName());
        if (updateDTO.getLastName() != null) user.setLastName(updateDTO.getLastName());
        if (updateDTO.getPhoneNumber() != null) user.setPhoneNumber(updateDTO.getPhoneNumber());
        if (updateDTO.getLevel() != null) user.setLevel(updateDTO.getLevel());

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Deactivated user: {}", user.getEmail());
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Activated user: {}", user.getEmail());
    }

    public long countStudents() {
        return userRepository.countByRole(Role.STUDENT);
    }

    public long countLabManagers() {
        return userRepository.countByRole(Role.LAB_MANAGER);
    }




//    public UserDTO updateLabManager(Long id, UpdateLabManagerRequest request) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Lab Manager not found with id: " + id));
//
//        user.setFirstName(request.getFirstName());
//        user.setLastName(request.getLastName());
//        user.setEmail(request.getEmail());
//        user.setPhoneNumber(request.getPhoneNumber());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        User updated = userRepository.save(user);
//        return userMapper.toDTO(updated);
//    }


    public UserDTO updateLabManager(Long id, UpdateLabManagerRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Manager not found with id: " + id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
//        user.setEmployeeId(request.getEmployeeId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getDepartment() != null) {
            Department department = departmentRepository.findByName(request.getDepartment())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartment()));
            user.setDepartment(department);
        }

        User updated = userRepository.save(user);
        return userMapper.toDTO(updated);
    }
}
