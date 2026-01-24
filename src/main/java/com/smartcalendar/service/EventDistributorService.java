package com.smartcalendar.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.smartcalendar.model.Event;
import com.smartcalendar.model.EventType;
import com.smartcalendar.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
@Service
public class EventDistributorService {
    private final Duration dayBegin = Duration.ofHours(6);
    public LocalDateTime getDayBegin(LocalDate date){
        return date.atStartOfDay().plusHours(dayBegin.toHoursPart()).plusMinutes(dayBegin.toMinutesPart());
    }
    public Duration defaultDuration(EventType type) {
        return //switch (String.valueOf(type)) {
            switch (type) {
                case STUDIES-> Duration.ofHours(2);
                case FITNESS -> Duration.ofHours(1);
                case WORK   -> Duration.ofHours(8);
            default        -> Duration.ofHours(1);
        };
    }


    List<Event.Interval> getFreeSlots(List<Event> events, LocalDate date) {
        LocalDateTime dayStart = getDayBegin(date);
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        events = new ArrayList<>(events);
        events.sort(Comparator.comparing(Event::getStart));
        List<Event.Interval> free = new ArrayList<>();
        LocalDateTime current = dayStart;

        for (Event e : events) {
            if (e.getStart().isAfter(current)) {
                free.add(new Event.Interval(current, e.getStart()));
            }
            current = e.getEnd().isAfter(current) ? e.getEnd() : current;
        }

        if (current.isBefore(dayEnd)) {
            free.add(new Event.Interval(current, dayEnd));
        }
        return free;
    }
    /** changes Id -  don't hope Id from GPT
     * */
    List<Event> normalizeEvents(List<Event> events, LocalDate day) {
        List<Event> normalized = new ArrayList<>();
        for (Event e : events) {
            Event normalizedEvent = new Event(e);

            LocalDateTime start = e.getStart() != null ? e.getStart() : day.atStartOfDay();
            LocalDateTime end = e.getEnd() != null ? e.getEnd() : start.plus(defaultDuration(e.getType()));

            normalizedEvent.setStart(start);
            normalizedEvent.setEnd(end);

            normalized.add(normalizedEvent);
        }
        return normalized;
    }

    /** traverse in linear, if event not in free slots set default time and place in nearest later slot
    * */
    List<Event> placeEvents(List<Event> generated, List<Event.Interval> freeSlots, User organizer, LocalDate date) {
        List<Event> placed = new ArrayList<>();
        generated = new ArrayList<>(normalizeEvents(generated, date));
        generated.sort(Comparator.comparing(Event::getStart));
        List<Event.Interval> mutableSlots = new ArrayList<>(freeSlots);

        int i = 0, j = 0;
        while (i < generated.size() && j < mutableSlots.size()) {
            Event generatedEvent = generated.get(i);
            Event.Interval slot = mutableSlots.get(j);

            LocalDateTime desiredStart = generatedEvent.getStart();
            Duration duration = defaultDuration(generatedEvent.getType());
            LocalDateTime desiredEnd = generatedEvent.getEnd();
            if (desiredStart.isBefore(slot.start)) {
                desiredStart = slot.start;
                desiredEnd = desiredStart.plus(duration);
            }
            if (desiredEnd.isAfter(slot.end)) {
                j++;
                continue;
            }
            Event newEvent = new Event(generatedEvent);
            newEvent.setStart(desiredStart);
            newEvent.setEnd(desiredEnd);
            placed.add(newEvent);

            i++;
            if (desiredEnd.equals(slot.end)) {
                j++;
            } else {
                mutableSlots.set(j, new Event.Interval(desiredEnd, slot.end));
            }
        }
        LocalDateTime lastEnd = mutableSlots.isEmpty() ? LocalDateTime.now() : mutableSlots.getLast().end;
        while (i < generated.size()) {
            Event g = generated.get(i++);
            Duration duration = defaultDuration(g.getType());

            Event newEvent = new Event();
            newEvent.setId(UUID.randomUUID());
            newEvent.setTitle(g.getTitle());
            newEvent.setDescription(g.getDescription());
            newEvent.setStart(lastEnd);
            newEvent.setEnd(lastEnd.plus(duration));
            newEvent.setLocation(g.getLocation() != null ? g.getLocation() : "");
            newEvent.setType(g.getType());
            newEvent.setOrganizer(organizer);
            placed.add(newEvent);
            lastEnd = newEvent.getEnd();
        }
        return placed;
    }
}
