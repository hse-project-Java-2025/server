package com.smartcalendar.controller;

import com.smartcalendar.dto.SubscriptionRequest;
import com.smartcalendar.model.Friendship;
import com.smartcalendar.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Autowired
    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/my-subscriptions")
    public ResponseEntity<List<Friendship>> getMySubscriptions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        List<Friendship> subscriptions = friendshipService.getSubscriptions(currentUserId);
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Friendship> subscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid SubscriptionRequest request
    ) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        Friendship subscription = friendshipService.createSubscription(currentUserId, request.getUser2Id());
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @DeleteMapping("/unsubscribe/{followingId}")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long user2Id
    ) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        friendshipService.deleteSubscription(currentUserId, user2Id);
        return ResponseEntity.noContent().build();
    }
}
