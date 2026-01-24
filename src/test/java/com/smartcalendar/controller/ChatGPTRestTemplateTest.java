package com.smartcalendar.controller;

import com.smartcalendar.SmartCalendarApplication;
import com.smartcalendar.config.TestSecurityConfig;
import com.smartcalendar.model.Event;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("openAI-api")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {SmartCalendarApplication.class, TestSecurityConfig.class}
)
@ActiveProfiles("test-real")
public class ChatGPTRestTemplateTest {

    @Autowired
    private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

    ResponseEntity<Map<String, Object>> sendAudioForSuggestions(Long userId, String filename) throws Exception {
        ClassPathResource audioFile = new ClassPathResource(filename);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.FileSystemResource(audioFile.getFile()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                "/api/chatgpt/" + userId + "/generate/suggestions/audio",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
    }
    @Test
    void testStudyEventFromAudio() throws Exception {
        ResponseEntity<Map<String, Object>> response = sendAudioForSuggestions(1L, "DescriptionIneedStudy.mp3");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> body = response.getBody();
        assertThat(body).containsKeys("query", "events");

        String query = body.get("query").toString();
        assertThat(query.toLowerCase()).contains("study");

        List<Map<String, Object>> events = (List<Map<String, Object>>) body.get("events");
        assertThat(events).isNotEmpty();

        Map<String, Object> event = events.get(0);
        assertThat(event.get("title").toString().toLowerCase()).contains("study");
    }
    @Test
    void testEmptyAudio() throws Exception {
        ResponseEntity<Map<String, Object>> response = sendAudioForSuggestions(1L, "empty.mp3");
        assertThat(response.getBody()).isNotNull();
        if (response.getStatusCode()==(HttpStatus.OK)){
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> events = (List<Map<String, Object>>) body.get("events");
            assertThat(events).isEmpty();
        } else {
            assertThat(response.getBody()).containsKey("error");
        }
    }
}