package com.smartcalendar.controller;

import com.smartcalendar.dto.*;
import com.smartcalendar.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/total-time-task-types")
    public ResponseEntity<TotalTimeTaskTypes> getTotalTimeTaskTypes() {
        return ResponseEntity.ok(statisticsService.getTotalTimeTaskTypes());
    }

    @GetMapping("/today")
    public ResponseEntity<TodayTimeVars> getTodayTimeVars() {
        return ResponseEntity.ok(statisticsService.getTodayTimeVars());
    }

    @GetMapping("/continuous-success-days")
    public ResponseEntity<ContinuousSuccessDaysVars> getContinuousSuccessDaysVars() {
        return ResponseEntity.ok(statisticsService.getContinuousSuccessDaysVars());
    }

    @GetMapping("/average-day-time")
    public ResponseEntity<AverageDayTimeVars> getAverageDayTimeVars() {
        return ResponseEntity.ok(statisticsService.getAverageDayTimeVars());
    }
}