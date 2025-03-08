package com.winseslas.microservices.bookStore.UserManager.kafka;

import com.winseslas.microservices.bookStore.UserManager.model.request.NotificationRequest;
import com.winseslas.microservices.bookStore.UserManager.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "notification-events";

    public void sendRegistrationEmail(String email, String userName, String confirmationLink) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("userName", userName);
        additionalData.put("confirmationLink", confirmationLink);

        NotificationRequest request = NotificationRequest.builder()
                .to(email)
                .subject("Confirmation de votre inscription")
                .type(NotificationType.EMAIL)
                .template("registration-confirmation")
                .additionalData(additionalData)
                .build();

        kafkaTemplate.send(TOPIC, email, request)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Registration notification sent successfully to {}", email);
                    } else {
                        log.error("Failed to send registration notification to {}: {}", email, ex.getMessage());
                    }
                });
    }

    public void sendLoginNotificationSMS(String phoneNumber, String userName) {
        NotificationRequest request = NotificationRequest.builder()
                .to(phoneNumber)
                .content("Bonjour " + userName + ", une nouvelle connexion a été détectée sur votre compte.")
                .type(NotificationType.SMS)
                .build();

        kafkaTemplate.send(TOPIC, phoneNumber, request)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Login SMS notification sent successfully to {}", phoneNumber);
                    } else {
                        log.error("Failed to send login SMS notification to {}: {}", phoneNumber, ex.getMessage());
                    }
                });
    }

    public void sendPasswordResetEmail(String email, String userName, String resetLink) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("userName", userName);
        additionalData.put("resetLink", resetLink);

        NotificationRequest request = NotificationRequest.builder()
                .to(email)
                .subject("Réinitialisation de votre mot de passe")
                .type(NotificationType.EMAIL)
                .template("password-reset")
                .additionalData(additionalData)
                .build();

        kafkaTemplate.send(TOPIC, email, request)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Password reset notification sent successfully to {}", email);
                    } else {
                        log.error("Failed to send password reset notification to {}: {}", email, ex.getMessage());
                    }
                });
    }
}
