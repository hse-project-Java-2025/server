package com.smartcalendar.repository;

import com.smartcalendar.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByOrganizerId(Long organizerId);
    @Query("SELECT e FROM Event e " +
            "LEFT JOIN e.participants p " +
            "WHERE (e.organizer.id = :userId OR p.id = :userId) " +
            "AND e.start BETWEEN :startOfDay AND :endOfDay")
    List<Event> findByUserIdAndDate(Long userId,
                                                 LocalDateTime startOfDay,
                                                 LocalDateTime endOfDay);
    @Query("SELECT e FROM Event e WHERE LOWER(e.location) = LOWER(:location) ORDER BY e.end ASC")
    List<Event> findByLocationIgnoreCase(String location);
    @Query("SELECT e FROM Event e WHERE LOWER(e.location) = LOWER(:location) AND :userId IS NOT NULL " +
            "AND (e NOT IN (SELECT v FROM User u JOIN u.events v WHERE u.id = :userId))")
    List<Event> findByLocationForUser(String location, Long userId);
}