package com.smartcalendar.service;

import com.smartcalendar.dto.DailyTaskDto;
import com.smartcalendar.dto.StatisticsData;
import com.smartcalendar.model.Event;
import com.smartcalendar.model.Task;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.EventRepository;
import com.smartcalendar.repository.StatisticsRepository;
import com.smartcalendar.repository.TaskRepository;
import com.smartcalendar.repository.UserRepository;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatisticsService statisticsService;
    private final StatisticsRepository statisticsRepository;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;

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
        List<Event> asOrganizer = eventRepository.findByOrganizerId(userId);
        List<Event> asParticipant = eventRepository.findAll().stream()
                .filter(e -> e.getParticipants() != null && e.getParticipants().stream().anyMatch(u -> u.getId().equals(userId)))
                .collect(Collectors.toList());
        Set<Event> all = new HashSet<>(asOrganizer);
        all.addAll(asParticipant);
        return new ArrayList<>(all);
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

    @Transactional
    public void deleteAllUsersAndStatistics() {
        statisticsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void notifyUserAddedToEvent(User user, Event event, String deviceToken) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            String subject = "You have been added to event: " + event.getTitle();
            String text = "Hello, you have been added to the event \"" + event.getTitle() + "\" by " +
                    (event.getOrganizer() != null ? event.getOrganizer().getUsername() : "system") + ".";
            notificationService.sendEmail(user.getEmail(), subject, text);
        }
        if (deviceToken != null && !deviceToken.isBlank()) {
            notificationService.sendPush(
                    deviceToken,
                    "Added to event",
                    "You have been added to event: " + event.getTitle()
            );
        }
    }

    public void notifyUserRemovedFromEvent(User user, Event event, String deviceToken) {
        sendEventEmail(user, event, "You have been removed from event: ", "removed from the event");
        notificationService.sendPush(deviceToken,
                "Removed from event",
                "You have been removed from event: " + event.getTitle());
    }

    private void sendEventEmail(User user, Event event, String subjectPrefix, String actionText) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            String subject = subjectPrefix + event.getTitle();
            String text = "Hello, you have been " + actionText + " \"" + event.getTitle() + "\""
                    + (event.getOrganizer() != null ? " by " + event.getOrganizer().getUsername() : "") + ".";
            notificationService.sendEmail(user.getEmail(), subject, text);
        }
    }

    public List<Event> findEventsByInvitee(String email) {
        return eventRepository.findAll().stream()
                .filter(event -> event.getInvitees() != null && event.getInvitees().contains(email))
                .collect(Collectors.toList());
    }


    public void notifyInvitees(Event event) {
        if (event.getInvitees() == null || event.getInvitees().isEmpty()) return;

        String subject = "You have been invited to the event: " + event.getTitle();
        String text = "You have been invited by " +
                (event.getOrganizer() != null ? event.getOrganizer().getUsername() : "a user") +
                " to the event \"" + event.getTitle() + "\".";

        for (String email : event.getInvitees()) {
            Optional<User> userOpt = findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                notificationService.sendEmail(user.getEmail(), subject, text);
                if (user.getDeviceToken() != null && !user.getDeviceToken().isBlank()) {
                    notificationService.sendPush(user.getDeviceToken(), "Invitation", text);
                }
            } else {
                notificationService.sendEmail(email, subject, text);
            }
        }
    }

    public Optional<User> findByLoginOrEmail(String loginOrEmail) {
        Optional<com.smartcalendar.model.User> userOpt = userRepository.findByUsername(loginOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(loginOrEmail);
        }
        return userOpt;
    }

    @Transactional
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public void notifyEventDeleted(Event event) {
        String subject = "Event \"" + event.getTitle() + "\" has been deleted";
        String text = "The organizer " + event.getOrganizer().getUsername() + " has deleted the event.";
        notifyAllEventUsers(event, subject, text);
    }

    public void notifyEventUpdated(Event oldEvent, Event newEvent) {
        String subject = "Event \"" + oldEvent.getTitle() + "\" has been updated";
        String text = "The organizer " + oldEvent.getOrganizer().getUsername() + " has updated the event details.";
        notifyAllEventUsers(oldEvent, subject, text);
    }

    private void notifyAllEventUsers(Event event, String subject, String text) {
        if (event.getParticipants() != null) {
            for (User user : event.getParticipants()) {
                if (!user.getId().equals(event.getOrganizer().getId())) {
                    notificationService.sendEmail(user.getEmail(), subject, text);
                    if (user.getDeviceToken() != null && !user.getDeviceToken().isBlank()) {
                        notificationService.sendPush(user.getDeviceToken(), subject, text);
                    }
                }
            }
        }
        if (event.getInvitees() != null) {
            for (String email : event.getInvitees()) {
                notificationService.sendEmail(email, subject, text);
            }
        }
    }
}