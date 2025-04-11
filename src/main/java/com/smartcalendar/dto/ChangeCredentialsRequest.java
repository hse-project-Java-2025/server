package com.smartcalendar.dto;

import lombok.Data;

@Data
public class ChangeCredentialsRequest {
    private String currentUsername;
    private String currentPassword;
    private String newUsername;
    private String newPassword;
}