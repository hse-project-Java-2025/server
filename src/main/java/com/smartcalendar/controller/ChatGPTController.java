package com.smartcalendar.controller;

import com.smartcalendar.model.Event;
import com.smartcalendar.service.AudioProcessingService;
import com.smartcalendar.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatgpt")
@RequiredArgsConstructor
public class ChatGPTController {

    private final ChatGPTService chatGPTService;
    private final AudioProcessingService audioProcessingService;

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
        Map<String, List<?>> result = chatGPTService.generateEvents(userQuery);
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

            List<?> events = response.get("events") instanceof List ? (List<?>) response.get("events") : List.of();
            List<?> tasks = response.get("tasks") instanceof List ? (List<?>) response.get("tasks") : List.of();

            Map<String, List<?>> validResponse = Map.of(
                    "events", events,
                    "tasks", tasks
            );

            List<Object> entities = chatGPTService.convertToEntities(validResponse);
            return ResponseEntity.ok(entities);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("{userId}/generate/suggestions")
    public ResponseEntity<Map<String, ?>> generateSuggestions(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        try {
            String query = body.get("query");

            List<Event> result = chatGPTService.generateSuggestions(userId, query);
            return ResponseEntity.ok(Map.of("events", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("{userId}/generate/suggestions/audio")
    public ResponseEntity<Map<String, ?>> generateSuggestionsFromAudio(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile audioFile) {
        String query;
        try {
            query = audioProcessingService.transcribeAudio(audioFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to transcribe: "+e.getMessage()));
        }
        try{
            List<Event> result = chatGPTService.generateSuggestions(userId, query);

            return ResponseEntity.ok(Map.of(
                    "query", query,
                    "events", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}