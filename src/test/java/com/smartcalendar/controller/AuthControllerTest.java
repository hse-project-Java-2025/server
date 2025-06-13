package com.smartcalendar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcalendar.model.User;
import com.smartcalendar.service.JwtService;
import com.smartcalendar.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(com.smartcalendar.config.TestSecurityConfig.class)
@ActiveProfiles("test")
@SpringJUnitConfig(AuthControllerTest.TestConfig.class)
public class AuthControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    private String registrationRequestJson(String username, String email, String password, String firstDay) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "username", username,
                "email", email,
                "password", password,
                "firstDay", firstDay
        ));
    }

    @BeforeEach
    void setUp() {
        userService.deleteAllUsersAndStatistics();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("validPassword123");
        userService.createUser(testUser);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequestJson("newuser", "new@example.com", "Password123!", "2025-06-07")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void testRegisterUser_MissingFirstDay() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "username", "user2",
                "email", "user2@example.com",
                "password", "Password123!"
        ));
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getUsername(),
                testUser.getPassword()
        );
        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtService.generateToken(testUser.getUsername()))
                .thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().string("mocked.jwt.token"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        User invalidUser = new User();
        invalidUser.setUsername("testuser");
        invalidUser.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLogin_MissingFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterUser_DuplicateUsername() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequestJson("testuser", "duplicate@example.com", "password123", "2025-06-07")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUser_PasswordIsHashed() {
        User user = new User();
        user.setUsername("uniqueuser");
        user.setPassword("rawPassword123");
        user.setEmail("unique@example.com");

        User savedUser = userService.createUser(user);

        assertNotEquals("rawPassword123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("rawPassword123", savedUser.getPassword()));
    }
}