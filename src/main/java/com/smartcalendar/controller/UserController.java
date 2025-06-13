package com.smartcalendar.controller;

import com.smartcalendar.dto.DailyTaskDto;
import com.smartcalendar.dto.StatisticsData;
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
    public ResponseEntity<Void> updateTaskStatus(
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
        userService.updateTaskStatus(taskId, completed);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Map<String, Object>> createEvent(
            @PathVariable Long userId,
            @RequestBody Event event,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        event.setOrganizer(currentUser);
        try {
            Event createdEvent = userService.createEventWithCustomId(event);
            return ResponseEntity.ok(Map.of("id", createdEvent.getId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<Void> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody Event event,
            @AuthenticationPrincipal UserDetails userDetails) {
        Event existingEvent = userService.getEventById(eventId);
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!existingEvent.getOrganizer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        userService.editEvent(eventId, event);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{userId}/tasks")
    public ResponseEntity<Map<String, Object>> createTask(
            @PathVariable Long userId,
            @RequestBody Task task,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        task.setUser(currentUser);
        Task createdTask = userService.createTaskWithCustomId(task);
        return ResponseEntity.ok(Map.of("id", createdTask.getId()));
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

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<Void> editTask(
            @PathVariable UUID taskId,
            @RequestBody Task task,
            @AuthenticationPrincipal UserDetails userDetails) {
        Task existingTask = userService.getTaskById(taskId);
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!existingTask.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        userService.editTask(taskId, task);
        return ResponseEntity.ok().build();
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

    @PatchMapping("/events/{eventId}/status")
    public ResponseEntity<Event> updateEventStatus(
            @PathVariable UUID eventId,
            @RequestBody Map<String, Boolean> requestBody,
            @AuthenticationPrincipal UserDetails userDetails) {
        Event event = userService.getEventById(eventId);
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        boolean completed = requestBody.get("completed");
        Event updatedEvent = userService.updateEventStatus(eventId, completed);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Map<String, UUID>> deleteEventById(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Event event = userService.getEventById(eventId);
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        UUID deletedId = userService.deleteEventById(eventId);
        return ResponseEntity.ok(Map.of("id", deletedId));
    }

    @GetMapping("/{userId}/statistics")
    public ResponseEntity<StatisticsData> getStatistics(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        StatisticsData statistics = userService.getStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @PutMapping("/{userId}/statistics")
    public ResponseEntity<Void> updateStatistics(
            @PathVariable Long userId,
            @RequestBody StatisticsData statisticsData,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        userService.updateStatistics(userId, statisticsData);
        return ResponseEntity.ok().build();
    }
}