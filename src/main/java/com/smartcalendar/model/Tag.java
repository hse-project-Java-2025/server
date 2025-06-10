package com.smartcalendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Table(name = "tags")
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    //@JsonBackReference(value = "event_tags")
    @JsonIgnore
    private List<Event> events;
}
