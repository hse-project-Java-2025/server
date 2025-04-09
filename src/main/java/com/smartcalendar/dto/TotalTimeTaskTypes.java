package com.smartcalendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TotalTimeTaskTypes {
    private long common;
    private long work;
    private long study;
    private long fitness;
}
