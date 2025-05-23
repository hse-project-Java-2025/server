package com.smartcalendar.controller;

import com.smartcalendar.dto.DailyTaskDto;
import com.smartcalendar.model.Event;
import com.smartcalendar.model.Task;
import com.smartcalendar.model.User;
import com.smartcalendar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<User> updateEmail(
            @PathVariable Long id,
            @RequestBody String newEmail,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }
        User updatedUser = userService.updateEmail(id, newEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestBody Map<String, Boolean> requestBody,
            @AuthenticationPrincipal UserDetails userDetails) {
        Task task = userService.getTaskById(taskId);
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!task.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        boolean completed = requestBody.get("completed");
        Task updatedTask = userService.updateTaskStatus(taskId, completed);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/tasks/{taskId}/description")
    public ResponseEntity<String> getTaskDescription(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Task task = userService.getTaskById(taskId);
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!task.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        String description = userService.getTaskDescription(taskId);
        return ResponseEntity.ok(description);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{userId}/tasks")
    public ResponseEntity<List<Task>> getTasksByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        List<Task> tasks = userService.findTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{userId}/events")
    public ResponseEntity<List<Event>> getEventsByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        List<Event> events = userService.findEventsByUserId(userId);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{userId}/events")
    public ResponseEntity<Event> createEvent(
            @PathVariable Long userId,
            @RequestBody Event event,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        event.setOrganizer(currentUser);
        Event createdEvent = userService.createEvent(event);
        return ResponseEntity.ok(createdEvent);
    }

    @PostMapping("/{userId}/tasks")
    public ResponseEntity<Task> createTask(
            @PathVariable Long userId,
            @RequestBody Task task,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        task.setUser(currentUser);
        Task createdTask = userService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Task task = userService.getTaskById(taskId);
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!task.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        userService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> result = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/events/dailytasks")
    public ResponseEntity<List<DailyTaskDto>> getAllEventsAsDailyTasks(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        List<DailyTaskDto> dailyTasks = userService.findAllEventsAsDailyTaskDto(userId);
        return ResponseEntity.ok(dailyTasks);
    }
}