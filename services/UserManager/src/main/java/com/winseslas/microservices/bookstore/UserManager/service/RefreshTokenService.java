package com.winseslas.microservices.bookstore.UserManager.service;

import com.winseslas.microservices.bookstore.UserManager.config.JwtConfig;
import com.winseslas.microservices.bookstore.UserManager.exception.TokenRefreshException;
import com.winseslas.microservices.bookstore.UserManager.model.entity.RefreshToken;
import com.winseslas.microservices.bookstore.UserManager.model.entity.User;
import com.winseslas.microservices.bookstore.UserManager.repository.RefreshTokenRepository;
import com.winseslas.microservices.bookstore.UserManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.findById(Math.toIntExact(userId))
                        .orElseThrow(() -> new RuntimeException("User not found")))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtConfig.getRefreshTokenExpiration()))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
