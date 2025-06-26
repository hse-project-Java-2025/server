package com.smartcalendar.repository;

import com.smartcalendar.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByOrganizerId(Long organizerId);
    @Query("SELECT e FROM Event e WHERE LOWER(e.location) = LOWER(:location) ORDER BY e.end ASC")
    List<Event> findByLocationIgnoreCase(String location);
    @Query("SELECT e FROM Event e WHERE LOWER(e.location) = LOWER(:location) AND :userId IS NOT NULL " +
            "AND (e NOT IN (SELECT v FROM User u JOIN u.events v WHERE u.id = :userId))")
    List<Event> findByLocationForUser(String location, Long userId);
}