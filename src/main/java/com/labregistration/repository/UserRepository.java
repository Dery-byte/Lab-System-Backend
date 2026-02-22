package com.labregistration.repository;

import com.labregistration.model.Role;
import com.labregistration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByStudentId(String studentId);
    
    boolean existsByEmail(String email);
    
    boolean existsByStudentId(String studentId);
    
    List<User> findByRole(Role role);
    
    List<User> findByRoleAndEnabled(Role role, Boolean enabled);
    
    @Query("SELECT u FROM User u WHERE u.program.id = :programId")
    List<User> findByProgramId(@Param("programId") Long programId);
    
    @Query("SELECT u FROM User u WHERE u.program.department.id = :departmentId")
    List<User> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.enabled = true")
    long countActiveByRole(@Param("role") Role role);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.studentId) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchByRoleAndKeyword(@Param("role") Role role, @Param("search") String search);
}
