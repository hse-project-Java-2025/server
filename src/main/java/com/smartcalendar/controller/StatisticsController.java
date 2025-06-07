package com.smartcalendar.controller;

import com.smartcalendar.dto.*;
import com.smartcalendar.model.User;
import com.smartcalendar.service.StatisticsService;
import com.smartcalendar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/total-time-task-types")
    public ResponseEntity<TotalTimeTaskTypesDto> getTotalTimeTaskTypes(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(statisticsService.getTotalTimeTaskTypes(userId));
    }

    @GetMapping("/today")
    public ResponseEntity<TodayTimeDto> getTodayTimeDto(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(statisticsService.getTodayTimeDto(userId));
    }

    @GetMapping("/continuous-success-days")
    public ResponseEntity<ContinuesSuccessDaysDto> getContinuesSuccessDaysDto(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(statisticsService.getContinuesSuccessDaysDto(userId));
    }

    @GetMapping("/average-day-time")
    public ResponseEntity<AverageDayTimeDto> getAverageDayTimeDto(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(statisticsService.getAverageDayTimeDto(userId));
    }
}