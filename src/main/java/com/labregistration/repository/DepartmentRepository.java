package com.labregistration.repository;

import com.labregistration.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByCode(String code);
    
    Optional<Department> findByName(String name);
    
    List<Department> findByActiveTrue();
    
    @Query("SELECT d FROM Department d WHERE d.faculty.id = :facultyId")
    List<Department> findByFacultyId(@Param("facultyId") Long facultyId);
    
    @Query("SELECT d FROM Department d WHERE d.faculty.id = :facultyId AND d.active = true")
    List<Department> findActiveByFacultyId(@Param("facultyId") Long facultyId);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
}
