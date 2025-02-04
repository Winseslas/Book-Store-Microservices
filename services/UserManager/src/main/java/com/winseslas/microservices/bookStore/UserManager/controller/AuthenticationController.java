package com.winseslas.microservices.bookStore.UserManager.controller;

import com.winseslas.microservices.bookStore.UserManager.exception.EmailAlreadyExistsException;
import com.winseslas.microservices.bookStore.UserManager.model.response.*;
import com.winseslas.microservices.bookStore.UserManager.service.AuthenticationService;
import com.winseslas.microservices.bookStore.UserManager.service.PasswordResetService;
import com.winseslas.microservices.bookStore.UserManager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations related to user authentication and account management")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Creates a new user account. Returns an error if the email is already in use.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Email already in use", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.emailExists(request.getEmail())) {
            throw new EmailAlreadyExistsException("This email is already in use");
        }
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Authenticate a user", description = "Authenticates the user with email and password. Returns a JWT token upon success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @Operation(summary = "Confirm user account", description = "Validates an email confirmation token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/confirm-account")
    public ResponseEntity<?> confirmAccount(@RequestParam("token") String token) {
        return ResponseEntity.ok(authenticationService.confirmEmail(token));
    }

    @Operation(summary = "Request password reset", description = "Sends an email with a password reset link.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "404", description = "Email not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        passwordResetService.sendPasswordResetEmail(email);
        return ResponseEntity.ok("Password reset email sent");
    }

    @Operation(summary = "Reset user password", description = "Resets the password using a reset token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(token, request));
    }
}