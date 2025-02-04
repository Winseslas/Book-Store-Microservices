package com.winseslas.microservices.bookStore.UserManager.model.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for an unauthorized response.
 */
@Getter
public class UnauthorizedResponse<T> {
    private final String message = "Unauthorized";
    @Setter
    private T data;

    public UnauthorizedResponse(T data) {
        this.data = data;
    }

}

