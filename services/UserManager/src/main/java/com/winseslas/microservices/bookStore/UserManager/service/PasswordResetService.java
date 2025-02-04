package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.auth.JwtService;
import com.winseslas.microservices.bookStore.UserManager.exception.InvalidTokenException;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
import com.winseslas.microservices.bookStore.UserManager.model.response.PasswordResetRequest;
import com.winseslas.microservices.bookStore.UserManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public String resetPassword(String token, PasswordResetRequest request) {
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtService.isTokenValid(token, user)) {
            throw new InvalidTokenException("Invalid reset token");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password reset successfully";
    }
}