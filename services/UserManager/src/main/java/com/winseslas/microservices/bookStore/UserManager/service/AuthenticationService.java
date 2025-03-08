package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
import com.winseslas.microservices.bookStore.UserManager.kafka.AuthEventProducer;
import com.winseslas.microservices.bookStore.UserManager.kafka.NotificationProducer;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.Role;
import com.winseslas.microservices.bookStore.UserManager.model.response.AuthenticationRequest;
import com.winseslas.microservices.bookStore.UserManager.model.response.AuthenticationResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.RegisterRequest;
import com.winseslas.microservices.bookStore.UserManager.model.response.RegisterResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.SuccessResponse;
import com.winseslas.microservices.bookStore.UserManager.repository.RoleRepository;
import com.winseslas.microservices.bookStore.UserManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthEventProducer authEventProducer;
    private final NotificationProducer notificationProducer;
    private static final Log LOG = LogFactory.getLog(AuthenticationService.class);

    /**
     * Registers a new user in the system.
     * <p>
     * This method creates a new user with the provided registration details,
     * saves the user to the repository, generates a confirmation token, and
     * sends a confirmation email to the user's email address.
     * </p>
     *
     * @param request the registration request containing the user's name, email,
     *                and password
     * @return a {@link RegisterResponse} containing the user ID, value, and a message indicating the
     *         registration status
     */
    public RegisterResponse register(RegisterRequest request) {
        try {
            // Create new user
            var user = User.builder()
                    .value(generateString(request.getEmail()))
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .isActive(false)
                    .roles(new HashSet<>())  // Initialize the roles Set
                    .build();

            // Find or create MEMBER role
            Role memberRole = roleRepository.findByValueAndName("MEMBRE", "Membre")
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .value("MEMBRE")
                                .name("Membre")
                                .description("Utilisateur classique qui peut emprunter des livres et suivre son compte.")
                                .isActive(true)
                                .build();
                        return roleRepository.save(newRole);
                    });

            LOG.info("Role found or created: " + memberRole.getName());

            // Add MEMBER role to user
            user.getRoles().add(memberRole);

            // Save user with role
            userRepository.save(user);

            String confirmationToken = jwtService.generateConfirmationToken(user);

            // Send confirmation email

            authEventProducer.publishAuthEvent(
                user.getId().toString(),
                user.getEmail(),
                "REGISTRATION",
                "SUCCESS",
                "User registered successfully"
            );

            notificationProducer.sendRegistrationEmail(
                user.getEmail(),
                user.getName(),
                generateConfirmationLink(confirmationToken)
            );

            return RegisterResponse.builder()
                    .userId(user.getId())
                    .value(user.getValue())
                    .message("Registration successful. Please check your email to activate your account.")
                    .build();
        } catch (Exception e) {
            authEventProducer.publishAuthEvent(
                null,
                request.getEmail(),
                "REGISTRATION",
                "FAILED",
                e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Authenticates a user based on the provided credentials.
     *
     * @param request The authentication request containing the user's email and
     *                password.
     * @return A {@link ResponseEntity} containing:
     *         - A JWT token if authentication is successful.
     *         - An error response if authentication fails.
     *
     *         Possible responses:
     *         - 200 OK: Authentication successful, returns the JWT token.
     *         - 400 BAD REQUEST: Invalid email or password.
     *         - 403 FORBIDDEN: Account is not activated.
     *         - 401 UNAUTHORIZED: Invalid credentials (user not found).
     *         - 500 INTERNAL SERVER ERROR: Unexpected error during authentication.
     *
     * @throws ResponseStatusException If the user is not found.
     */
    public ResponseEntity<?> authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                    request.getPassword()));

            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Invalid credentials"));

            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not activated");
            }

            var jwtToken = jwtService.generateToken(user);

            authEventProducer.publishAuthEvent(
                user.getId().toString(),
                user.getEmail(),
                "LOGIN",
                "SUCCESS",
                "User authenticated successfully"
            );

            if (user.getPhoneNumber() != null) {
                notificationProducer.sendLoginNotificationSMS(
                    user.getPhoneNumber(),
                    user.getName()
                );
            }

            return ResponseEntity.ok(AuthenticationResponse.builder().token(jwtToken).build());

        } catch (BadCredentialsException e) {
            authEventProducer.publishAuthEvent(
                null,
                request.getEmail(),
                "LOGIN",
                "FAILED",
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email or password");
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not activated");
        } catch (Exception e) {
            authEventProducer.publishAuthEvent(
                null,
                request.getEmail(),
                "LOGIN",
                "FAILED",
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during authentication");
        }
    }

    /**
     * Confirms a user's email address using a verification token.
     *
     * @param token The email confirmation token.
     * @return A {@link ResponseEntity} containing:
     *         - A success message if the email is confirmed successfully.
     *         - An error response if the confirmation fails.
     *
     *         Possible responses:
     *         - 200 OK: Email confirmed successfully, account activated.
     *         - 400 BAD REQUEST: Invalid or expired confirmation token.
     *         - 404 NOT FOUND: User not found.
     *         - 500 INTERNAL SERVER ERROR: Unexpected error during email
     *         confirmation.
     *
     * @throws ResponseStatusException If the user is not found.
     */
    public ResponseEntity<?> confirmEmail(String token) {
        try {
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User not found"));

            if (!jwtService.isTokenValid(token, user)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or expired confirmation token");
            }

            user.setActive(true);
            userRepository.save(user);

            authEventProducer.publishAuthEvent(
                user.getId().toString(),
                user.getEmail(),
                "EMAIL_CONFIRMATION",
                "SUCCESS",
                "Email confirmed successfully"
            );

            return ResponseEntity.ok("Account activated successfully");
        } catch (ResponseStatusException e) {
            authEventProducer.publishAuthEvent(
                null,
                null,
                "EMAIL_CONFIRMATION",
                "FAILED",
                e.getMessage()
            );
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            authEventProducer.publishAuthEvent(
                null,
                null,
                "EMAIL_CONFIRMATION",
                "FAILED",
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while confirming the email");
        }
    }

    /**
     * Logs out a user by invalidating their refresh token.
     * <p>
     * This method extracts the user information from the JWT token,
     * finds the user in the database, and deletes their refresh token.
     * </p>
     *
     * @param token the JWT token from the Authorization header (with "Bearer "
     *              prefix)
     * @return a message indicating successful logout
     * @throws ResponseStatusException if no user is found with the email from the
     *                                 token
     */
    public String logout(String token) {
        String jwt = token.substring(7); // Remove "Bearer " prefix
        String userEmail = jwtService.extractUsername(jwt);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Invalidate the refresh token
        refreshTokenService.deleteByUserId(user.getId());

        authEventProducer.publishAuthEvent(
            user.getId().toString(),
            user.getEmail(),
            "LOGOUT",
            "SUCCESS",
            "User logged out successfully"
        );

        return "Successfully logged out";
    }

    /**
     * Generates a pseudo-random string based on the given email after removing the
     * '@' symbol.
     *
     * @param email The email to transform.
     * @return A pseudo-random string derived from the modified email.
     * @throws IllegalArgumentException If the email is null or does not contain
     *                                  '@'.
     */
    public String generateString(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Remove the first occurrence of '@' from the email
        email = email.replaceFirst("@", "");

        // Generate a pseudo-random value based on the modified email
        return generateHash(email, 6).toUpperCase(); // Generates a 6-character string
    }

    /**
     * Generates a hexadecimal string based on the SHA-256 hash of the given input.
     *
     * @param input  The input text to hash.
     * @param length The desired length of the resulting string.
     * @return A substring of the hash in hexadecimal format.
     */
    private String generateHash(String input, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert the first bytes of the hash into a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < length; i++) {
                hexString.append(Integer.toHexString(Byte.toUnsignedInt(hash[i]) % 16));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    private String generateConfirmationLink(String token) {
        return String.format("http://localhost:8080/api/v1/auth/confirm-account?token=%s", token);
    }

    private String generatePasswordResetLink(String token) {
        return String.format("http://localhost:8080/api/v1/auth/reset-password?token=%s", token);
    }
}