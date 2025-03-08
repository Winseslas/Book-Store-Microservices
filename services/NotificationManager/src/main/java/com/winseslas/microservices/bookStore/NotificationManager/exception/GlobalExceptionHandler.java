package com.winseslas.microservices.bookStore.NotificationManager.exception;

import com.winseslas.microservices.bookStore.NotificationManager.exception.NotificationException.NotificationErrorType;
import com.winseslas.microservices.bookStore.NotificationManager.util.NotificationDebugger;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final NotificationDebugger debugger;
    private final MeterRegistry meterRegistry;

    @ExceptionHandler(NotificationException.class)
    public void handleNotificationException(NotificationException ex) {
        Map<String, Object> context = new HashMap<>();
        context.put("errorType", ex.getErrorType());
        context.put("recipient", ex.getRecipient());
        context.put("details", ex.getDetails());
        
        log.error("Notification error: {} for recipient: {}", ex.getErrorType(), ex.getRecipient());
        debugger.logError("NOTIFICATION_ERROR", ex, context);
        
        meterRegistry.counter("notification.errors", 
            "type", ex.getErrorType().toString(),
            "recipient", ex.getRecipient() != null ? ex.getRecipient() : "unknown"
        ).increment();

        if (ex.getErrorType() == NotificationErrorType.KAFKA_CONNECTION_ERROR ||
            ex.getErrorType() == NotificationErrorType.EMAIL_SEND_ERROR ||
            ex.getErrorType() == NotificationErrorType.SMS_SEND_ERROR) {
            handleRetry(ex);
        }
    }

    @ExceptionHandler(ListenerExecutionFailedException.class)
    public void handleKafkaException(ListenerExecutionFailedException ex) {
        Map<String, Object> context = new HashMap<>();
        context.put("kafkaKey", ex.getGroupId());
        context.put("topic", ex.getMessage());
        
        log.error("Kafka error: {} for group: {}", ex.getMessage(), ex.getGroupId());
        debugger.logError("KAFKA_ERROR", ex, context);
        
        meterRegistry.counter("kafka.errors",
            "group", ex.getGroupId()
        ).increment();
    }

    @ExceptionHandler(MessagingException.class)
    public void handleEmailException(MessagingException ex) {
        Map<String, Object> context = new HashMap<>();
        context.put("emailError", ex.getMessage());
        
        log.error("Email error: {}", ex.getMessage());
        debugger.logError("EMAIL_ERROR", ex, context);
        
        meterRegistry.counter("email.errors").increment();
    }

    private void handleRetry(NotificationException ex) {
        // Logique de retry à implémenter
        // Par exemple : envoyer vers une DLQ, réessayer avec backoff, etc.
        log.info("Handling retry for notification error: {} to recipient: {}", 
            ex.getErrorType(), ex.getRecipient());
    }
}
