package com.smartcalendar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;

    private String description;

    @Column(name = "start_time")
    private LocalDateTime start;

    @Column(name = "end_time")
    private LocalDateTime end;

    private String location;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private LocalDateTime creationTime = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    private boolean completed = false;
}