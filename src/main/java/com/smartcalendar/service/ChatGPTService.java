package com.smartcalendar.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcalendar.model.Event;
import com.smartcalendar.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatGPTService {

    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);
    private final ObjectMapper objectMapper;

    @Value("${chatgpt.api.url}")
    private String apiUrl;

    @Value("${chatgpt.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder().build();

    public ChatGPTService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public String askChatGPT(String question, String model) {
        logger.info("Sending request to ChatGPT API with question: {}", question);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", question)
                ),
                "temperature", 0.7,
                "max_tokens", 300
        );

        try {
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode rootNode = objectMapper.readTree(response);
            String content = rootNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            logger.info("Extracted content: {}", content);
            return content;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.error("Invalid API key: {}", e.getResponseBodyAsString());
            } else {
                logger.error("Error from ChatGPT API: {}", e.getResponseBodyAsString());
            }
            throw new RuntimeException("Failed to get response from ChatGPT: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while communicating with ChatGPT API", e);
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    public Map<String, List<?>> generateEventsAndTasks(String userQuery) {
        logger.info("Generating events and tasks for query: {}", userQuery);

        String prompt = "Based on the user's query: \"" + userQuery + "\", generate a list of events and tasks. " +
                "Respond in JSON format with the following structure: " +
                "{ \"events\": [{ \"title\": \"string\", \"start\": \"ISO 8601 datetime\", \"end\": \"ISO 8601 datetime\", \"location\": \"string\" }], " +
                "\"tasks\": [{ \"title\": \"string\", \"description\": \"string\", \"completed\": false }] }";

        String response = askChatGPT(prompt, "gpt-3.5-turbo");

        try {
            return objectMapper.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            logger.error("Error parsing ChatGPT response into events and tasks", e);
            throw new RuntimeException("Failed to parse ChatGPT response: " + e.getMessage());
        }
    }

    public List<Object> convertToEntities(Map<String, List<?>> data) {
        logger.info("Converting data to entities: {}", data);

        List<Object> entities = new ArrayList<>();

        List<Map<String, Object>> events = (List<Map<String, Object>>) data.get("events");
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) data.get("tasks");

        if (events != null) {
            for (Map<String, Object> eventData : events) {
                Event event = objectMapper.convertValue(eventData, Event.class);
                entities.add(event);
            }
        }

        if (tasks != null) {
            for (Map<String, Object> taskData : tasks) {
                Task task = objectMapper.convertValue(taskData, Task.class);
                entities.add(task);
            }
        }

        return entities;
    }

    public Map<String, Object> processTranscript(String transcript) {
        String prompt = "Based on the following transcript: \"" + transcript + "\", determine if it is related to creating events or tasks. " +
                "If it is, generate a list of events and tasks strictly in JSON format with the following structure: " +
                "{ \"events\": [{ \"title\": \"string\", \"start\": \"ISO 8601 datetime\", \"end\": \"ISO 8601 datetime\", \"location\": \"string\", \"description\": \"string\", \"type\": \"string\" }], " +
                "\"tasks\": [{ \"title\": \"string\", \"description\": \"string\", \"completed\": false, \"dueDate\": \"ISO 8601 date\" }] }. " +
                "If the transcript is not related to events or tasks, respond with: { \"error\": \"Unrelated request\" }. " +
                "Do not include any additional text or explanation.";

        String response = askChatGPT(prompt, "gpt-3.5-turbo");

        try {
            Map<String, Object> result = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            if (result.containsKey("error")) {
                logger.warn("ChatGPT returned an error: {}", result);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process ChatGPT response: " + e.getMessage());
        }
    }
}