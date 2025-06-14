package com.smartcalendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    // private final JavaMailSender mailSender;
    // private final PushNotificationService pushNotificationService;

    public void sendEmail(String to, String subject, String text) {
        System.out.println("[STUB] Email to: " + to + ", subject: " + subject + ", text: " + text);
    }

    public void sendPush(String deviceToken, String title, String body) {
        System.out.println("[STUB] Push to: " + deviceToken + ", title: " + title + ", body: " + body);
    }
}