package com.winseslas.microservices.bookStore.NotificationManager.util;

import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationDebugger {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<String, DebugContext> debugContexts = new ConcurrentHashMap<>();

    public void logNotificationRequest(NotificationRequest request, String stage) {
        DebugContext context = debugContexts.computeIfAbsent(
            getContextKey(request), 
            k -> new DebugContext()
        );

        log.info("=== Notification Request Debug [{}] ===", stage);
        log.info("Timestamp: {}", DATE_FORMAT.format(LocalDateTime.now()));
        log.info("Type: {}", request.getType());
        log.info("To: {}", request.getTo());
        log.info("Subject: {}", request.getSubject());
        log.info("Template: {}", request.getTemplate());
        if (request.getAdditionalData() != null) {
            log.info("Additional Data:");
            request.getAdditionalData().forEach((key, value) -> 
                log.info("  {} -> {}", key, value));
        }
        log.info("Attempt: {}", ++context.attempts);
        log.info("===============================");
    }

    public void logKafkaMessage(Message<?> message, String stage) {
        log.info("=== Kafka Message Debug [{}] ===", stage);
        log.info("Timestamp: {}", DATE_FORMAT.format(LocalDateTime.now()));
        log.info("Topic: {}", message.getHeaders().get(KafkaHeaders.TOPIC));
        log.info("Partition: {}", message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION));
        log.info("Message Timestamp: {}", message.getHeaders().get(KafkaHeaders.TIMESTAMP));
        log.info("Key: {}", message.getHeaders().get(KafkaHeaders.KEY));
        log.info("Payload Type: {}", message.getPayload().getClass().getSimpleName());
        log.info("===============================");
    }

    public void logEmailAttempt(MimeMessage message, String stage) {
        try {
            log.info("=== Email Attempt Debug [{}] ===", stage);
            log.info("Timestamp: {}", DATE_FORMAT.format(LocalDateTime.now()));
            log.info("To: {}", message.getAllRecipients()[0]);
            log.info("Subject: {}", message.getSubject());
            log.info("===============================");
        } catch (MessagingException e) {
            logError("EMAIL_DEBUG", e, Map.of("stage", stage));
        }
    }

    public void logSMSAttempt(String to, String content, String stage) {
        log.info("=== SMS Attempt Debug [{}] ===", stage);
        log.info("Timestamp: {}", DATE_FORMAT.format(LocalDateTime.now()));
        log.info("To: {}", to);
        log.info("Content Length: {}", content.length());
        log.info("===============================");
    }

    public void logError(String operation, Throwable error, Map<String, Object> context) {
        log.error("=== Error Debug [{}] ===", operation);
        log.error("Timestamp: {}", DATE_FORMAT.format(LocalDateTime.now()));
        log.error("Error Type: {}", error.getClass().getSimpleName());
        log.error("Message: {}", error.getMessage());
        if (context != null) {
            log.error("Context:");
            context.forEach((key, value) -> 
                log.error("  {} -> {}", key, value));
        }
        log.error("Stack Trace:", error);
        log.error("===============================");
    }

    public void logMetrics(String operation, Map<String, Object> metrics) {
        log.info("=== Metrics Debug [{}] ===", operation);
        log.info("Timestamp: {}", DATE_FORMAT.format(LocalDateTime.now()));
        if (metrics != null) {
            metrics.forEach((key, value) -> 
                log.info("  {} -> {}", key, value));
        }
        log.info("===============================");
    }

    private String getContextKey(NotificationRequest request) {
        return String.format("%s:%s", request.getType(), request.getTo());
    }

    private static class DebugContext {
        private final LocalDateTime startTime;
        private final Map<String, Object> metrics;
        private int attempts;
        private String lastError;

        DebugContext() {
            this.startTime = LocalDateTime.now();
            this.metrics = new ConcurrentHashMap<>();
            this.attempts = 0;
        }
    }
}
