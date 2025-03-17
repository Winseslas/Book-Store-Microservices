package com.winseslas.microservices.bookstore.UserManager.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse {
    private String message;

    public static SuccessResponse message(String message) {
        return builder().message(message).build();
    }
}
