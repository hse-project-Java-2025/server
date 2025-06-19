package com.smartcalendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String title;
    @Column
    private String description;
    @Column
    private boolean isCompleted;

    private LocalDateTime dueDateTime;
    private Boolean allDay = false;

    private LocalDateTime creationTime = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user_tasks")
    private User user;
}