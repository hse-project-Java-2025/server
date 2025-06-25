package com.smartcalendar.service;

import com.smartcalendar.exceptions.ConflictException;
import com.smartcalendar.exceptions.ResourceNotFoundException;
import com.smartcalendar.model.Friendship;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.FriendshipRepository;
import com.smartcalendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public List<Friendship> getSubscriptions(Long user1Id) {
        userRepository.findById(user1Id).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + user1Id));
        return friendshipRepository.findByUser1(userRepository.findById(user1Id).get());
    }

    public Friendship createSubscription(Long user1Id, Long user2Id) {
        userRepository.findById(user1Id).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + user1Id));

        userRepository.findById(user2Id).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + user2Id));

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        if (friendshipRepository.existsByUser1AndUser2(user1, user2)) {
            throw new ConflictException("Subscription already exists");
        }

        Friendship subscription = new Friendship();
        subscription.setUser1(user1);
        subscription.setUser2(user2);

        return friendshipRepository.save(subscription);
    }

    public void deleteSubscription(Long user1Id, Long user2Id) {
        Friendship subscription = friendshipRepository
                .findByUser1AndUser2(userRepository.findById(user1Id).get(), userRepository.findById(user2Id).get())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        friendshipRepository.delete(subscription);
    }
}