package com.smartcalendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "group_messages")
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessage {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column
    private String messageText;

    @Column(name = "time_sent")
    private LocalDateTime timeWhenSent;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonBackReference(value = "messages_in_group_chat")
    private GroupChat chat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "group_message_author")
    private User user;
}
