package com.labregistration.repository;

import com.labregistration.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    @Query("SELECT a FROM Attendance a WHERE a.registration.id = :registrationId ORDER BY a.sessionDate")
    List<Attendance> findByRegistrationId(@Param("registrationId") Long registrationId);
    
    @Query("SELECT a FROM Attendance a WHERE a.registration.id = :registrationId AND a.sessionDate = :date")
    Optional<Attendance> findByRegistrationIdAndDate(@Param("registrationId") Long registrationId, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.registration.labSession.id = :sessionId AND a.sessionDate = :date")
    List<Attendance> findBySessionIdAndDate(@Param("sessionId") Long sessionId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.registration.id = :registrationId AND a.present = true")
    int countPresentByRegistrationId(@Param("registrationId") Long registrationId);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.registration.id = :registrationId")
    int countTotalByRegistrationId(@Param("registrationId") Long registrationId);
    
    // Get attendance stats for a session on a specific date
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.registration.labSession.id = :sessionId AND a.sessionDate = :date AND a.present = true")
    int countPresentBySessionIdAndDate(@Param("sessionId") Long sessionId, @Param("date") LocalDate date);
    
    // Find all attendance records for a session
    @Query("SELECT a FROM Attendance a WHERE a.registration.labSession.id = :sessionId ORDER BY a.sessionDate, a.registration.student.lastName")
    List<Attendance> findBySessionId(@Param("sessionId") Long sessionId);
}
