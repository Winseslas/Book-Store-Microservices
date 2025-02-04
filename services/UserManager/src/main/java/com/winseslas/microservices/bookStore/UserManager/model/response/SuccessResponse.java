package com.winseslas.microservices.bookStore.UserManager.model.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for a successful response.
 *
 * @param <T> The type of the data object in the response.
 */
@Getter
@Setter
public class SuccessResponse<T> {
    private final boolean success = true;
    private T data;

    public SuccessResponse(T data) {
        this.data = data;
    }

}
