package com.smartcalendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;
    private final PushNotificationService pushNotificationService;

    public void sendEmail(String to, String subject, String text) {
        //if (to == null || to.isBlank()) return;
        //SimpleMailMessage message = new SimpleMailMessage();
        //message.setTo(to);
        //message.setSubject(subject);
        //message.setText(text);
        //mailSender.send(message);
    }//

    public void sendPush(String deviceToken, String title, String body) {
        if (deviceToken == null || deviceToken.isBlank()) return;
        pushNotificationService.sendPush(deviceToken, title, body);
    }
}