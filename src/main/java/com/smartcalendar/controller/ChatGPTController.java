package com.smartcalendar.controller;

import com.smartcalendar.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}