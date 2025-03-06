package com.winseslas.microservices.bookStore.UserManager.controller;

import com.winseslas.microservices.bookStore.UserManager.model.response.AuthenticationRequest;
import com.winseslas.microservices.bookStore.UserManager.model.response.AuthenticationResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.ErrorResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.PasswordResetRequest;
import com.winseslas.microservices.bookStore.UserManager.model.response.RegisterRequest;
import com.winseslas.microservices.bookStore.UserManager.model.response.RegisterResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.SuccessResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.UnauthorizedResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
  name = "Authentication",
  description = "Operations related to user authentication and account management"
)
public class AuthenticationController {
  private final AuthenticationService authenticationService;
  private final PasswordResetService passwordResetService;
  private final UserService userService;

  @Operation(
    summary = "Register a new user",
    description = "Creates a new user account. Returns an error if the email is already in use."
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "201",
      description = "User registered successfully",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Passwords do not match",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "409",
      description = "Email already in use",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder().message("Passwords do not match").build());
    }

    if (userService.emailExists(request.getEmail())) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.builder().message("This email is already in use").build());
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
  }

  @Operation(
    summary = "Authenticate a user",
    description = "Authenticates the user with email and password. Returns a JWT token upon success."
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Authentication successful",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid email or password",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized - Invalid credentials",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "403",
      description = "Forbidden - Account not activated",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  @PostMapping("/authenticate")
  public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }

  @Operation(
    summary = "Confirm user account",
    description = "Validates an email confirmation token."
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Account confirmed successfully",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid or expired token",
      content = @Content (mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  @GetMapping("/confirm-account")
  public ResponseEntity<?> confirmAccount(@RequestParam("token") String token) {
    return ResponseEntity.ok(authenticationService.confirmEmail(token));
  }

  @Operation(
    summary = "Request password reset",
    description = "Sends an email with a password reset link."
  )
  @ApiResponses(value = {
    @ApiResponse(
        responseCode = "200", 
        description = "Password reset email sent", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))
    ),
    @ApiResponse(
        responseCode = "404", 
        description = "Email not found", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
        responseCode = "500", 
        description = "Internal server error", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@RequestParam String email) {
    passwordResetService.sendPasswordResetEmail(email);
    return ResponseEntity.ok(new SuccessResponse("Password reset email sent"));
  }

  @Operation(
    summary = "Reset user password",
    description = "Resets the password using a reset token."
  )
  @ApiResponses(value = {
    @ApiResponse(
        responseCode = "200", 
        description = "Password reset successfully", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))
    ),
    @ApiResponse(
        responseCode = "400", 
        description = "Invalid or expired token", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
        responseCode = "404", 
        description = "Email not found", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
        responseCode = "500", 
        description = "Internal server error", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestParam String token, @Valid @RequestBody PasswordResetRequest request) {
    return passwordResetService.resetPassword(token, request);
  }

  @Operation(
    summary = "Logout user",
    description = "Invalidates the user's refresh token and logs them out"
  )
  @ApiResponses(value = {
    @ApiResponse(
        responseCode = "200", 
        description = "Successfully logged out",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))
    ),
    @ApiResponse(
        responseCode = "401", 
        description = "Unauthorized", 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedResponse.class))
    ),
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
    return ResponseEntity.ok(authenticationService.logout(token));
  }
}