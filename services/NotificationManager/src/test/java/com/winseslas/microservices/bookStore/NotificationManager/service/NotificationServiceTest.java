package com.winseslas.microservices.bookStore.NotificationManager.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationRequest;
import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationType;
import com.winseslas.microservices.bookStore.NotificationManager.monitoring.NotificationMonitor;
import com.winseslas.microservices.bookStore.NotificationManager.util.NotificationDebugger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private NotificationDebugger debugger;

    @Mock
    private NotificationMonitor monitor;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Timer timer;

    @Mock
    private Counter counter;

    @Mock
    private Message twilioMessage;

    @Mock
    private MessageCreator messageCreator;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.timer(anyString(), any(String[].class))).thenReturn(timer);
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        notificationService = new NotificationService(mailSender, templateEngine, debugger, monitor, meterRegistry);
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "+1234567890");
    }

    @Test
    void handleNotification_ProcessesEmailCorrectly() throws MessagingException {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .type(NotificationType.EMAIL)
                .build();

        Map<String, Object> headers = new HashMap<>();
        headers.put(KafkaHeaders.RECEIVED_PARTITION, 0);
        MessageHeaders messageHeaders = new MessageHeaders(headers);
        org.springframework.messaging.Message<NotificationRequest> message = 
            new GenericMessage<>(request, messageHeaders);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        notificationService.handleNotification(message);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
        verify(monitor).recordSuccess(eq(NotificationType.EMAIL), anyLong());
        verify(debugger).logKafkaMessage(eq(message), eq("RECEIVED"));
        verify(debugger).logNotificationRequest(eq(request), eq("EMAIL_START"));
        verify(debugger).logEmailAttempt(any(), eq("SENT"));
    }

    @Test
    void handleNotification_ProcessesSMSCorrectly() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .to("+1234567890")
                .content("Test SMS")
                .type(NotificationType.SMS)
                .build();

        Map<String, Object> headers = new HashMap<>();
        headers.put(KafkaHeaders.RECEIVED_PARTITION, 0);
        MessageHeaders messageHeaders = new MessageHeaders(headers);
        org.springframework.messaging.Message<NotificationRequest> message = 
            new GenericMessage<>(request, messageHeaders);

        try (MockedStatic<Message> mockedMessage = mockStatic(Message.class)) {
            when(messageCreator.create()).thenReturn(twilioMessage);
            when(twilioMessage.getStatus()).thenReturn(Message.Status.SENT);
            mockedMessage.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    anyString()
            )).thenReturn(messageCreator);

            // Act
            notificationService.handleNotification(message);

            // Assert
            verify(monitor).recordSuccess(eq(NotificationType.SMS), anyLong());
            verify(debugger).logKafkaMessage(eq(message), eq("RECEIVED"));
            verify(debugger).logNotificationRequest(eq(request), eq("SMS_START"));
            verify(debugger).logSMSAttempt(eq("+1234567890"), eq("Test SMS"), eq("SENT"));
        }
    }

    @Test
    void handleNotification_WithTemplate_ProcessesCorrectly() throws MessagingException {
        // Arrange
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("userName", "John");
        templateData.put("confirmationLink", "http://example.com/confirm");

        NotificationRequest request = NotificationRequest.builder()
                .to("test@example.com")
                .subject("Welcome")
                .template("welcome-template")
                .additionalData(templateData)
                .type(NotificationType.EMAIL)
                .build();

        Map<String, Object> headers = new HashMap<>();
        headers.put(KafkaHeaders.RECEIVED_PARTITION, 0);
        MessageHeaders messageHeaders = new MessageHeaders(headers);
        org.springframework.messaging.Message<NotificationRequest> message = 
            new GenericMessage<>(request, messageHeaders);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-template"), any(Context.class)))
                .thenReturn("<html>Welcome John!</html>");

        // Act
        notificationService.handleNotification(message);

        // Assert
        verify(templateEngine).process(eq("welcome-template"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
        verify(monitor).recordSuccess(eq(NotificationType.EMAIL), anyLong());
        verify(debugger).logKafkaMessage(eq(message), eq("RECEIVED"));
        verify(debugger).logNotificationRequest(eq(request), eq("EMAIL_START"));
        verify(debugger).logEmailAttempt(any(), eq("SENT"));
    }
}