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
    public ResponseEntity<List<Object>> generateEntities(@RequestBody Map<String, String> requestBody) {
        String userQuery = requestBody.get("query");
        Map<String, List<?>> data = chatGPTService.generateEventsAndTasks(userQuery);
        List<Object> entities = chatGPTService.convertToEntities(data);
        return ResponseEntity.ok(entities);
    }
}