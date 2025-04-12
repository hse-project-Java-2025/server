package com.smartcalendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "group_messages")
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String text;

    @Column(name = "time_sent")
    private LocalDateTime timeWhenSent;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonBackReference
    private GroupChat chat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
