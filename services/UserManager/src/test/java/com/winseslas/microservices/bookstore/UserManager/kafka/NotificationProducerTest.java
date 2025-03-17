package com.winseslas.microservices.bookstore.UserManager.kafka;

import com.winseslas.microservices.bookstore.UserManager.model.request.NotificationRequest;
import com.winseslas.microservices.bookstore.UserManager.model.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    @Test
    void sendRegistrationEmail_Success() {
        // Arrange
        String email = "test@example.com";
        String userName = "John Doe";
        String confirmationLink = "http://example.com/confirm";
        
        when(kafkaTemplate.send(eq("notification-events"), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        notificationProducer.sendRegistrationEmail(email, userName, confirmationLink);

        // Assert
        verify(kafkaTemplate).send(
            eq("notification-events"),
            eq(email),
            any(NotificationRequest.class)
        );
    }

    @Test
    void sendLoginNotificationSMS_Success() {
        // Arrange
        String phoneNumber = "+1234567890";
        String userName = "John Doe";
        
        when(kafkaTemplate.send(eq("notification-events"), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        notificationProducer.sendLoginNotificationSMS(phoneNumber, userName);

        // Assert
        verify(kafkaTemplate).send(
            eq("notification-events"),
            eq(phoneNumber),
            any(NotificationRequest.class)
        );
    }

    @Test
    void sendPasswordResetEmail_Success() {
        // Arrange
        String email = "test@example.com";
        String userName = "John Doe";
        String resetLink = "http://example.com/reset";
        
        when(kafkaTemplate.send(eq("notification-events"), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        notificationProducer.sendPasswordResetEmail(email, userName, resetLink);

        // Assert
        verify(kafkaTemplate).send(
            eq("notification-events"),
            eq(email),
            any(NotificationRequest.class)
        );
    }
}
