package com.winseslas.microservices.bookStore.UserManager.model.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for an error response.
 */
@Getter
@Setter
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
