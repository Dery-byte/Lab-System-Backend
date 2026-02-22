package com.labregistration.repository;

import com.labregistration.model.LabSession;
import com.labregistration.model.Registration;
import com.labregistration.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    @Query("SELECT r FROM Registration r WHERE r.student.id = :studentId ORDER BY r.createdAt DESC")
    List<Registration> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT r FROM Registration r WHERE r.labSession.id = :sessionId ORDER BY r.createdAt")
    List<Registration> findByLabSessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT r FROM Registration r WHERE r.timeSlot.id = :slotId")
    List<Registration> findByTimeSlotId(@Param("slotId") Long slotId);
    
    @Query("SELECT r FROM Registration r WHERE r.student.id = :studentId AND r.labSession.id = :sessionId")
    Optional<Registration> findByStudentIdAndLabSessionId(@Param("studentId") Long studentId, @Param("sessionId") Long sessionId);
    
    // Find active registrations (not cancelled)
    @Query("SELECT r FROM Registration r WHERE r.student.id = :studentId AND r.status IN ('PENDING', 'CONFIRMED', 'WAITLISTED') ORDER BY r.createdAt DESC")
    List<Registration> findActiveByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT r FROM Registration r WHERE r.labSession.id = :sessionId AND r.status IN ('PENDING', 'CONFIRMED', 'WAITLISTED')")
    List<Registration> findActiveBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT r FROM Registration r WHERE r.timeSlot.id = :slotId AND r.status IN ('CONFIRMED')")
    List<Registration> findConfirmedBySlotId(@Param("slotId") Long slotId);
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.timeSlot.id = :slotId AND r.status = 'CONFIRMED'")
    int countConfirmedBySlotId(@Param("slotId") Long slotId);
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.labSession.id = :sessionId AND r.status = 'CONFIRMED'")
    int countConfirmedBySessionId(@Param("sessionId") Long sessionId);
    
    // Find waitlisted registrations ordered by position
    @Query("SELECT r FROM Registration r WHERE r.labSession.id = :sessionId AND r.status = 'WAITLISTED' ORDER BY r.waitlistPosition")
    List<Registration> findWaitlistedBySessionId(@Param("sessionId") Long sessionId);
    
    // Check if student already has an active registration for a session
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Registration r " +
           "WHERE r.student.id = :studentId AND r.labSession.id = :sessionId AND r.status IN :statuses")
    boolean existsByStudentIdAndLabSessionIdAndStatusIn(
            @Param("studentId") Long studentId, 
            @Param("sessionId") Long sessionId, 
            @Param("statuses") List<RegistrationStatus> statuses);
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.status = :status")
    long countByStatus(@Param("status") RegistrationStatus status);
    
    List<Registration> findByStatus(RegistrationStatus status);
    
    // Find registrations for a course
    @Query("SELECT r FROM Registration r WHERE r.labSession.course.id = :courseId ORDER BY r.createdAt DESC")
    List<Registration> findByCourseId(@Param("courseId") Long courseId);

//    List<Registration> findActiveByGroupId(Long groupId);

    List<Registration> findByLabSessionIdAndActiveTrue(Long labSessionId);

//    int countActiveByGroupId(Long id);












    @Query("SELECT r FROM Registration r WHERE r.labSession.id = :sessionId AND r.status = 'WAITLISTED' ORDER BY r.registeredAt")
    List<Registration> findWaitlistedBySessionOrderedByTime(@Param("sessionId") Long sessionId);
//
//
//    @Query("SELECT COUNT(r) FROM Registration r WHERE r.labSession.id = :labSessionId AND r.status IN ('PENDING', 'CONFIRMED')")
//    int countActiveRegistrationsForSession(@Param("labSessionId") Long labSessionId);  @Query("SELECT COUNT(r) FROM Registration r WHERE r.labSession.id = :sessionId AND r.status IN ('PENDING', 'CONFIRMED')")

    int countByLabSession(LabSession session);


    int countByLabSessionIdAndActiveTrue(Long labSessionId);

//    List<Registration> findByLabGroupId(Long id);


    int countByLabSessionIdAndStatus(Long labSessionId, RegistrationStatus status);


    Optional<Registration> findByStudentIdAndLabSessionIdAndStatus(
            Long studentId, Long labSessionId, RegistrationStatus status);














//    REPORT STUFF
        List<Registration> findByLabSessionIdAndStatusOrderByWaitlistPosition(
            Long labSessionId, RegistrationStatus status);


    List<Registration> findByLabSessionIdAndStatusOrderByWaitlistPositionAsc(
            Long labSessionId, RegistrationStatus status);
}
