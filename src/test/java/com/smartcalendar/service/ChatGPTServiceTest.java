package com.smartcalendar.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatGPTServiceTest {

    @InjectMocks
    private ChatGPTService chatGPTService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvertToEntities() {
        Map<String, List<?>> data = Map.of(
                "events", List.of(Map.of("title", "Event1")),
                "tasks", List.of(Map.of("title", "Task1", "completed", false))
        );
        var entities = chatGPTService.convertToEntities(data);
        assertEquals(2, entities.size());
        assertTrue(entities.stream().anyMatch(e -> e.getClass().getSimpleName().equals("Event")));
        assertTrue(entities.stream().anyMatch(e -> e.getClass().getSimpleName().equals("Task")));
    }

    @Test
    void testProcessTranscript_Error() {
        ChatGPTService spyService = spy(chatGPTService);
        doReturn("{\"error\": \"Unrelated request\"}").when(spyService).askChatGPT(anyString(), anyString());
        Map<String, Object> result = spyService.processTranscript("some unrelated text");
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testGenerateEventsAndTasks_ValidJson() {
        ChatGPTService spyService = spy(chatGPTService);
        doReturn("{\"events\":[],\"tasks\":[]}").when(spyService).askChatGPT(anyString(), anyString());
        Map<String, List<?>> result = spyService.generateEventsAndTasks("test");
        assertTrue(result.containsKey("events"));
        assertTrue(result.containsKey("tasks"));
    }
}