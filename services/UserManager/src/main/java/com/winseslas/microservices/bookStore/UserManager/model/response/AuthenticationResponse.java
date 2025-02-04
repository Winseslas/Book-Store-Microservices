package com.winseslas.microservices.bookStore.UserManager.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private Long userId;
    private String email;
    private String token;
    private String message;

    public static AuthenticationResponse of(String token, String email, Long userId) {
        return builder()
                .email(email)
                .token(token)
                .userId(userId)
                .build();
    }

    public static AuthenticationResponse message(String message) {
        return builder().message(message).build();
    }
}