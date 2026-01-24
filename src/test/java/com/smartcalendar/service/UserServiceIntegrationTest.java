package com.smartcalendar.service;

import com.smartcalendar.SmartCalendarApplication;
import com.smartcalendar.config.TestSecurityConfig;
import com.smartcalendar.model.Event;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.EventRepository;
import com.smartcalendar.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = {SmartCalendarApplication.class, TestSecurityConfig.class}
)
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User testUser;
    private User partisipantUser;
    private final Event eventToday = new Event();
    private final Event eventTodayEvening = new Event();
    private final Event eventTomorrow = new Event();
    private final LocalDate date = LocalDate.now();
    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("12345");
        userRepository.save(testUser);
        partisipantUser = new User();
        partisipantUser.setId(2L);
        partisipantUser.setUsername("invited");
        partisipantUser.setEmail("invited@example.com");
        partisipantUser.setPassword("12345");
        userRepository.save(partisipantUser);

        eventToday.setTitle("Morning Meeting");
        eventToday.setId(UUID.randomUUID());
        eventToday.setStart(date.atTime(9, 0));
        eventToday.setEnd(date.atTime(10, 0));
        eventToday.setOrganizer(testUser);

        eventTodayEvening.setId(UUID.randomUUID());
        eventTodayEvening.setTitle("Workshop");
        eventTodayEvening.setStart(date.atTime(15, 0));
        eventTodayEvening.setEnd(date.atTime(17, 0));
        eventTodayEvening.setOrganizer(testUser);

        eventTomorrow.setTitle("Workshop");
        eventTomorrow.setId(UUID.randomUUID());
        eventTomorrow.setStart(date.plusDays(1).atTime(15, 0));
        eventTomorrow.setEnd(date.plusDays(1).atTime(17, 0));
        eventTomorrow.setOrganizer(testUser);
    }

    @Transactional
    @Test
    void testSaveAndFindEventsByUserIdAndDate() {
        userService.saveEvent(eventToday);
        userService.saveEvent(eventTodayEvening);
        userService.saveEvent(eventTomorrow);

        List<Event> events = userService.findEventsByUserIdAndDate(testUser.getId(), date);

        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e.getTitle().equals("Morning Meeting")));
        assertTrue(events.stream().anyMatch(e -> e.getTitle().equals("Workshop")));
        List<Event> eventsNull = userService.findEventsByUserIdAndDate(testUser.getId(), date.minusDays(1));
        assertTrue(eventsNull.isEmpty());
    }

    @Transactional
    @Test
    void testFindEventsByUserIdAndDate_Participants() {
        userService.saveEvent(eventToday);

        eventToday.getParticipants().add(partisipantUser);
        userService.saveEvent(eventToday);

        List<Event> events = userService.findEventsByUserIdAndDate(partisipantUser.getId(), date);
        assertTrue(events.stream().anyMatch(e -> e.getTitle().equals("Morning Meeting")));
    }
}