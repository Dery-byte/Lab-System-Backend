package com.labregistration.repository;

import com.labregistration.model.Course;
import com.labregistration.model.Level;
import com.labregistration.model.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByCourseCode(String courseCode);
    
    List<Course> findByActiveTrue();
    
    List<Course> findByHasLabTrue();
    
    List<Course> findByActiveTrueAndHasLabTrue();
    
    @Query("SELECT c FROM Course c WHERE c.department.id = :departmentId")
    List<Course> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    List<Course> findByLevel(Level level);
    
    List<Course> findBySemester(Semester semester);
    
    List<Course> findByAcademicYear(String academicYear);
    
    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId")
    List<Course> findByInstructorId(@Param("instructorId") Long instructorId);
    
    boolean existsByCourseCode(String courseCode);




    @Query("SELECT c FROM Course c WHERE c.departmentName = :departmentName")
    List<Course> findByDepartmentName(@Param("departmentName") String departmentName);

    @Query("SELECT c FROM Course c WHERE c.active = true AND c.hasLab = true AND " +
           "(LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.courseName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Course> searchActiveLabCourses(@Param("search") String search);
}
