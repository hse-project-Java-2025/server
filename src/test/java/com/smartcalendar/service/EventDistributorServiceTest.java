package com.smartcalendar.service;

import com.smartcalendar.model.Event;
import com.smartcalendar.model.EventType;
import com.smartcalendar.model.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventDistributorServiceTest {
    private final EventDistributorService service = new EventDistributorService();

    private User organizer = new User();
    private Event createEvent(String title, LocalDateTime start, LocalDateTime end, EventType type) {
        Event e = new Event();
        e.setId(UUID.randomUUID());
        e.setTitle(title);
        e.setStart(start);
        e.setEnd(end);
        e.setType(type);
        e.setOrganizer(organizer);
        return e;
    }
    LocalDate date = LocalDate.now();
    Event firstEvent = createEvent("Math", date.atTime(9, 0), date.atTime(10, 0), EventType.STUDIES);
    Event secondEvent = createEvent("Pool", date.atTime(11, 30), date.atTime(13, 0), EventType.FITNESS);

    @Test
    void testGetFreeSlots() {

        List<Event.Interval> freeSlotsNoEvents = service.getFreeSlots(List.of(), date);
        LocalDateTime dayBegin = service.getDayBegin(date);
        assertEquals(1, freeSlotsNoEvents.size());
        assertEquals(dayBegin, freeSlotsNoEvents.getFirst().start);
        assertEquals(date.plusDays(1).atStartOfDay(), freeSlotsNoEvents.getFirst().end);

        List<Event.Interval> freeSlots = service.getFreeSlots(List.of(secondEvent, firstEvent), date);
        assertEquals(3, freeSlots.size());
        assertEquals(firstEvent.getStart(), freeSlots.getFirst().end);
        assertEquals(secondEvent.getEnd(), freeSlots.getLast().start);
    }

    @Test
    void testPlaceEvents() {
        List<Event.Interval> slots = List.of(
                new Event.Interval(date.atTime(8, 0), date.atTime(12, 0)),
                new Event.Interval(date.atTime(13, 0), date.atTime(15, 0))
        );

        Event thirdEvent = createEvent("Gym", date.atTime(14, 30), date.atTime(15, 0), EventType.FITNESS);

        //first ok, second in interval, third ok
        List<Event> placed = service.placeEvents(List.of(firstEvent, secondEvent, thirdEvent), slots, organizer, date);

        placed.getFirst().setId(firstEvent.getId());//id and creation time will changed
        placed.getFirst().setCreationTime(firstEvent.getCreationTime());
        placed.getLast().setId(thirdEvent.getId());
        placed.getLast().setCreationTime(thirdEvent.getCreationTime());

        assertEquals(3, placed.size());
        assertEquals(firstEvent, placed.getFirst());
        assertEquals(slots.getLast().start.plus(service.defaultDuration(secondEvent.getType())),
                placed.get(1).getEnd());
        assertEquals(thirdEvent, placed.get(2));
    }
}
