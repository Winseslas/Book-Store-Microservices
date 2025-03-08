package com.winseslas.microservices.bookStore.NotificationManager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String to;
    private String subject;
    private String content;
    private String template;
    private NotificationType type;
    private Map<String, Object> additionalData;
}
