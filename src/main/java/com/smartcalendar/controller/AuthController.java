package com.smartcalendar.controller;

import com.smartcalendar.model.User;
import com.smartcalendar.service.JwtService;
import com.smartcalendar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        logger.info("Attempting to authenticate user: {}", user.getUsername());

        if (user.getUsername() == null || user.getPassword() == null) {
            logger.warn("Username or password is null. Username: {}, Password: {}", user.getUsername(), user.getPassword());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password are required");
        }

        try {
            logger.debug("Creating authentication token for user: {}", user.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            logger.debug("Authentication successful for user: {}", user.getUsername());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtService.generateToken(user.getUsername());
            logger.info("JWT token generated for user: {}", user.getUsername());

            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        logger.info("Attempting to register user: {}", user.getUsername());

        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            logger.warn("Missing required fields for registration. Username: {}, Email: {}", user.getUsername(), user.getEmail());
            return ResponseEntity.badRequest().body(null);
        }

        if (userService.existsByUsername(user.getUsername())) {
            logger.warn("Username already exists: {}", user.getUsername());
            return ResponseEntity.badRequest().body(null);
        }
        if (userService.existsByEmail(user.getEmail())) {
            logger.warn("Email already exists: {}", user.getEmail());
            return ResponseEntity.badRequest().body(null);
        }

        try {
            User createdUser = userService.createUser(user);
            logger.info("User registered successfully: {}", user.getUsername());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}