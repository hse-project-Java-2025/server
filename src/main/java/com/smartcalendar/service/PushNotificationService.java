package com.smartcalendar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PushNotificationService {

    @Value("${fcm.server.key}")
    private String fcmServerKey;

    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";

    public void sendPush(String deviceToken, String title, String body) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + fcmServerKey);

        Map<String, Object> notification = Map.of(
                "title", title,
                "body", body
        );
        Map<String, Object> message = Map.of(
                "to", deviceToken,
                "notification", notification
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);
        restTemplate.postForEntity(FCM_API_URL, request, String.class);
    }
}