package com.smartcalendar.controller;

import com.smartcalendar.SmartCalendarApplication;
import com.smartcalendar.config.TestSecurityConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("openAI-api")
//@Disabled("call real OpenAI API")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {SmartCalendarApplication.class, TestSecurityConfig.class} // подмешиваем TestSecurityConfig
)
@ActiveProfiles("test-real")
public class AudioControllerRestTemplateTest {

    @Autowired
    private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

    ResponseEntity testAudioSend(String filename) throws Exception {
        ClassPathResource audioFile = new ClassPathResource(filename);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.FileSystemResource(audioFile.getFile()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity response = restTemplate.postForEntity(
                "/api/audio/process", requestEntity, Object.class
        );
        return response;
    }
    void testSuccessfulAudioSend(String filename, Predicate<Map<String, Object>> checker) throws Exception {
        ResponseEntity response = testAudioSend(filename);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        ResponseEntity<List> responseList = (ResponseEntity<List>) response;
        Map<String, Object> task = (Map<String, Object>) responseList.getBody().get(0);
        assertTrue(checker.test(task));
    }
    void testUnrelatedAudio(String filename) throws Exception{
        ResponseEntity response = testAudioSend(filename);
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.containsKey("error"));
    }
    @Test
    void testStudyEvent(){
        Predicate<Map<String, Object>> descriptionStudy = task ->(task.get("description").toString()).contains("I need to study");
        assertDoesNotThrow(()->testSuccessfulAudioSend("DescriptionIneedStudy.mp3", descriptionStudy));
    }
    @Test
    void testSwimmingEvent(){
        Predicate<Map<String, Object>> type = task ->(task.get("type").toString().contains("FITNESS") || task.get("type").toString().contains("COMMON"));
        //если CHAT GPT выставляет некорректный(SPORT например) COMMON выставляется
        Predicate<Map<String, Object>> time12to14=task->task.get("start").toString().contains("12:00") && task.get("end").toString().contains("14:00");
        Predicate<Map<String, Object>> predicate = task->type.test(task) && time12to14.test(task);
        assertDoesNotThrow(()->testSuccessfulAudioSend("Swimming12-14Sport.mp3", predicate));
    }
    @Test
    void testEmptyAudio(){
        assertDoesNotThrow(()->testUnrelatedAudio("empty.mp3"));
    }
}