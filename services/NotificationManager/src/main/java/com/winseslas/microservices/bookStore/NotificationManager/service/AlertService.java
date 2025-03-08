package com.winseslas.microservices.bookStore.NotificationManager.service;

import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationType;
import com.winseslas.microservices.bookStore.NotificationManager.util.NotificationDebugger;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final JavaMailSender mailSender;
    private final NotificationDebugger debugger;
    private final MeterRegistry meterRegistry;
    private final RestTemplate restTemplate;

    @Value("${alert.admin.email}")
    private String adminEmail;

    @Value("${alert.slack.webhook:}")
    private String slackWebhook;

    private final Map<String, AtomicInteger> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAlertSent = new ConcurrentHashMap<>();

    public enum AlertLevel {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    @Async
    public void sendAlert(String message, AlertLevel level, Map<String, Object> context) {
        String alertKey = generateAlertKey(message, level);
        AtomicInteger counter = errorCounters.computeIfAbsent(alertKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        // Vérifier si nous devons envoyer une alerte (éviter le spam)
        if (shouldSendAlert(alertKey, currentCount, level)) {
            // Enregistrer le moment de l'envoi de l'alerte
            lastAlertSent.put(alertKey, LocalDateTime.now());

            // Préparer le contexte d'alerte pour le logging
            Map<String, Object> alertContext = new HashMap<>(context);
            alertContext.put("alert_level", level);
            alertContext.put("alert_count", currentCount);
            alertContext.put("message", message);

            // Envoyer l'alerte via différents canaux selon le niveau
            switch (level) {
                case INFO -> debugger.logMetrics("ALERT_INFO", alertContext);
                case WARNING -> {
                    debugger.logMetrics("ALERT_WARNING", alertContext);
                    sendSlackAlert(message, alertContext);
                }
                case ERROR, CRITICAL -> {
                    debugger.logMetrics("ALERT_" + level, alertContext);
                    sendSlackAlert(message, alertContext);
                    sendEmailAlert(message, alertContext);
                    if (level == AlertLevel.CRITICAL) {
                        sendPagerDutyAlert(message, alertContext);
                    }
                }
            }

            // Enregistrer la métrique d'alerte
            meterRegistry.counter("notification.alerts",
                "level", level.toString(),
                "type", context.getOrDefault("type", "UNKNOWN").toString()
            ).increment();
        }
    }

    public void monitorServiceHealth(Map<String, Object> stats) {
        stats.forEach((type, typeStats) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> metrics = (Map<String, Object>) typeStats;
            double successCount = (double) metrics.get("success_count");
            double failureCount = (double) metrics.get("failure_count");
            double avgDuration = (double) metrics.get("avg_duration_ms");
            
            // Calculer le taux de succès
            double successRate = successCount / (successCount + failureCount) * 100;
            
            // Définir le niveau d'alerte en fonction des métriques
            AlertLevel level = determineAlertLevel(successRate, avgDuration, failureCount);
            
            if (level != AlertLevel.INFO) {
                Map<String, Object> alertContext = Map.of(
                    "type", type,
                    "successRate", String.format("%.2f%%", successRate),
                    "avgDuration", String.format("%.2fms", avgDuration),
                    "failureCount", String.format("%.0f", failureCount)
                );
                
                String message = String.format(
                    "Service health alert for %s: Success rate=%.2f%%, Avg duration=%.2fms, Failures=%.0f",
                    type, successRate, avgDuration, failureCount
                );
                
                sendAlert(message, level, alertContext);
            }
        });
    }

    public void monitorResourceUsage(NotificationType type, double cpuUsage, double memoryUsage) {
        Map<String, Object> context = new HashMap<>();
        context.put("type", type);
        context.put("cpu_usage", cpuUsage);
        context.put("memory_usage", memoryUsage);

        AlertLevel level = AlertLevel.INFO;
        if (cpuUsage > 90 || memoryUsage > 90) {
            level = AlertLevel.CRITICAL;
        } else if (cpuUsage > 80 || memoryUsage > 80) {
            level = AlertLevel.ERROR;
        } else if (cpuUsage > 70 || memoryUsage > 70) {
            level = AlertLevel.WARNING;
        }

        if (level != AlertLevel.INFO) {
            String message = String.format(
                "High resource usage for %s notifications: CPU=%.2f%%, Memory=%.2f%%",
                type, cpuUsage, memoryUsage);

            sendAlert(message, level, context);
        }
    }

    private AlertLevel determineAlertLevel(double successRate, double avgDuration, double failureCount) {
        if (successRate < 70 || avgDuration > 10000 || failureCount > 100) {
            return AlertLevel.CRITICAL;
        } else if (successRate < 80 || avgDuration > 7000 || failureCount > 50) {
            return AlertLevel.ERROR;
        } else if (successRate < 90 || avgDuration > 5000 || failureCount > 20) {
            return AlertLevel.WARNING;
        }
        return AlertLevel.INFO;
    }

    private String generateAlertKey(String message, AlertLevel level) {
        return level + ":" + message.substring(0, Math.min(50, message.length()));
    }

    private boolean shouldSendAlert(String alertKey, int currentCount, AlertLevel level) {
        LocalDateTime lastAlert = lastAlertSent.get(alertKey);
        if (lastAlert == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastAlert = java.time.Duration.between(lastAlert, now).toMinutes();

        // Définir la fréquence des alertes en fonction du niveau
        return switch (level) {
            case INFO -> minutesSinceLastAlert >= 60;
            case WARNING -> minutesSinceLastAlert >= 30;
            case ERROR -> minutesSinceLastAlert >= 15;
            case CRITICAL -> minutesSinceLastAlert >= 5;
        };
    }

    private void sendEmailAlert(String message, Map<String, Object> context) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(adminEmail);
        mailMessage.setSubject("Notification Service Alert");
        mailMessage.setText(formatAlertMessage(message, context));
        
        try {
            mailSender.send(mailMessage);
            meterRegistry.counter("alert.email.sent").increment();
        } catch (Exception e) {
            log.error("Failed to send email alert", e);
            meterRegistry.counter("alert.email.errors").increment();
        }
    }

    private void sendSlackAlert(String message, Map<String, Object> context) {
        if (slackWebhook == null || slackWebhook.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> slackMessage = formatSlackMessage(message, context);
            restTemplate.postForEntity(slackWebhook, slackMessage, String.class);
            meterRegistry.counter("alert.slack.sent").increment();
        } catch (Exception e) {
            log.error("Failed to send Slack alert", e);
            meterRegistry.counter("alert.slack.errors").increment();
        }
    }

    private void sendPagerDutyAlert(String message, Map<String, Object> context) {
        // Implémentation à venir pour PagerDuty
        log.warn("PagerDuty integration not implemented yet");
    }

    private String formatAlertMessage(String message, Map<String, Object> context) {
        StringBuilder builder = new StringBuilder();
        builder.append(message).append("\n\nContext:\n");
        
        context.forEach((key, value) -> 
            builder.append(key).append(": ").append(value).append("\n"));
        
        return builder.toString();
    }

    private Map<String, Object> formatSlackMessage(String message, Map<String, Object> context) {
        Map<String, Object> slackMessage = new HashMap<>();
        slackMessage.put("text", message);
        
        // Ajouter les détails du contexte comme des champs attachés
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", getSlackColor(context));
        attachment.put("fields", formatSlackFields(context));
        
        slackMessage.put("attachments", List.of(attachment));
        return slackMessage;
    }

    private String getSlackColor(Map<String, Object> context) {
        return switch ((AlertLevel) context.get("alert_level")) {
            case INFO -> "#36a64f";
            case WARNING -> "#ffcc00";
            case ERROR -> "#ff9900";
            case CRITICAL -> "#ff0000";
        };
    }

    private List<Map<String, Object>> formatSlackFields(Map<String, Object> context) {
        return context.entrySet().stream()
            .map(entry -> {
                Map<String, Object> field = new HashMap<>();
                field.put("title", entry.getKey());
                field.put("value", entry.getValue().toString());
                field.put("short", true);
                return field;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
