package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.auth.JwtService;
import com.winseslas.microservices.bookStore.UserManager.exception.InvalidTokenException;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
import com.winseslas.microservices.bookStore.UserManager.model.response.AuthenticationRequest;
import com.winseslas.microservices.bookStore.UserManager.model.response.AuthenticationResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.RegisterRequest;
import com.winseslas.microservices.bookStore.UserManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
//    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * Registers a new user in the system.
     * <p>
     * This method creates a new user with the provided registration details,
     * saves the user to the repository, generates a confirmation token, and
     * sends a confirmation email to the user's email address.
     * </p>
     *
     * @param request the registration request containing the user's name, email, and password
     * @return an {@link AuthenticationResponse} containing a message indicating the registration status
     */
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .build();

        userRepository.save(user);

        String confirmationToken = jwtService.generateConfirmationToken(user);
//         emailService.sendConfirmationEmail(user.getEmail(), confirmationToken);

        return AuthenticationResponse.builder()
                .message("Registration successful. Please check your email to activate your account.")
                .build();
    }

    /**
     * Authenticates a user based on the provided authentication request.
     * <p>
     * This method verifies the user's credentials and checks if the account is active.
     * If the authentication is successful and the account is active, a JWT token is generated
     * and returned in the response.
     * </p>
     *
     * @param request the authentication request containing the user's email and password
     * @return an {@link AuthenticationResponse} containing the JWT token if authentication is successful
     * @throws DisabledException if the user's account is not activated
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!user.isActive()) {
            throw new DisabledException("Account not activated");
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Confirms a user's email address using a confirmation token.
     * <p>
     * This method validates the provided token, activates the user's account if the token is valid,
     * and throws an exception if the token is invalid or the user is not found.
     * </p>
     *
     * @param token the confirmation token sent to the user's email
     * @return a String message indicating successful account activation
     * @throws UsernameNotFoundException if no user is found with the email associated with the token
     * @throws InvalidTokenException if the provided token is invalid or expired
     */
    public String confirmEmail(String token) {
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (jwtService.isTokenValid(token, user)) {
            user.setActive(true);
            userRepository.save(user);
            return "Account activated successfully";
        }
        throw new InvalidTokenException("Invalid confirmation token");
    }
}