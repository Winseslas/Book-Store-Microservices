package com.winseslas.microservices.bookStore.UserManager.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION_MS = 86400000; // 24h in milliseconds
    private static final long CONFIRMATION_EXPIRATION_MS = 900000; // 15m
    private static final long PASSWORD_RESET_EXPIRATION_MS = 1800000; // 30m

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, JWT_EXPIRATION_MS);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, JWT_EXPIRATION_MS);
    }

    public String generateConfirmationToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, CONFIRMATION_EXPIRATION_MS);
    }

    public String generatePasswordResetToken(UserDetails userDetails) {
        return buildToken(
                Map.of("password_reset", true),
                userDetails,
                PASSWORD_RESET_EXPIRATION_MS
        );
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expiration) {
        final Date now = new Date();

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isPasswordResetToken(String token) {
        return Boolean.TRUE.equals(extractClaim(token, c -> c.get("password_reset", Boolean.class)));
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }
}