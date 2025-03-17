package com.winseslas.microservices.bookstore.UserManager.model.response;

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
    private String token;
    private String refreshToken;
    private String message;
}
