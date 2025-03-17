package com.smartcalendar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "end_time")
    private LocalDateTime end;

    @Column(name = "start_time")
    private LocalDateTime start;

    private String location;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;
}