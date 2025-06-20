package com.smartcalendar.controller;

import com.smartcalendar.service.ChatGPTService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "JWT_SECRET=test_jwt_secret",
        "chatgpt.api.url=http://dummy-url",
        "chatgpt.api.key=dummy-key",
        "spring.security.enabled=false"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
class ChatGPTControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatGPTService chatGPTService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ChatGPTService chatGPTService() {
            return Mockito.mock(ChatGPTService.class);
        }
    }

    @Test
    @WithMockUser
    void testAskChatGPT() throws Exception {
        Mockito.when(chatGPTService.askChatGPT(any(), any())).thenReturn("response");
        mockMvc.perform(post("/api/chatgpt/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"test?\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("response"));
    }

    @Test
    @WithMockUser
    void testGenerateEventsAndTasks() throws Exception {
        Mockito.when(chatGPTService.generateEventsAndTasks(any())).thenReturn(
                Map.of("events", List.of(), "tasks", List.of())
        );
        mockMvc.perform(post("/api/chatgpt/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").exists())
                .andExpect(jsonPath("$.tasks").exists());
    }

    @Test
    @WithMockUser
    void testGenerateEntities_Error() throws Exception {
        Mockito.when(chatGPTService.processTranscript(any())).thenReturn(Map.of("error", "Unrelated request"));
        mockMvc.perform(post("/api/chatgpt/generate/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}