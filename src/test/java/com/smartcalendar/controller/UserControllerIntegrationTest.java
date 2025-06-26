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

    // --- USERS ---

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
    void testCreateUserWithExistingUsername() throws Exception {
        Mockito.when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        String json = """
            {
                "username": "existinguser",
                "email": "newuser@example.com",
                "password": "Password123!"
            }
        """;
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    @WithMockUser
    void testCreateUserWithExistingEmail() throws Exception {
        Mockito.when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        String json = """
            {
                "username": "newuser",
                "email": "existing@example.com",
                "password": "Password123!"
            }
        """;
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    // --- EMAIL ---

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
    void testUpdateEmail_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        mockMvc.perform(put("/api/users/2/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"newemail@example.com\""))
                .andExpect(status().isForbidden());
    }

    // --- TASKS ---

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
    void testGetTasksByUserId_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/users/2/tasks"))
                .andExpect(status().isForbidden());
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
    void testCreateTask_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Task task = new Task();
        String json = objectMapper.writeValueAsString(task);
        mockMvc.perform(post("/api/users/2/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testGetTaskDescription_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        Task task = new Task();
        task.setId(UUID.randomUUID());
        User otherUser = mockUser(2L, "otheruser");
        task.setUser(otherUser);

        Mockito.when(userService.getTaskById(any(UUID.class))).thenReturn(task);
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/tasks/" + task.getId() + "/description"))
                .andExpect(status().isForbidden());
    }

    // --- EVENTS ---

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
    void testGetEventsByUserId_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/users/2/events"))
                .andExpect(status().isForbidden());
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
    void testCreateEvent_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Event event = new Event();
        String json = objectMapper.writeValueAsString(event);
        mockMvc.perform(post("/api/users/2/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    // --- COLLABORATIVE EVENTS ---

    @Test
    @WithMockUser
    void testInviteUserToEvent_UserAlreadyParticipant() throws Exception {
        User user = mockUser(1L, "testuser");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setParticipants(List.of(user));
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);
        Mockito.when(userService.findByLoginOrEmail("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginOrEmail\":\"testuser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is already a participant"));
    }

    @Test
    @WithMockUser
    void testInviteUserToEvent_UserAlreadyInvited() throws Exception {
        User user = mockUser(1L, "testuser");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setInvitees(new ArrayList<>(List.of(user.getEmail())));
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);
        Mockito.when(userService.findByLoginOrEmail("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginOrEmail\":\"testuser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is already invited"));
    }

    @Test
    @WithMockUser
    void testInviteUserToEvent_UserNotFound() throws Exception {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);
        Mockito.when(userService.findByLoginOrEmail("nouser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginOrEmail\":\"nouser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @WithMockUser
    void testAcceptInvite_NoInvite() throws Exception {
        User user = mockUser(1L, "testuser");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setInvitees(new ArrayList<>());
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/accept-invite"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No invite found for this user"));
    }

    @Test
    @WithMockUser
    void testRemoveParticipant_NotOrganizer() throws Exception {
        User user = mockUser(1L, "testuser");
        User other = mockUser(2L, "other");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setOrganizer(other);
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/remove-participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginOrEmail\":\"other\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only organizer can remove participants"));
    }

    @Test
    @WithMockUser
    void testRemoveParticipant_OrganizerCannotBeRemoved() throws Exception {
        User user = mockUser(1L, "testuser");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setOrganizer(user);
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);
        Mockito.when(userService.findByLoginOrEmail("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/remove-participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginOrEmail\":\"testuser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Organizer cannot be removed"));
    }

    @Test
    @WithMockUser
    void testRemoveParticipant_UserNotFound() throws Exception {
        User user = mockUser(1L, "testuser");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setOrganizer(user);
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);
        Mockito.when(userService.findByLoginOrEmail("nouser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/remove-participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginOrEmail\":\"nouser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @WithMockUser
    void testRemoveParticipant_UserNotParticipant() throws Exception {
        User user = mockUser(1L, "testuser");
        User other = mockUser(2L, "other");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setOrganizer(user);
        event.setParticipants(new ArrayList<>());
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.getEventById(event.getId())).thenReturn(event);
        Mockito.when(userService.findByLoginOrEmail("other")).thenReturn(Optional.of(other));

        mockMvc.perform(post("/api/users/events/" + event.getId() + "/remove-participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginOrEmail\":\"other\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is not a participant"));
    }

    // --- STATISTICS ---

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
    void testGetStatistics_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/users/2/statistics"))
                .andExpect(status().isForbidden());
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

    @Test
    @WithMockUser
    void testUpdateStatistics_Forbidden() throws Exception {
        User user = mockUser(1L, "testuser");
        StatisticsData statisticsData = new StatisticsData();
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        String json = objectMapper.writeValueAsString(statisticsData);
        mockMvc.perform(put("/api/users/2/statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    // --- EDGE CASES ---

    @Test
    @WithMockUser
    void testGetAllEventsAsDailyTasks_Empty() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.findAllEventsAsDailyTaskDto(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/1/events/dailytasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser
    void testGetEventsByUserId_Empty() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.findEventsByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser
    void testGetTasksByUserId_Empty() throws Exception {
        User user = mockUser(1L, "testuser");
        Mockito.when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userService.findTasksByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}