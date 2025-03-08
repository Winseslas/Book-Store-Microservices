package com.winseslas.microservices.bookStore.NotificationManager.integration;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationRequest;
import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationType;
import com.winseslas.microservices.bookStore.NotificationManager.monitoring.NotificationMonitor;
import com.winseslas.microservices.bookStore.NotificationManager.service.NotificationService;
import com.winseslas.microservices.bookStore.NotificationManager.util.NotificationDebugger;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 3, topics = {"notification-events"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.kafka.consumer.group-id=notification-group",
    "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
    "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
    "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
    "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
    "spring.kafka.consumer.properties.spring.json.trusted.packages=com.winseslas.microservices.bookStore.NotificationManager.model",
    "twilio.account-sid=test-sid",
    "twilio.auth-token=test-token",
    "twilio.phone-number=+1234567890",
    "alert.admin.email=admin@example.com",
    "alert.slack.webhook=http://example.com/webhook"
})
class NotificationIntegrationTest {

    @Autowired
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private TemplateEngine templateEngine;

    @MockBean
    private NotificationDebugger debugger;

    @MockBean
    private NotificationMonitor monitor;

    @MockBean
    private MeterRegistry meterRegistry;

    @MockBean
    private MimeMessage mimeMessage;

    @Autowired
    private NotificationService notificationService;

    @Test
    void whenSendingNotificationViaKafka_thenEmailIsSent() throws Exception {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("userName", "John Doe");
        data.put("confirmationLink", "http://example.com/confirm");

        NotificationRequest request = NotificationRequest.builder()
                .to("test@example.com")
                .subject("Test Integration")
                .content("Test content")
                .type(NotificationType.EMAIL)
                .template("registration-confirmation")
                .additionalData(data)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test content</html>");

        // Act
        kafkaTemplate.send("notification-events", request);

        // Assert - vérifie que l'email est envoyé dans les 5 secondes
        verify(mailSender, timeout(5000)).send((MimeMessage) any());
        verify(monitor, timeout(5000)).recordSuccess(eq(NotificationType.EMAIL), anyLong());
        verify(debugger, timeout(5000)).logKafkaMessage(any(), eq("RECEIVED"));
    }

    @Test
    void whenSendingSMSNotificationViaKafka_thenSMSIsLogged() throws Exception {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .to("+1234567890")
                .content("Test SMS content")
                .type(NotificationType.SMS)
                .build();

        Message mockedMessage = mock(Message.class);
        MessageCreator messageCreator = mock(MessageCreator.class);
        when(messageCreator.create()).thenReturn(mockedMessage);
        when(mockedMessage.getStatus()).thenReturn(Message.Status.SENT);

        try (MockedStatic<Message> mockedMessageClass = mockStatic(Message.class)) {
            mockedMessageClass.when(() -> Message.creator(
                    any(PhoneNumber.class),
                    any(PhoneNumber.class),
                    anyString()
            )).thenReturn(messageCreator);

            // Act
            kafkaTemplate.send("notification-events", request);

            // Assert
            verify(monitor, timeout(5000)).recordSuccess(eq(NotificationType.SMS), anyLong());
            verify(debugger, timeout(5000)).logKafkaMessage(any(), eq("RECEIVED"));
            verify(debugger, timeout(5000)).logSMSAttempt(eq("+1234567890"), eq("Test SMS content"), eq("SENT"));
            verify(messageCreator, timeout(5000)).create();
        }
    }
}
