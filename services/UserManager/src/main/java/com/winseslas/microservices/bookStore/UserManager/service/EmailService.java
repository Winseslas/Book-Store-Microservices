//package com.winseslas.microservices.bookStore.UserManager.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.env.Environment;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
////    private final JavaMailSender mailSender;
//    private final Environment env;
//
//    public void sendConfirmationEmail(String to, String token) {
//        String confirmationUrl = env.getProperty("app.base-url") + "/api/v1/auth/confirm-account?token=" + token;
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Account Confirmation");
//        message.setText("Please click the link below to activate your account:\n" + confirmationUrl);
//
////        mailSender.send(message);
//    }
//
//    /**
//     * Sends a password reset email to the specified email address.
//     * This method constructs a password reset URL using the application's base URL
//     * and the provided token, then sends an email with instructions to reset the password.
//     *
//     * @param to    The email address of the recipient
//     * @param token The unique token associated with the password reset request
//     */
//    public void sendPasswordResetEmail(String to, String token) {
//        String resetUrl = env.getProperty("app.base-url") + "/reset-password?token=" + token;
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Password Reset Request");
//        message.setText("Click the link to reset your password:\n" + resetUrl);
//
////        mailSender.send(message);
//    }
//}