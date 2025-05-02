package com.smartcalendar.controller;

import com.smartcalendar.service.AudioProcessingService;
import com.smartcalendar.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioProcessingService audioProcessingService;
    private final ChatGPTService chatGPTService;

    @PostMapping("/process")
    public ResponseEntity<?> processAudio(@RequestParam("file") MultipartFile file) {
        try {
            String transcript = audioProcessingService.transcribeAudio(file);

            Map<String, List<?>> response = chatGPTService.processTranscript(transcript);

            if (response.containsKey("error")) {
                return ResponseEntity.badRequest().body(response);
            }

            List<Object> entities = chatGPTService.convertToEntities(response);
            return ResponseEntity.ok(entities);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}