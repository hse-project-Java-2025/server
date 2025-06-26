package com.smartcalendar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequest {
    private Long user2Id;

    public Long getUser2Id() {
        return user2Id;
    }
}

