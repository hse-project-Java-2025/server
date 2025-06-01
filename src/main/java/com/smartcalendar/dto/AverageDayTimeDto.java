package com.smartcalendar.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AverageDayTimeDto {
    private long totalWorkMinutes;
    private long totalDays;
}