package com.labregistration.repository;

import com.labregistration.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    
    Optional<Faculty> findByCode(String code);
    
    Optional<Faculty> findByName(String name);
    
    List<Faculty> findByActiveTrue();
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsByNameAndIdNot(String name, Long id);
}
