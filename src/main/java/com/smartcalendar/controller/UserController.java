package com.smartcalendar.controller;

import com.smartcalendar.model.Task;
import com.smartcalendar.model.User;
import com.smartcalendar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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