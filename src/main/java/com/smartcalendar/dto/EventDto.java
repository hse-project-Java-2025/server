package com.smartcalendar.dto;

import com.smartcalendar.model.EventType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EventDto {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private String location;
    private EventType type;
    private LocalDateTime creationTime;
    private UserShortDto organizer;
    private boolean completed;
    private boolean isShared;
    private List<String> invitees;
    private List<UserShortDto> participants;
}