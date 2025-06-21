package com.smartcalendar.service;

import com.smartcalendar.model.Event;
import com.smartcalendar.model.Tag;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.EventRepository;
import com.smartcalendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public EventService(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public List<Event> getPersonalizedEvents(String location, Long userId) {
        List<Event> events = new ArrayList<>();
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                events = calculateRelevance(location, user);
            }
        }
        if (events.isEmpty()) {
            events = eventRepository.findByLocationIgnoreCase(location);
        }
        return filterEventsByTime(events);
    }

    private List<Event> filterEventsByTime(List<Event> events) {
        return events.stream().filter(event -> event.getEnd().isAfter(LocalDateTime.now())).toList();
    }

    private List<Event> calculateRelevance(String location, User user) {
        List<Event> locationEvents = eventRepository.findByLocationForUser(location, user.getId());
        Map<Long, Integer> tagFrequency = new HashMap<>();
        user.getVisitedEvents().forEach(event -> event.getTags().forEach(tag ->
                tagFrequency.put(tag.getId(), tagFrequency.getOrDefault(tag.getId(), 0) + 1)));
        Map<Event, Integer> relevanceMap = new HashMap<>();
        for (Event event : locationEvents) {
            int score = 0;
            for (Long tagId : event.getTags().stream().map(Tag::getId).toList()) {
                score += tagFrequency.getOrDefault(tagId, 0);
            }
            relevanceMap.put(event, score);
        }
        return locationEvents.stream().sorted((lhs, rhs) -> {
                    int relevanceCompare = Integer.compare(relevanceMap.get(rhs), relevanceMap.get(lhs));
                    if (relevanceCompare != 0) return relevanceCompare;
                    return lhs.getStart().compareTo(rhs.getStart());
        }).collect(Collectors.toList());
    }
}
