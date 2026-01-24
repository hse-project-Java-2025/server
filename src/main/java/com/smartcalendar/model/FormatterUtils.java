package com.smartcalendar.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

@Component
public class FormatterUtils {
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(FormatterUtils.class);

    public FormatterUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String convertEventsToJson(List<Event> events) {
        try {
            List<Map<String, String>> simplifiedEvents = events.stream()
                    .map(e -> Map.ofEntries(
                            entry("title", Objects.toString(e.getTitle(), "")),
                            entry("description", Objects.toString(e.getDescription(), "")),
                            entry("start", e.getStart() != null ? e.getStart().toString() : ""),
                            entry("end", e.getEnd() != null ? e.getEnd().toString() : ""),
                            entry("location", Objects.toString(e.getLocation(), "")),
                            entry("type", e.getType() != null ? e.getType().toString() : "COMMON")
                    ))
                    .toList();
            return objectMapper.writeValueAsString(simplifiedEvents);
        } catch (Exception ex) {
            logger.error("Failed to convert events to JSON", ex);
            return "[]";
        }
    }
    public String convertDayIntervalsToJson(LocalDate date, List<Event.Interval> intervals){
        try {
            List<Map<String, String>> simplifiedIntervals = intervals.stream()
                    .map(e -> Map.ofEntries(
                            entry("date", date.toString()),
                            entry("start", Objects.toString(e.start, "")),
                            entry("end", Objects.toString(e.end, ""))
                    ))
                    .toList();
            return objectMapper.writeValueAsString(simplifiedIntervals);
        } catch (Exception ex) {
            logger.error("Failed to convert events to JSON", ex);
            return "[]";
        }
    }
}
