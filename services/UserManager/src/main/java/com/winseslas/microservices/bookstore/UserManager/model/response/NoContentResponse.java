package com.winseslas.microservices.bookstore.UserManager.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoContentResponse {
    private String message;

    public static NoContentResponse message(String message) {
        return builder().message(message).build();
    }
}
