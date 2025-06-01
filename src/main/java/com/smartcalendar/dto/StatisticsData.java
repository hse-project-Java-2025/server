package com.smartcalendar.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsData {
    private TotalTimeTaskTypesDto totalTime = new TotalTimeTaskTypesDto(0, 0, 0, 0);
    private long weekTime = 0;
    private TodayTimeDto todayTime = new TodayTimeDto(0, 0);
    private ContinuesSuccessDaysDto continuesSuccessDays = new ContinuesSuccessDaysDto(0, 0);
    private AverageDayTimeDto averageDayTime = new AverageDayTimeDto(0, 0);
}