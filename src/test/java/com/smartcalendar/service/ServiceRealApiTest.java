package com.smartcalendar.service;

import com.smartcalendar.model.Event;
import com.smartcalendar.model.FormatterUtils;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.UserRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@Tag("openAI-api")
@Nested
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-real")
class ServiceRealApiTest {

    @Autowired
    private AudioProcessingService audioProcessingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FormatterUtils formatterUtils;

    @Autowired
    private ChatGPTService chatGPTService;

    private final User testUser = new User();
    private final Event eventToday = new Event();
    private final LocalDate todayDate = LocalDate.now();

    @BeforeEach
    void setup() {
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("12345");
        userRepository.save(testUser);
        eventToday.setTitle("Morning Meeting");
        eventToday.setId(UUID.randomUUID());
        eventToday.setStart(todayDate.atTime(0, 0));
        eventToday.setEnd(todayDate.atTime(23, 0));
        eventToday.setOrganizer(testUser);
    }

    MockMultipartFile convertFileToMultipart(String filename) throws IOException {
        File audio = new File("src/test/resources/" + filename);
        byte[] content = Files.readAllBytes(audio.toPath());

        return new MockMultipartFile(
                "file",
                audio.getName(),
                "audio/mpeg",
                content
        );
    }

    @Test
    void testRealAudioTranscription() {
        MockMultipartFile multipartFile = assertDoesNotThrow(() -> convertFileToMultipart("DescriptionIneedStudy.mp3"));

        String result = audioProcessingService.transcribeAudio(multipartFile);
        assertNotNull(result);
        assertTrue(result.contains("need") && result.contains("description"));
    }

    @Test
    void testProcessTranscript_RealRequest() {
        String transcript = "Create event type study with exactly this description: I need to study";

        Map<String, Object> result = chatGPTService.processTranscript(transcript);
        assertNotNull(result);
        assertTrue(result.containsKey("events"));

        List<Map<String, Object>> events = (List<Map<String, Object>>) result.get("events");
        assertFalse(events.isEmpty());
        Map<String, Object> firstEvent = events.getFirst();
        assertEquals("I need to study", firstEvent.get("description"));
        System.out.println("Processed events: " + result);
    }

    @Test
    void testFindDates_RealRequest() {
        String userQuery = "I want to create tasks on 22nd and 23rd August 2025";
        List<?> dates = chatGPTService.findDates(userQuery);
        assertNotNull(dates);
        assertFalse(dates.isEmpty());
        assertTrue(dates.stream().anyMatch(d -> d.toString().equals("2025-08-22")));
        assertTrue(dates.stream().anyMatch(d -> d.toString().equals("2025-08-23")));
    }

    @Test
    void testGenerateEventsWithTaskInfo_RealRequest() {
        List<Event.Interval> slots = List.of(
                new Event.Interval(todayDate.atTime(20, 0), todayDate.atTime(23, 59))
        );
        String intervalsInfo = formatterUtils.convertDayIntervalsToJson(todayDate, slots);

        String userQuery = "Schedule study session today";
        Map<String, List<?>> result = chatGPTService.generateEventsWithTaskInfo(userQuery, intervalsInfo);

        assertNotNull(result);
        assertTrue(result.containsKey("events"));

        List<?> events = result.get("events");
        assertFalse(events.isEmpty());

        Map<String, Object> firstEvent = (Map<String, Object>) events.get(0);
        String startStr = (String) firstEvent.get("start");
        assertNotNull(startStr);

        LocalDateTime startDateTime = LocalDateTime.parse(startStr);

        assertTrue(startDateTime.getHour() >= 20, "Event should start after 20:00");
        System.out.println("Generated events: " + events);
    }

    @Test
    void testGenerateSuggestions_RealRequest() {
        Long userId = testUser.getId();
        String query = "Schedule study session today";

        List<Event> result = chatGPTService.generateSuggestions(userId, query);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        Event first = result.getFirst();
        assertNotNull(first.getTitle());
        assertNotNull(first.getStart());
        assertNotNull(first.getEnd());

        System.out.println("AI generated event: " + first.getTitle() + " " + first.getStart() + " - " + first.getEnd());
    }
}