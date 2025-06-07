package com.smartcalendar.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AverageDayTimeDto {
    private long totalWorkMinutes;
    private LocalDate firstDay;
}