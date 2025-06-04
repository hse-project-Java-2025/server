package com.smartcalendar.service;

import com.smartcalendar.dto.DailyTaskDto;
import com.smartcalendar.dto.StatisticsData;
import com.smartcalendar.model.Event;
import com.smartcalendar.model.Task;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.EventRepository;
import com.smartcalendar.repository.TaskRepository;
import com.smartcalendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatisticsService statisticsService;

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    public List<Task> findTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(UUID taskId) {
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        eventRepository.deleteById(eventId);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public boolean changeCredentials(String currentUsername, String currentPassword, String newUsername, String newPassword) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            if (newUsername != null && !newUsername.isEmpty()) {
                user.setUsername(newUsername);
            }
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(passwordEncoder.encode(newPassword));
            }
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public UserDetailsService userDetailsService() {
        return this::loadUserByUsername;
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    @Transactional
    public User updateEmail(Long id, String newEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    @Transactional
    public Task updateTaskStatus(UUID taskId, boolean completed) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setCompleted(completed);
        return taskRepository.save(task);
    }

    @Transactional
    public void updateEvent(UUID eventId, Event event) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setStart(event.getStart());
        existingEvent.setEnd(event.getEnd());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setType(event.getType());
        existingEvent.setCreationTime(event.getCreationTime());
        eventRepository.save(existingEvent);
    }

    @Transactional(readOnly = true)
    public String getTaskDescription(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return task.getDescription();
    }

    public List<Event> findEventsByUserId(Long userId) {
        return eventRepository.findByOrganizerId(userId);
    }

    public Event createEvent(Event event) {
        event.setId(null);
        return eventRepository.save(event);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<DailyTaskDto> findAllEventsAsDailyTaskDto(Long userId) {
        List<Event> events = findEventsByUserId(userId);
        return events.stream().map(event -> new DailyTaskDto(
                event.getId(),
                event.getTitle(),
                event.isCompleted(),
                event.getType(),
                event.getCreationTime(),
                event.getDescription(),
                event.getStart() != null ? event.getStart().toLocalTime() : null,
                event.getEnd() != null ? event.getEnd().toLocalTime() : null,
                event.getStart() != null ? event.getStart().toLocalDate() : null
        )).collect(Collectors.toList());
    }

    public Task getTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Event getEventById(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Transactional
    public Event updateEventStatus(UUID eventId, boolean completed) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setCompleted(completed);
        return eventRepository.save(event);
    }

    @Transactional
    public UUID deleteEventById(UUID eventId) {
        eventRepository.deleteById(eventId);
        return eventId;
    }

    public StatisticsData getStatistics(Long userId) {
        return statisticsService.getStatistics(userId);
    }

    @Transactional
    public void updateStatistics(Long userId, StatisticsData statisticsData) {
        statisticsService.updateStatistics(userId, statisticsData);
    }

    @Transactional
    public Task createTaskWithCustomId(Task task) {
        if (task.getId() != null && taskRepository.existsById(task.getId())) {
            throw new IllegalArgumentException("Task with this id already exists");
        }
        return taskRepository.save(task);
    }

    @Transactional
    public void editTask(UUID taskId, Task task) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setCompleted(task.isCompleted());
        existingTask.setDueDateTime(task.getDueDateTime());
        existingTask.setAllDay(task.getAllDay());
        existingTask.setCreationTime(task.getCreationTime());
        taskRepository.save(existingTask);
    }

    @Transactional
    public Event createEventWithCustomId(Event event) {
        if (event.getId() != null && eventRepository.existsById(event.getId())) {
            throw new IllegalArgumentException("Event with this id already exists");
        }
        return eventRepository.save(event);
    }

    @Transactional
    public void editEvent(UUID eventId, Event event) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setStart(event.getStart());
        existingEvent.setEnd(event.getEnd());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setType(event.getType());
        existingEvent.setCreationTime(event.getCreationTime());
        existingEvent.setCompleted(event.isCompleted());
        eventRepository.save(existingEvent);
    }
}