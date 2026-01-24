package com.smartcalendar.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcalendar.model.Event;
import com.smartcalendar.model.EventType;
import com.smartcalendar.model.FormatterUtils;
import com.smartcalendar.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatGPTService {
    @Autowired
    private EventDistributorService eventDistributorService;
    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);
    private final ObjectMapper objectMapper;

    @Autowired
    private FormatterUtils formatterUtils;

    private final UserService userService;

    @Value("${chatgpt.api.url}")
    private String apiUrl;

    @Value("${chatgpt.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder().build();

    public ChatGPTService(UserService userService) {
        this.userService = userService;
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

    public List<LocalDate> findDates(String userQuery) {
        logger.info("Find dates for query: {}", userQuery);

        String prompt = "Today is " + LocalDate.now()+
                " Based on the user's query: \"" + userQuery + "\"," +
                " create a JSON array of ISO 8601 dates (YYYY-MM-DD) representing the days the user refers to or intends. " +
                "Respond STRICTLY with a JSON array, e.g. [\"2025-08-22\",\"2025-08-23\"]. " +
                "If there are no dates, return JSON array of today date . Do not include any extra text.";

        String response = askChatGPT(prompt, "gpt-3.5-turbo");
        if (response == null) {
            logger.warn("ChatGPT returned null response for findDates");
            return List.of();
        }

        List<LocalDate> result = new ArrayList<>();
        try {
            List<String> dateStrings = objectMapper.readValue(response, new TypeReference<>() {
            });
            for (String s : dateStrings) {
                if (s == null) continue;
                try {
                    LocalDate d = LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                    result.add(d);
                } catch (DateTimeParseException ex) {
                    logger.warn("Skipping unparsable date string from JSON array: '{}'", s);
                }
            }
            return result.stream().distinct().toList();
        } catch (Exception e) {
            logger.info("Failed extract dates from query. Response: {}", response);
        }
        return List.of();
    }
    public List<Event> generateSuggestions(Long userId, String query) {
        List<LocalDate> datesList = findDates(query);
        Map<LocalDate, List<Event.Interval>> intervalsByDate = new HashMap<>();
        StringBuilder intervalsInfo = new StringBuilder();
        for (LocalDate date: datesList){
            List<Event.Interval> dateIntervals = eventDistributorService.getFreeSlots(userService.findEventsByUserIdAndDate(userId, date), date);
            intervalsInfo.append(formatterUtils.convertDayIntervalsToJson(date, dateIntervals));
            intervalsByDate.put(date, dateIntervals);
        }

        Map<String, List<?>> chatResponse = generateEventsWithTaskInfo(query, intervalsInfo.toString());
        Map<LocalDate, List<Event>> generatedByDate = convertToEntities(chatResponse)
                .stream()
                .filter(e -> e instanceof Event)
                .map(e -> (Event) e)
                .collect(Collectors.groupingBy(e -> e.getStart().toLocalDate()));


        List<Event> finalPlaced = new ArrayList<>();
        User organizer = userService.findUserById(userId);

        for (LocalDate date : datesList) {
            List<Event.Interval> freeSlots = intervalsByDate.getOrDefault(date, List.of());
            List<Event> dayGenerated = generatedByDate.getOrDefault(date, List.of());

            finalPlaced.addAll(eventDistributorService.placeEvents(dayGenerated, freeSlots, organizer, date));
        }
        return finalPlaced;
    }

    public Map<String, List<?>> generateEventsWithTaskInfo(String userQuery, String intervalsInfo) {
        logger.info("Generating events for query: {}", userQuery);

        String prompt = "Based on the user's query and user's free intervals in that days  generate a list of events. "+
                "USER_QUERY:\n"+ userQuery+ "\n " +
                "USER FREE INTERVALS: \n "+ intervalsInfo + "\n " +
                "Respond strictly in JSON format with the following structure: " +
                "{ \"events\": [{ " +
                "\"title\": \"string\", " +
                "\"description\": \"string\", " +
                "\"start\": \"ISO 8601 datetime\", " +
                "\"end\": \"ISO 8601 datetime\", " +
                "\"location\": \"string\", " +
                "\"type\": \"COMMON|FITNESS|STUDIES|WORK\" " +
                "}], " +
                "If the query does not require adding an event, return an empty list."+
                "If the user mentions a note, description, or additional information related to an event, include it in the 'description' field of the corresponding event, " +
                "unless it is clearly a separate event. " +
                "Do not include any additional text or explanation. The \"type\" field MUST be exactly one of: \"COMMON\", \"FITNESS\", \"STUDIES\", \"WORK\". \n" +
                "If the event is about sports or training, always use \"FITNESS\". \n" +
                "Do not invent other values (e.g., \"SPORT\")."+
                "Do not repeat or copy any of the provided user events. +\n" +
                "Generate only new events that are required based on the user's query. "+
                "When generating new events, do not duplicate or overlap with existing events in USERS_EVENTS.";

        String response = askChatGPT(prompt, "gpt-3.5-turbo");

        try {
            return objectMapper.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            logger.error("Error parsing ChatGPT response into events", e);
            throw new RuntimeException("Failed to parse ChatGPT response: " + e.getMessage());
        }
    }

    public Map<String, List<?>> generateEvents(String userQuery) {
        logger.info("Generating events for query: {}", userQuery);
        String prompt = "Based on the user's query: \"" + userQuery + "\", generate a list of events . " +
                "If the user mentions a note, description, or additional information related to an event, include it in the 'description' field of the corresponding event, " +
                "unless it is clearly a separate task. " +
                "Respond strictly in JSON format with the following structure: " +
                "{ \"events\": [{ " +
                "\"title\": \"string\", " +
                "\"description\": \"string\", " +
                "\"start\": \"ISO 8601 datetime\", " +
                "\"end\": \"ISO 8601 datetime\", " +
                "\"location\": \"string\", " +
                "\"type\": \"COMMON|FITNESS|STUDIES|WORK\" " +
                "}], " +
                "Do not include any additional text or explanation. The \"type\" field MUST be exactly one of: \"COMMON\", \"FITNESS\", \"STUDIES\", \"WORK\". \n" +
                "If the event is about sports or training, always use \"FITNESS\". \n" +
                "Do not invent other values (e.g., \"SPORT\").";

        String response = askChatGPT(prompt, "gpt-3.5-turbo");

        try {
            return objectMapper.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            logger.error("Error parsing ChatGPT response into events", e);
            throw new RuntimeException("Failed to parse ChatGPT response: " + e.getMessage());
        }
    }

    public List<Object> convertToEntities(Map<String, List<?>> data) {
        logger.info("Converting data to entities: {}", data);

        List<Object> entities = new ArrayList<>();

        List<Map<String, Object>> events = (List<Map<String, Object>>) data.get("events");

        if (events != null) {
            for (Map<String, Object> eventData : events) {
                Event event = objectMapper.convertValue(eventData, Event.class);
                event.setType(EventType.COMMON);

                if (eventData.get("type")!=null && Arrays.stream(EventType.values()).anyMatch(x->Objects.equals(x.toString(),eventData.get("type").toString()))) {
                    try {
                        event.setType(EventType.valueOf(eventData.get("type").toString()));
                    } catch (Exception ignored) {}
                }

                if (event.getId() == null) {
                    event.setId(UUID.randomUUID());
                }
                if (event.getCreationTime() == null) {
                    event.setCreationTime(LocalDateTime.now());
                }
                if (!event.isCompleted() && eventData.get("completed") != null) {
                    event.setCompleted(Boolean.parseBoolean(eventData.get("completed").toString()));
                }

                event.setShared(false);
                event.setInvitees(new ArrayList<>());
                event.setParticipants(new ArrayList<>());
                entities.add(event);
            }
        }


        return entities;
    }

    public Map<String, Object> processTranscript(String transcript) {
        String today = LocalDate.now().toString();
        String prompt = "Today is " + today + ". Based on the following transcript: \"" + transcript + "\", determine if it is related to creating events. " +
                "If it is, generate a list of events strictly in JSON format with the following structure: " +
                "{ \"events\": [{ " +
                "\"title\": \"string\", " +
                "\"description\": \"string\", " +
                "\"start\": \"ISO 8601 datetime\", " +
                "\"end\": \"ISO 8601 datetime\", " +
                "\"location\": \"string\", " +
                "\"type\": \"COMMON|FITNESS|STUDIES|WORK\" " +
                "}], " +
                "If the transcript contains a note, description, or additional information about an event, include it in the 'description' field of the event, " +
                "If the transcript is not related to events, respond with: { \"error\": \"Unrelated request\" }. " +
                "Treat any mention of a 'task' as an event. " +
                "Do not include any additional text or explanation.";

        String response = askChatGPT(prompt, "gpt-3.5-turbo");

        try {
            Map<String, Object> result = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            if (result.containsKey("error")) {
                logger.warn("ChatGPT returned an error: {}", result);
            }
            if (!result.containsKey("events")) {
                result.put("events", List.of());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process ChatGPT response: " + e.getMessage());
        }
    }
}
