package com.smartcalendar.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalTimeTaskTypesDto {
    private long common;
    private long work;
    private long study;
    private long fitness;
}