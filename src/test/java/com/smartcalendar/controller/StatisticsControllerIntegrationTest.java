package com.smartcalendar.controller;

import com.smartcalendar.dto.*;
import com.smartcalendar.model.User;
import com.smartcalendar.service.StatisticsService;
import com.smartcalendar.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "JWT_SECRET=test_jwt_secret"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(com.smartcalendar.config.TestSecurityConfig.class)
class StatisticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserService userService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public StatisticsService statisticsService() {
            return Mockito.mock(StatisticsService.class);
        }
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    private void mockAuth() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
    }

    @Test
    @WithMockUser
    void testGetTotalTimeTaskTypes() throws Exception {
        mockAuth();
        Mockito.when(statisticsService.getTotalTimeTaskTypes(any())).thenReturn(new TotalTimeTaskTypesDto(1,2,3,4));
        mockMvc.perform(get("/api/statistics/total-time-task-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.common").value(1));
    }

    @Test
    @WithMockUser
    void testGetTodayTimeDto() throws Exception {
        mockAuth();
        Mockito.when(statisticsService.getTodayTimeDto(any())).thenReturn(new TodayTimeDto(5,6));
        mockMvc.perform(get("/api/statistics/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planned").value(5));
    }

    @Test
    @WithMockUser
    void testGetContinuesSuccessDaysDto() throws Exception {
        mockAuth();
        Mockito.when(statisticsService.getContinuesSuccessDaysDto(any())).thenReturn(new ContinuesSuccessDaysDto(7,8));
        mockMvc.perform(get("/api/statistics/continuous-success-days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.record").value(7));
    }

    @Test
    @WithMockUser
    void testGetAverageDayTimeDto() throws Exception {
        mockAuth();
        Mockito.when(statisticsService.getAverageDayTimeDto(any())).thenReturn(new AverageDayTimeDto(9, null));
        mockMvc.perform(get("/api/statistics/average-day-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkMinutes").value(9));
    }

    @Test
    @WithMockUser
    void testGetStatisticsWithJsonDate() throws Exception {
        mockAuth();
        StatisticsData statisticsData = new StatisticsData(
                new TotalTimeTaskTypesDto(1, 2, 3, 4),
                5L,
                new TodayTimeDto(6, 7),
                new ContinuesSuccessDaysDto(8, 9),
                new AverageDayTimeDto(10, null),
                new Date()
        );
        Mockito.when(userService.getStatistics(any())).thenReturn(statisticsData);

        mockMvc.perform(get("/api/users/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTime.common").value(1))
                .andExpect(jsonPath("$.jsonDate").exists());
    }
}