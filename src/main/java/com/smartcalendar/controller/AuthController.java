package com.smartcalendar.controller;

import com.smartcalendar.dto.RegistrationRequest;
import com.smartcalendar.model.User;
import com.smartcalendar.service.JwtService;
import com.smartcalendar.service.StatisticsService;
import com.smartcalendar.service.UserService;
import com.smartcalendar.dto.ChangeCredentialsRequest;
import com.smartcalendar.dto.StatisticsData;
import com.smartcalendar.dto.AverageDayTimeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final StatisticsService statisticsService;

    @Operation(
            summary = "User authentication",
            description = "Returns JWT-token for registered user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authentication"),
            @ApiResponse(responseCode = "400", description = "Wrong request parameters"),
            @ApiResponse(responseCode = "403", description = "Wrong authentication data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        logger.info("Attempting to authenticate user: {}", user.getUsername());

        if ((user.getUsername() == null && user.getEmail() == null) || user.getPassword() == null) {
            logger.warn("Username/email or password is null. Username: {}, Email: {}", user.getUsername(), user.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username/email and password are required");
        }

        try {
            logger.debug("Determining if login is by username or email: {}", user.getUsername() != null ? user.getUsername() : user.getEmail());
            UserDetails userDetails;

            try {
                if (user.getUsername() != null) {
                    userDetails = userService.loadUserByUsername(user.getUsername());
                } else {
                    userDetails = userService.loadUserByEmail(user.getEmail());
                }
            } catch (UsernameNotFoundException e) {
                logger.error("User not found: {}", user.getUsername() != null ? user.getUsername() : user.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid username or email");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDetails.getUsername(), user.getPassword())
            );

            logger.debug("Authentication successful for user: {}", userDetails.getUsername());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (user.getDeviceToken() != null && !user.getDeviceToken().isBlank()) {
                Optional<User> dbUserOpt = userService.findByUsername(userDetails.getUsername());
                if (dbUserOpt.isPresent()) {
                    User dbUser = dbUserOpt.get();
                    dbUser.setDeviceToken(user.getDeviceToken());
                    userService.createUser(dbUser);
                }
            }

            String jwt = jwtService.generateToken(userDetails.getUsername());
            logger.info("JWT token generated for user: {}", userDetails.getUsername());

            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", user.getUsername() != null ? user.getUsername() : user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user: {}", user.getUsername() != null ? user.getUsername() : user.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @Operation(
            summary = "New user signup",
            description = "Creates a new user profile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful signup"),
            @ApiResponse(responseCode = "400", description = "Wrong request parameters or User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(@RequestBody RegistrationRequest request) {
        logger.info("Attempting to register user: {}", request.getUsername());

        if (request.getUsername() == null || request.getPassword() == null || request.getEmail() == null || request.getFirstDay() == null) {
            logger.warn("Missing required fields for registration. Username: {}, Email: {}, FirstDay: {}", request.getUsername(), request.getEmail(), request.getFirstDay());
            return ResponseEntity.badRequest().body(null);
        }

        if (userService.existsByUsername(request.getUsername())) {
            logger.warn("Username already exists: {}", request.getUsername());
            return ResponseEntity.badRequest().body(null);
        }
        if (userService.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            return ResponseEntity.badRequest().body(null);
        }

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            User createdUser = userService.createUser(user);

            StatisticsData statisticsData = new StatisticsData();
            statisticsData.setAverageDayTime(
                    new AverageDayTimeDto(0, LocalDate.parse(request.getFirstDay()))
            );
            statisticsService.updateStatistics(createdUser.getId(), statisticsData);

            logger.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/change-credentials")
    public ResponseEntity<?> changeCredentials(@RequestBody ChangeCredentialsRequest request) {
        boolean success = userService.changeCredentials(
                request.getCurrentUsername(),
                request.getCurrentPassword(),
                request.getNewUsername(),
                request.getNewPassword()
        );

        if (success) {
            return ResponseEntity.ok("Credentials updated successfully");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid current username or password");
    }
}