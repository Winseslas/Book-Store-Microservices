package com.winseslas.microservices.bookStore.NotificationManager.service;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import com.winseslas.microservices.bookStore.NotificationManager.exception.NotificationException;
import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationRequest;
import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationType;
import com.winseslas.microservices.bookStore.NotificationManager.monitoring.NotificationMonitor;
import com.winseslas.microservices.bookStore.NotificationManager.util.NotificationDebugger;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationDebugger debugger;
    private final NotificationMonitor monitor;
    private final MeterRegistry meterRegistry;

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @jakarta.annotation.PostConstruct
    public void init() {
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }

    @KafkaListener(topics = "notification-events", groupId = "notification-group")
    public void handleNotification(org.springframework.messaging.Message<NotificationRequest> message) {
        NotificationRequest request = message.getPayload();
        debugger.logKafkaMessage(message, "RECEIVED");

        Instant start = Instant.now();
        try {
            switch (request.getType()) {
                case EMAIL -> sendEmail(request);
                case SMS -> sendSMS(request);
                case PUSH -> sendPushNotification(request);
            }
            long duration = Duration.between(start, Instant.now()).toMillis();
            monitor.recordSuccess(request.getType(), duration);
            meterRegistry.timer("notification.processing", "type", request.getType().toString())
                    .record(duration, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            monitor.recordError(request.getType());
            meterRegistry.counter("notification.errors", "type", request.getType().toString()).increment();
            throw new NotificationException("Failed to send notification", e);
        }
    }

    private void sendEmail(NotificationRequest request) throws MessagingException {
        debugger.logNotificationRequest(request, "EMAIL_START");
        
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        
        String content;
        if (request.getTemplate() != null) {
            content = processTemplate(request.getTemplate(), request.getAdditionalData());
        } else {
            content = request.getContent();
        }
        
        helper.setText(content, true);
        debugger.logEmailAttempt(mimeMessage, "BEFORE_SEND");
        
        Instant start = Instant.now();
        try {
            mailSender.send(mimeMessage);
            long duration = Duration.between(start, Instant.now()).toMillis();
            meterRegistry.timer("email.send.time").record(duration, TimeUnit.MILLISECONDS);
            debugger.logEmailAttempt(mimeMessage, "SENT");
        } catch (Exception e) {
            meterRegistry.counter("email.send.errors").increment();
            debugger.logError("EMAIL_SEND", e, Map.of("recipient", request.getTo()));
            throw new NotificationException("Failed to send email", e);
        }
    }

    private void sendSMS(NotificationRequest request) {
        debugger.logNotificationRequest(request, "SMS_START");
        
        Instant start = Instant.now();
        try {
            com.twilio.rest.api.v2010.account.Message message = com.twilio.rest.api.v2010.account.Message.creator(
                new PhoneNumber(request.getTo()),
                new PhoneNumber(twilioPhoneNumber),
                request.getContent()
            ).create();
            
            long duration = Duration.between(start, Instant.now()).toMillis();
            meterRegistry.timer("sms.send.time").record(duration, TimeUnit.MILLISECONDS);
            debugger.logSMSAttempt(request.getTo(), request.getContent(), "SENT");
            
            if (!message.getStatus().toString().equals("sent")) {
                throw new NotificationException("SMS not sent successfully");
            }
        } catch (Exception e) {
            meterRegistry.counter("sms.send.errors").increment();
            debugger.logError("SMS_SEND", e, Map.of("recipient", request.getTo()));
            throw new NotificationException("Failed to send SMS", e);
        }
    }

    private void sendPushNotification(NotificationRequest request) {
        // Implémentation future pour les notifications push
        throw new UnsupportedOperationException("Push notifications not yet implemented");
    }

    private String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        
        Instant start = Instant.now();
        try {
            String content = templateEngine.process(templateName, context);
            long duration = Duration.between(start, Instant.now()).toMillis();
            meterRegistry.timer("template.processing.time", 
                "template", templateName).record(duration, TimeUnit.MILLISECONDS);
            return content;
        } catch (Exception e) {
            meterRegistry.counter("template.processing.errors",
                "template", templateName).increment();
            throw new NotificationException("Failed to process template", e);
        }
    }
}
