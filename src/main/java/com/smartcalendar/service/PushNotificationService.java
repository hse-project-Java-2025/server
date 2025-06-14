package com.smartcalendar.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void sendPush(String deviceToken, String title, String body) {
        try {
            String url = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(ResourceUtils.getFile(credentialsPath)))
                    .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
            credentials.refreshIfExpired();
            String accessToken = credentials.getAccessToken().getTokenValue();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> notification = Map.of(
                    "title", title,
                    "body", body
            );
            Map<String, Object> message = Map.of(
                    "token", deviceToken,
                    "notification", notification
            );
            Map<String, Object> bodyMap = Map.of("message", message);

            String jsonBody = objectMapper.writeValueAsString(bodyMap);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}