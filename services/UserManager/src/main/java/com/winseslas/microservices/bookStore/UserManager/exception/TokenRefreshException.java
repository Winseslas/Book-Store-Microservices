package com.winseslas.microservices.bookStore.UserManager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Exception thrown when there is an issue with refreshing the JWT token.
 * This could be due to an expired or invalid refresh token.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * -- GETTER --
     *  Gets the token that caused the exception.
     *
     */
    @Getter
    private final String token;
    private final String message;

    /**
     * Constructs a new TokenRefreshException with the specified token and message.
     *
     * @param token the refresh token that caused the exception
     * @param message the detail message explaining the reason for the exception
     */
    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
        this.token = token;
        this.message = message;
    }

    /**
     * Gets the detail message explaining the reason for the exception.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return message;
    }
}
