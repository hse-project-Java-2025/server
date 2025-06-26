package com.smartcalendar.controller;

import com.smartcalendar.service.AudioProcessingService;
import com.smartcalendar.service.ChatGPTService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "JWT_SECRET=test_jwt_secret",
        "chatgpt.api.url=http://dummy-url",
        "chatgpt.api.key=dummy-key",
        "spring.security.enabled=false"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
class AudioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AudioProcessingService audioProcessingService;

    @Autowired
    private ChatGPTService chatGPTService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public AudioProcessingService audioProcessingService() {
            return Mockito.mock(AudioProcessingService.class);
        }
        @Bean
        public ChatGPTService chatGPTService() {
            return Mockito.mock(ChatGPTService.class);
        }
    }

    @Test
    @WithMockUser
    void testProcessAudio_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "audio.wav", "audio/wav", "dummy".getBytes());

        Mockito.when(audioProcessingService.transcribeAudio(any())).thenReturn("test transcript");
        Mockito.when(chatGPTService.processTranscript(any())).thenReturn(
                Map.of("events", List.of(Map.of("title", "Event1")), "tasks", List.of())
        );
        Mockito.when(chatGPTService.convertToEntities(any())).thenReturn(List.of());

        mockMvc.perform(multipart("/api/audio/process").file(file))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testProcessAudio_Error() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "audio.wav", "audio/wav", "dummy".getBytes());

        Mockito.when(audioProcessingService.transcribeAudio(any())).thenThrow(new RuntimeException("Transcribe error"));

        mockMvc.perform(multipart("/api/audio/process").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}