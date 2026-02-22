package com.labregistration.repository;

import com.labregistration.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    
    Optional<Program> findByCode(String code);
    
    Optional<Program> findByName(String name);
    
    List<Program> findByActiveTrue();
    
    @Query("SELECT p FROM Program p WHERE p.department.id = :departmentId")
    List<Program> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT p FROM Program p WHERE p.department.id = :departmentId AND p.active = true")
    List<Program> findActiveByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT p FROM Program p WHERE p.department.faculty.id = :facultyId")
    List<Program> findByFacultyId(@Param("facultyId") Long facultyId);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
}
