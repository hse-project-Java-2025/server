package com.smartcalendar.dto;

import com.smartcalendar.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DailyTaskDto {
    private UUID id;
    private String title;
    private boolean isComplete;
    private EventType type;
    private LocalDateTime creationTime;
    private String description;
    private LocalTime start;
    private LocalTime end;
    private LocalDate date;
}