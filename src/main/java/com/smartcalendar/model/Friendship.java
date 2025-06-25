package com.smartcalendar.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Table(name = "friendships")
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id1")
    @JsonBackReference(value = "friends1")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user_id2")
    @JsonBackReference(value = "friends2")
    private User user2;

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }
}
