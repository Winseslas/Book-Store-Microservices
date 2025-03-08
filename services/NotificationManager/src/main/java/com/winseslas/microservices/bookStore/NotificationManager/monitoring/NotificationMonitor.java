package com.winseslas.microservices.bookStore.NotificationManager.monitoring;

import com.winseslas.microservices.bookStore.NotificationManager.model.NotificationType;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMonitor {

    private final MeterRegistry meterRegistry;
    private final Map<NotificationType, NotificationMetrics> metricsMap = new ConcurrentHashMap<>();

    public void recordSuccess(NotificationType type, long duration) {
        NotificationMetrics metrics = metricsMap.computeIfAbsent(type, k -> new NotificationMetrics());
        metrics.recordSuccess(duration);
        
        meterRegistry.counter("notification.success", "type", type.toString()).increment();
        meterRegistry.timer("notification.duration", "type", type.toString())
                    .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void recordError(NotificationType type) {
        NotificationMetrics metrics = metricsMap.computeIfAbsent(type, k -> new NotificationMetrics());
        metrics.recordError();
        
        meterRegistry.counter("notification.errors", "type", type.toString()).increment();
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void checkHealth() {
        metricsMap.forEach((type, metrics) -> {
            double errorRate = metrics.getErrorRate();
            double avgDuration = metrics.getAverageDuration();
            
            log.info("Health check for {}: Error rate = {}%, Avg duration = {}ms", 
                    type, errorRate * 100, avgDuration);
            
            if (errorRate > 0.1) { // More than 10% errors
                log.warn("High error rate for {}: {}%", type, errorRate * 100);
            }
            
            if (avgDuration > 5000) { // More than 5 seconds
                log.warn("High average duration for {}: {}ms", type, avgDuration);
            }
        });
    }

    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        metricsMap.forEach((type, metrics) -> {
            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("success_count", metrics.getSuccessCount());
            typeStats.put("error_count", metrics.getErrorCount());
            typeStats.put("avg_duration_ms", metrics.getAverageDuration());
            typeStats.put("error_rate", metrics.getErrorRate());
            
            stats.put(type.toString(), typeStats);
        });
        
        return stats;
    }

    private static class NotificationMetrics {
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger errorCount = new AtomicInteger(0);
        private final AtomicLong totalDuration = new AtomicLong(0);

        public void recordSuccess(long duration) {
            successCount.incrementAndGet();
            totalDuration.addAndGet(duration);
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getErrorCount() {
            return errorCount.get();
        }

        public double getAverageDuration() {
            int total = successCount.get();
            return total > 0 ? (double) totalDuration.get() / total : 0;
        }

        public double getErrorRate() {
            int total = successCount.get() + errorCount.get();
            return total > 0 ? (double) errorCount.get() / total : 0;
        }
    }
}
