package com.smartcalendar.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
@ActiveProfiles("h2")
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
}