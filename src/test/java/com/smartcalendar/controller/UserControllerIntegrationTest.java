package com.smartcalendar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcalendar.dto.DailyTaskDto;
import com.smartcalendar.dto.EventDto;
import com.smartcalendar.dto.StatisticsData;
import com.smartcalendar.dto.UserShortDto;
import com.smartcalendar.model.Event;
import com.smartcalendar.model.EventType;
import com.smartcalendar.model.Task;
import com.smartcalendar.model.User;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "JWT_SECRET=test_jwt_secret",
        "spring.security.enabled=false"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(com.smartcalendar.config.TestSecurityConfig.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    private User mockUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }

    @Test
    @WithMockUser
    void testGetAllUsers() throws Exception {
        Mockito.when(userService.findAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetUserById() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findUserById(1L)).thenReturn(user);
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void testCreateUser() throws Exception {
        User user = mockUser(2L, "newuser");
        Mockito.when(userService.createUser(any(User.class))).thenReturn(user);

        String json = """
            {
                "username": "newuser",
                "email": "newuser@example.com",
                "password": "Password123!"
            }
        """;
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser
    void testUpdateEmail() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.updateEmail(eq(1L), anyString())).thenReturn(user);

        mockMvc.perform(put("/api/users/1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"newemail@example.com\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void testGetTasksByUserId() throws Exception {
        User user = mockUser(1L, "testuser");
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setUser(user);
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.findTasksByUserId(1L)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/users/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @WithMockUser
    void testGetEventsByUserId() throws Exception {
        User user = mockUser(1L, "testuser");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setOrganizer(user);

        EventDto eventDto = new EventDto();
        eventDto.setId(event.getId());
        eventDto.setTitle("Test Event");
        eventDto.setOrganizer(new UserShortDto(user.getUsername(), user.getEmail()));

        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.findEventsByUserId(1L)).thenReturn(List.of(event));
        Mockito.when(userService.toEventDto(event)).thenReturn(eventDto);

        mockMvc.perform(get("/api/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(event.getId().toString()))
                .andExpect(jsonPath("$[0].organizer.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void testCreateEvent() throws Exception {
        User user = mockUser(1L, "testuser");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setOrganizer(user);

        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.createEventWithCustomId(any(Event.class))).thenReturn(event);

        String json = objectMapper.writeValueAsString(event);
        mockMvc.perform(post("/api/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser
    void testCreateTask() throws Exception {
        User user = mockUser(1L, "testuser");
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setUser(user);

        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.createTaskWithCustomId(any(Task.class))).thenReturn(task);

        String json = objectMapper.writeValueAsString(task);
        mockMvc.perform(post("/api/users/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser
    void testGetCurrentUserInfo() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void testGetAllEventsAsDailyTasks() throws Exception {
        User user = mockUser(1L, "testuser");
        DailyTaskDto dailyTask = new DailyTaskDto(
                UUID.randomUUID(),
                "Test Task",
                false,
                EventType.WORK,
                LocalDateTime.now(),
                "desc",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.now()
        );
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.findAllEventsAsDailyTaskDto(1L)).thenReturn(List.of(dailyTask));

        mockMvc.perform(get("/api/users/1/events/dailytasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    @WithMockUser
    void testGetStatistics() throws Exception {
        User user = mockUser(1L, "testuser");
        StatisticsData statisticsData = new StatisticsData();
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.getStatistics(1L)).thenReturn(statisticsData);

        mockMvc.perform(get("/api/users/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTime").exists());
    }

    @Test
    @WithMockUser
    void testUpdateStatistics() throws Exception {
        User user = mockUser(1L, "testuser");
        StatisticsData statisticsData = new StatisticsData();
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        String json = objectMapper.writeValueAsString(statisticsData);
        mockMvc.perform(put("/api/users/1/statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}