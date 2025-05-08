package com.smartcalendar.controller;

import com.smartcalendar.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatgpt")
@RequiredArgsConstructor
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    @PostMapping("/ask")
    public ResponseEntity<String> askChatGPT(@RequestBody Map<String, String> requestBody) {
        String question = requestBody.get("question");
        String model = requestBody.getOrDefault("model", "gpt-3.5-turbo");
        String response = chatGPTService.askChatGPT(question, model);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, List<?>>> generateEventsAndTasks(@RequestBody Map<String, String> requestBody) {
        String userQuery = requestBody.get("query");
        Map<String, List<?>> result = chatGPTService.generateEventsAndTasks(userQuery);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate/entities")
    public ResponseEntity<?> generateEntities(@RequestBody Map<String, String> requestBody) {
        String userQuery = requestBody.get("query");

        try {
            Map<String, Object> response = chatGPTService.processTranscript(userQuery);

            if (response.containsKey("error")) {
                return ResponseEntity.badRequest().body(response);
            }

            if (!(response.get("events") instanceof List) || !(response.get("tasks") instanceof List)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid response format from ChatGPT"));
            }

            Map<String, List<?>> validResponse = Map.of(
                    "events", (List<?>) response.get("events"),
                    "tasks", (List<?>) response.get("tasks")
            );

            List<Object> entities = chatGPTService.convertToEntities(validResponse);
            return ResponseEntity.ok(entities);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}