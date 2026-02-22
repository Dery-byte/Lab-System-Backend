package com.labregistration.repository;

import com.labregistration.model.LabSession;
import com.labregistration.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.labSession.id = :sessionId ORDER BY ts.sessionDate, ts.groupNumber")
    List<TimeSlot> findByLabSessionIdOrderByDateAndSlot(@Param("sessionId") Long sessionId);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.labSession.id = :sessionId AND ts.sessionDate = :date ORDER BY ts.groupNumber")
    List<TimeSlot> findByLabSessionIdAndDate(@Param("sessionId") Long sessionId, @Param("date") LocalDate date);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.labSession.id = :sessionId AND ts.active = true AND ts.currentCount < ts.maxStudents ORDER BY ts.sessionDate, ts.groupNumber")
    List<TimeSlot> findAvailableSlots(@Param("sessionId") Long sessionId);














    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.labSession.id = :sessionId AND ts.groupNumber = :slotNumber")
    List<TimeSlot> findByLabSessionIdAndGroupNumber(@Param("sessionId") Long sessionId, @Param("slotNumber") Integer slotNumber);
    
    // Find the first available slot across all dates for a session
//    @Query("SELECT ts FROM TimeSlot ts WHERE ts.labSession.id = :sessionId AND ts.active = true AND ts.currentCount < ts.maxStudents ORDER BY ts.groupNumber, ts.sessionDate")
//    Optional<TimeSlot> findFirstAvailableSlot(@Param("sessionId") Long sessionId);




    @Query("SELECT t FROM TimeSlot t WHERE t.labSession.id = :sessionId " +
            "AND t.active = true AND t.currentCount < t.maxStudents " +
            "AND t.sessionDate >= CURRENT_DATE " +
            "ORDER BY t.sessionDate ASC, t.groupNumber ASC " +
            "LIMIT 1")
    Optional<TimeSlot> findFirstAvailableSlot(@Param("sessionId") Long sessionId);




    
    @Modifying
    @Query("UPDATE TimeSlot ts SET ts.currentCount = ts.currentCount + 1 WHERE ts.id = :slotId AND ts.currentCount < ts.maxStudents")
    int incrementCount(@Param("slotId") Long slotId);
    
    @Modifying
    @Query("UPDATE TimeSlot ts SET ts.currentCount = ts.currentCount - 1 WHERE ts.id = :slotId AND ts.currentCount > 0")
    int decrementCount(@Param("slotId") Long slotId);
    
    @Query("SELECT COALESCE(MAX(ts.groupNumber), 0) FROM TimeSlot ts WHERE ts.labSession.id = :sessionId")
    int findMaxSlotNumberBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.labSession.id = :sessionId AND ts.sessionDate >= :today ORDER BY ts.sessionDate, ts.groupNumber")
    List<TimeSlot> findUpcomingSlots(@Param("sessionId") Long sessionId, @Param("today") LocalDate today);

    List<TimeSlot> findByLabSession(LabSession session);



//    REPORT STUFF

    List<TimeSlot> findBySessionDateBetweenOrderBySessionDateAsc(LocalDate startDate, LocalDate endDate);


}
