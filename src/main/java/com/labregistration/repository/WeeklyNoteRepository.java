package com.labregistration.repository;

import com.labregistration.model.WeeklyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyNoteRepository extends JpaRepository<WeeklyNote, Long> {

    List<WeeklyNote> findByLabSessionIdOrderByWeekNumber(Long labSessionId);

    Optional<WeeklyNote> findByLabSessionIdAndWeekNumber(Long labSessionId, Integer weekNumber);

    @Query("SELECT wn FROM WeeklyNote wn WHERE wn.labSession.id = :labSessionId AND wn.isPublished = true ORDER BY wn.weekNumber")
    List<WeeklyNote> findPublishedByLabSessionId(@Param("labSessionId") Long labSessionId);

    @Query("SELECT COUNT(wn) FROM WeeklyNote wn WHERE wn.labSession.id = :labSessionId AND wn.isPublished = true")
    int countPublishedByLabSessionId(@Param("labSessionId") Long labSessionId);

    void deleteByLabSessionId(Long labSessionId);
}
