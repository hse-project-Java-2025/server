package com.smartcalendar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AudioProcessingService {

    @Value("${whisper.api.url}")
    private String whisperApiUrl;
    @Value("${gpt4o-mini-transcribe.api.url}")
    private String transcribeApiUrl;
    @Value("${chatgpt.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private void convertToWav(Path input, Path output) throws IOException, InterruptedException {
        String[] command = {
                "ffmpeg", "-y",
                "-i", input.toString(),
                "-ar", "16000",
                "-ac", "1",
                "-c:a", "pcm_s16le",
                output.toString()
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("ffmpeg failed to convert audio, exit code: " + exitCode);
        }

        log.info("Converted file with ffmpeg: {}", output.toAbsolutePath());
    }
    public String transcribeAudio(MultipartFile file) {
        try {
            Path uploadsDir = Paths.get(System.getProperty("user.dir"), "tmp", "uploads").toAbsolutePath();
            Files.createDirectories(uploadsDir);

            Path path = uploadsDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            file.transferTo(path.toFile());
            log.info("Saved uploaded file to: {}", path.toAbsolutePath());

            //Path fixedPath = uploadsDir.resolve("fixed.wav");
            //convertToWav(path, fixedPath);

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new FileSystemResource(path));
            builder.part("model", "whisper-1");

            String response = webClient.post()
                    .uri("https://api.openai.com/v1/audio/transcriptions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response;
        } catch (Exception e) {
            log.info("Using API key starts with: {}", apiKey.substring(0, 10));
            log.error("Transcription request failed", e);
            throw new RuntimeException("Failed to transcribe audio: " + e.getMessage());
        }
    }
}