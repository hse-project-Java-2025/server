package com.smartcalendar.controller;

import com.smartcalendar.model.Event;
import com.smartcalendar.model.Task;
import com.smartcalendar.model.User;
import com.smartcalendar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<User> updateEmail(@PathVariable Long id, @RequestBody String newEmail) {
        User updatedUser = userService.updateEmail(id, newEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long taskId, @RequestBody Map<String, Boolean> requestBody) {
        boolean completed = requestBody.get("completed");
        Task updatedTask = userService.updateTaskStatus(taskId, completed);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/tasks/{taskId}/description")
    public ResponseEntity<String> getTaskDescription(@PathVariable Long taskId) {
        String description = userService.getTaskDescription(taskId);
        return ResponseEntity.ok(description);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{userId}/tasks")
    public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long userId) {
        List<Task> tasks = userService.findTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{userId}/events")
    public ResponseEntity<List<Event>> getEventsByUserId(@PathVariable Long userId) {
        List<Event> events = userService.findEventsByUserId(userId);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{userId}/events")
    public ResponseEntity<Event> createEvent(@PathVariable Long userId, @RequestBody Event event) {
        User user = userService.findUserById(userId);
        event.setOrganizer(user);
        Event createdEvent = userService.createEvent(event);
        return ResponseEntity.ok(createdEvent);
    }

    @PostMapping("/{userId}/tasks")
    public ResponseEntity<Task> createTask(@PathVariable Long userId, @RequestBody Task task) {
        User user = userService.findUserById(userId);
        task.setUser(user);
        Task createdTask = userService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        userService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

}