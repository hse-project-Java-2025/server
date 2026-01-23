package com.smartcalendar.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

@Tag("openAI-api")
@Nested
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-real")
class ServiceRealApiTest {

    @Autowired
    private AudioProcessingService audioProcessingService;

    @Autowired
    private ChatGPTService chatGPTService;

    MockMultipartFile convertFileToMultipart(String filename) throws IOException {
        File audio = new File("src/test/resources/"+filename);
        byte[] content = Files.readAllBytes(audio.toPath());

        return new MockMultipartFile(
                "file",
                audio.getName(),
                "audio/mpeg",
                content
        );
    }

    @Test
    @Disabled("call real OpenAI API")
    void testRealAudioTranscription() {
        MockMultipartFile multipartFile = assertDoesNotThrow(() -> convertFileToMultipart("DescriptionIneedStudy.mp3"));

        String result = audioProcessingService.transcribeAudio(multipartFile);
        assertNotNull(result);
        assertTrue(result.contains("need") && result.contains("description"));
    }

    @Test
    @Disabled("call real OpenAI API")
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
}