package com.smartcalendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column(name = "start_time")
    private LocalDateTime start;

    @Column(name = "end_time")
    private LocalDateTime end;

    @Column
    private String location;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    @JsonBackReference
    private User organizer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "events_tags",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonManagedReference
    private List<Tag> tags;

    @ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<User> users;
}