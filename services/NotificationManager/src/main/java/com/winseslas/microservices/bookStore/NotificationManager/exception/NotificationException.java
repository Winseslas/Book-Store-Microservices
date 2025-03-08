package com.winseslas.microservices.bookStore.NotificationManager.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {
    private final NotificationErrorType errorType;
    private final String recipient;
    private final String details;

    public NotificationException(String message) {
        super(message);
        this.errorType = NotificationErrorType.UNKNOWN;
        this.recipient = null;
        this.details = null;
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = NotificationErrorType.UNKNOWN;
        this.recipient = null;
        this.details = null;
    }

    public NotificationException(NotificationErrorType errorType, String recipient, String message, String details) {
        super(message);
        this.errorType = errorType;
        this.recipient = recipient;
        this.details = details;
    }

    public NotificationException(NotificationErrorType errorType, String recipient, String message, String details, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.recipient = recipient;
        this.details = details;
    }

    public enum NotificationErrorType {
        KAFKA_CONNECTION_ERROR,
        KAFKA_SERIALIZATION_ERROR,
        EMAIL_AUTHENTICATION_ERROR,
        EMAIL_SEND_ERROR,
        SMS_AUTHENTICATION_ERROR,
        SMS_SEND_ERROR,
        TEMPLATE_ERROR,
        INVALID_RECIPIENT,
        CONFIGURATION_ERROR,
        UNKNOWN
    }
}
