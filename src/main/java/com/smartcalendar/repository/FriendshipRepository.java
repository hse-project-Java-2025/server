package com.smartcalendar.repository;

import com.smartcalendar.model.Friendship;
import com.smartcalendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByUser1(User user1);
    Optional<Friendship> findByUser1AndUser2(User user1, User user2);
    boolean existsByUser1AndUser2(User user1, User user2);
}