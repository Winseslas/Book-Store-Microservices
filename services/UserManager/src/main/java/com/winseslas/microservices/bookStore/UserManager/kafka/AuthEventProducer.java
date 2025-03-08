package com.winseslas.microservices.bookStore.UserManager.kafka;

import com.winseslas.microservices.bookStore.UserManager.model.event.AuthenticationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAuthEvent(String userId, String email, String eventType, String status, String details) {
        AuthenticationEvent event = AuthenticationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(userId)
                .email(email)
                .timestamp(LocalDateTime.now())
                .status(status)
                .details(details)
                .build();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("user-events", event.getEventId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Auth event sent successfully for user: {} with eventType: {}", userId, eventType);
            } else {
                log.error("Failed to send auth event for user: {} with eventType: {}", userId, eventType, ex);
            }
        });
    }
}
