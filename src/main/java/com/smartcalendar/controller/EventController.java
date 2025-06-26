package com.smartcalendar.controller;

import com.smartcalendar.model.Event;
import com.smartcalendar.repository.EventRepository;
import com.smartcalendar.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getEvents(@RequestParam(name = "location") String location,
                                                 @RequestParam(name = "userId") Long userId) {
        if (location == null || location.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<Event> events = eventService.getPersonalizedEvents(location.trim(), userId);
        return ResponseEntity.ok(events);
    }
}