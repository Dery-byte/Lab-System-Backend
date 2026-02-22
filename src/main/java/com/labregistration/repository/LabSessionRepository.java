package com.labregistration.repository;

import com.labregistration.model.LabSession;
import com.labregistration.model.Level;
import com.labregistration.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabSessionRepository extends JpaRepository<LabSession, Long> {
    
    List<LabSession> findByStatus(SessionStatus status);
    
    @Query("SELECT ls FROM LabSession ls WHERE ls.course.id = :courseId")
    List<LabSession> findByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT ls FROM LabSession ls WHERE ls.createdBy.id = :userId")
    List<LabSession> findByCreatedById(@Param("userId") Long userId);
    
    @Query("SELECT ls FROM LabSession ls WHERE ls.status = 'OPEN' AND ls.endDate >= :today")
    List<LabSession> findAvailableSessions(@Param("today") LocalDate today);
    
    @Query("SELECT ls FROM LabSession ls WHERE ls.status = 'OPEN' AND ls.course.id = :courseId AND ls.endDate >= :today")
    List<LabSession> findAvailableSessionsByCourse(@Param("courseId") Long courseId, @Param("today") LocalDate today);
    
    // Find sessions that a specific program can register for
    @Query("SELECT DISTINCT ls FROM LabSession ls " +
           "LEFT JOIN ls.allowedPrograms p " +
           "WHERE ls.status = 'OPEN' AND ls.endDate >= :today " +
           "AND (ls.openToAllPrograms = true OR p.id = :programId)")
    List<LabSession> findAvailableSessionsForProgram(@Param("programId") Long programId, @Param("today") LocalDate today);
    
    @Query("SELECT COUNT(ls) FROM LabSession ls WHERE ls.status = :status")
    long countByStatus(@Param("status") SessionStatus status);
    
    @Query("SELECT ls FROM LabSession ls WHERE ls.startDate <= :date AND ls.endDate >= :date")
    List<LabSession> findSessionsOnDate(@Param("date") LocalDate date);
    
    // Find sessions that need status update (past end date but still OPEN)
    @Query("SELECT ls FROM LabSession ls WHERE ls.status = 'OPEN' AND ls.endDate < :today")
    List<LabSession> findSessionsToClose(@Param("today") LocalDate today);





    // Add these to your LabSessionRepository interface

    @Query("SELECT s FROM LabSession s WHERE s.startDate <= :endDate AND s.endDate >= :startDate")
    List<LabSession> findSessionsBetweenDates(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("""
    SELECT s FROM LabSession s 
    WHERE s.status = 'OPEN' 
      AND EXISTS (
          SELECT t FROM TimeSlot t 
          WHERE t.labSession = s 
            AND t.sessionDate >= :today 
            AND t.currentCount < t.maxStudents 
            AND t.active = true
      )
""")
    List<LabSession> findSessionsWithAvailableSlots(@Param("today") LocalDate today);


    @Query("""
    SELECT s FROM LabSession s 
    WHERE s.labRoom = :labRoom 
      AND s.startDate <= :date 
      AND s.endDate >= :date
""")
    List<LabSession> findByLabRoomAndDate(@Param("labRoom") String labRoom,
                                          @Param("date") LocalDate date);


}
