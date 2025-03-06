package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.exception.InvalidTokenException;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
import com.winseslas.microservices.bookStore.UserManager.model.response.ErrorResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.PasswordResetRequest;
import com.winseslas.microservices.bookStore.UserManager.model.response.SuccessResponse;
import com.winseslas.microservices.bookStore.UserManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
//    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String resetToken = jwtService.generatePasswordResetToken(user);
//        emailService.sendPasswordResetEmail(email, resetToken);
    }

    public ResponseEntity<?> resetPassword(String token, PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder().message("Passwords do not match").build());
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        if (!jwtService.isTokenValid(token, user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                .message("Invalid or expired confirmation token").build()
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(SuccessResponse.builder()
            .message("Password reset successfully").build()
        );
    }
}