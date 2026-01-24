package com.smartcalendar.service;

import com.smartcalendar.model.Event;
import com.smartcalendar.model.EventType;
import com.smartcalendar.model.FormatterUtils;
import com.smartcalendar.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatGPTServiceTest {

    private Event createEvent(String title, LocalDateTime start, LocalDateTime end, EventType type, String description) {
        Event e = new Event();
        e.setId(UUID.randomUUID());
        e.setTitle(title);
        e.setStart(start);
        e.setEnd(end);
        e.setType(type);
        e.setDescription(description);
        return e;
    }
    @Mock
    private UserService userService;
    @Mock
    private EventDistributorService eventDistributorService;
    @Mock
    private FormatterUtils formatterUtils;

    @InjectMocks
    private ChatGPTService chatGPTService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvertToEntities() {
        Map<String, List<?>> data = Map.of(
                "events", List.of(Map.of("title", "Event1"))
        );
        var entities = chatGPTService.convertToEntities(data);
        assertEquals(1, entities.size());
        assertTrue(entities.stream().anyMatch(e -> e.getClass().getSimpleName().equals("Event")));
    }

    @Test
    void testProcessTranscript_Error() {
        ChatGPTService spyService = spy(chatGPTService);
        doReturn("{\"error\": \"Unrelated request\"}").when(spyService).askChatGPT(anyString(), anyString());
        Map<String, Object> result = spyService.processTranscript("some unrelated text");
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testGenerateEvents_ValidJson() {
        ChatGPTService spyService = spy(chatGPTService);
        doReturn("{\"events\":[],\"tasks\":[]}").when(spyService).askChatGPT(anyString(), anyString());
        Map<String, List<?>> result = spyService.generateEvents("test");
        assertTrue(result.containsKey("events"));
        assertTrue(result.containsKey("tasks"));
    }
    @Test
    void findDates(){
        ChatGPTService spyService = spy(chatGPTService);
        doReturn("[\"2025-08-22\",\"2025-08-23\"]").when(spyService).askChatGPT(anyString(), anyString());
        List<LocalDate> dates = spyService.findDates("I want to create tasks on 22 and 23 August 2025");
        assertNotNull(dates);
        assertEquals(2, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 8, 22)));
        assertTrue(dates.contains(LocalDate.of(2025, 8, 23)));
    }

    @Test
    void testGenerateSuggestions() {
        Long userId = 1L;
        String query = "Schedule study session today";
        LocalDate today = LocalDate.of(2025, 8, 25);

        ChatGPTService spyService = Mockito.spy(chatGPTService);

        doReturn("[\"2025-08-25\"]").when(spyService).askChatGPT(anyString(), anyString());

        Event.Interval slot = new Event.Interval(
                today.atTime(20, 0),
                today.atTime(23, 59)
        );
        when(eventDistributorService.getFreeSlots(anyList(), eq(today)))
                .thenReturn(List.of(slot));

        when(formatterUtils.convertDayIntervalsToJson(eq(today), anyList()))
                .thenReturn("[{\"date\":\"2025-08-25\",\"start\":\"2025-08-25T20:00\",\"end\":\"2025-08-25T23:59\"}]");

        Map<String, List<?>> chatResponse = Map.of(
                "events", List.of(
                        Map.of(
                                "title", "Study Session",
                                "description", "Scheduled study session today",
                                "start", "2025-08-25T20:00",
                                "end", "2025-08-25T23:59",
                                "location", "",
                                "type", "STUDIES"
                        )
                )
        );
        doReturn(chatResponse).when(spyService).generateEventsWithTaskInfo(anyString(), anyString());

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@test.com");

        Event placedEvent = new Event();
        placedEvent.setId(UUID.randomUUID());
        placedEvent.setTitle("Study Session");
        placedEvent.setDescription("Scheduled study session today");
        placedEvent.setStart(today.atTime(20, 0));
        placedEvent.setEnd(today.atTime(23, 59));
        placedEvent.setType(EventType.STUDIES);

        when(eventDistributorService.placeEvents(
                anyList(),
                anyList(),
                isNull(),
                eq(today)
        )).thenReturn(List.of(placedEvent));

        List<Event> result = spyService.generateSuggestions(userId, query);

        assertNotNull(result);
        assertEquals(1, result.size());
        Event e = result.get(0);
        assertEquals("Study Session", e.getTitle());
        assertEquals(EventType.STUDIES, e.getType());
        assertEquals(today.atTime(20, 0), e.getStart());
        assertEquals(today.atTime(23, 59), e.getEnd());

        verify(eventDistributorService).placeEvents(anyList(),
                anyList(),
                isNull(), eq(today));
    }
}