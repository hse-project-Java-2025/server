package com.smartcalendar.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = EventTypeDeserializer.class)
public enum EventType {
    COMMON, FITNESS, WORK, STUDIES
}

