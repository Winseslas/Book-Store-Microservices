package com.winseslas.microservices.bookStore.UserManager.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationEvent {
    private String eventId;
    private String eventType;
    private String userId;
    private String email;
    private LocalDateTime timestamp;
    private String status;
    private String details;
}
