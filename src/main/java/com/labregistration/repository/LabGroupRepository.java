package com.labregistration.repository;

import com.labregistration.model.LabGroup;
import com.labregistration.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabGroupRepository extends JpaRepository<LabGroup, Long> {
    List<LabGroup> findByLabSessionId(Long labSessionId);
    
    @Query("SELECT g FROM LabGroup g WHERE g.labSession.id = :sessionId ORDER BY g.groupNumber")
    List<LabGroup> findByLabSessionIdOrderByGroupNumber(@Param("sessionId") Long sessionId);
    
    Optional<LabGroup> findByLabSessionIdAndGroupNumber(Long labSessionId, Integer groupNumber);
    
    @Query("SELECT MAX(g.groupNumber) FROM LabGroup g WHERE g.labSession.id = :sessionId")
    Integer findMaxGroupNumberBySessionId(@Param("sessionId") Long sessionId);





































//    List<LabGroup> findByLabSessionId(Long labSessionId);

    @Query("SELECT g FROM LabGroup g WHERE g.labSession.id = :sessionId ORDER BY g.sessionDate, g.startTime, g.groupNumber")
    List<LabGroup> findByLabSessionIdOrdered(@Param("sessionId") Long sessionId);

//    Optional<LabGroup> findByLabSessionIdAndGroupNumber(Long labSessionId, Integer groupNumber);

    @Query("SELECT g FROM LabGroup g WHERE g.labSession.id = :sessionId AND g.sessionDate = :date ORDER BY g.startTime")
    List<LabGroup> findByLabSessionIdAndSessionDate(@Param("sessionId") Long sessionId, @Param("date") LocalDate date);

//    @Query("SELECT g FROM LabGroup g WHERE g.labSession.id = :sessionId AND " +
//            "(SELECT COUNT(r) FROM Registration r WHERE r.labGroup = g AND r.status IN ('PENDING', 'CONFIRMED')) < g.maxSize " +
//            "ORDER BY g.sessionDate, g.startTime")
//    List<LabGroup> findAvailableGroupsForSession(@Param("sessionId") Long sessionId);
//
//    @Query("SELECT g FROM LabGroup g LEFT JOIN FETCH g.members WHERE g.id = :groupId")
//    LabGroup findByIdWithMembers(@Param("groupId") Long groupId);

//    @Query("SELECT COUNT(r) FROM Registration r WHERE r.labGroup.id = :groupId AND r.status IN ('PENDING', 'CONFIRMED')")
//    int countActiveMembersInGroup(@Param("groupId") Long groupId);

    @Query("SELECT g FROM LabGroup g WHERE g.labSession.id = :sessionId AND g.groupName = :groupName")
    Optional<LabGroup> findBySessionIdAndGroupName(@Param("sessionId") Long sessionId, @Param("groupName") String groupName);

    @Query("SELECT MAX(g.groupNumber) FROM LabGroup g WHERE g.labSession.id = :sessionId")
    Integer findMaxGroupNumberForSession(@Param("sessionId") Long sessionId);

    @Query("SELECT DISTINCT g.sessionDate FROM LabGroup g WHERE g.labSession.id = :sessionId ORDER BY g.sessionDate")
    List<LocalDate> findDistinctSessionDatesBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Find time slots that conflict with a given time range for the same course level and date
     * This is used to prevent scheduling conflicts
     */
//    @Query("SELECT g FROM LabGroup g " +
//            "WHERE g.labSession.course.level = :level " +
//            "AND g.labSession.course.id = :courseId " +
//            "AND g.sessionDate = :date " +
//            "AND g.labSession.id != :excludeSessionId " +
//            "AND ((g.startTime < :endTime AND g.endTime > :startTime))")
//    List<LabGroup> findConflictingTimeSlots(
//            @Param("level") Level level,
//            @Param("courseId") Long courseId,
//            @Param("date") LocalDate date,
//            @Param("startTime") LocalTime startTime,
//            @Param("endTime") LocalTime endTime,
//            @Param("excludeSessionId") Long excludeSessionId);

    /**
     * Find all time slots for a specific level and date (for overview)
     */
    @Query("SELECT g FROM LabGroup g " +
            "WHERE g.labSession.course.level = :level " +
            "AND g.sessionDate = :date " +
            "ORDER BY g.startTime")
    List<LabGroup> findByLevelAndDate(@Param("level") Level level, @Param("date") LocalDate date);

    void deleteByLabSessionId(Long labSessionId);
}
