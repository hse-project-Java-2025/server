package com.smartcalendar.dto;
import com.smartcalendar.model.Event;
import lombok.Data;

@Data
public class AddCollaborativeEventRequest {
    private String loginOrEmail;
    private String deviceToken;
    private Event event;
}