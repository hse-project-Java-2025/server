package com.smartcalendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "private_chats")
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChat {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id1")
    @JsonBackReference(value = "chats1")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user_id2")
    @JsonBackReference(value = "chats2")
    private User user2;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "messages_in_private_chat")
    private List<PrivateMessage> messages;
}
