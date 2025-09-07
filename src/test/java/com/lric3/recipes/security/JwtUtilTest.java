package com.lric3.recipes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;
    private String secret;
    private Long expiration;
    private String validToken;
    private String expiredToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        // Set up test values using ReflectionTestUtils
        secret = "mySecretKey123456789012345678901234567890";
        expiration = 3600000L; // 1 hour in milliseconds
        
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);

        // Create test user
        userDetails = new User("testuser", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Generate valid token for testing
        validToken = jwtUtil.generateToken(userDetails);
        
        // Create expired token (manually create one with past expiration)
        expiredToken = createExpiredToken();
        
        // Create invalid token (malformed JWT)
        invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";
    }

    @Test
    void generateToken_WithValidUserDetails_ShouldReturnValidToken() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT format has dots
        
        // Verify token can be parsed
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void generateToken_WithDifferentUsers_ShouldReturnDifferentTokens() {
        // Given
        UserDetails user1 = new User("user1", "password", Collections.emptyList());
        UserDetails user2 = new User("user2", "password", Collections.emptyList());

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals("user1", jwtUtil.extractUsername(token1));
        assertEquals("user2", jwtUtil.extractUsername(token2));
    }

    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        // When
        String username = jwtUtil.extractUsername(validToken);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        // When & Then
        assertThrows(JwtException.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void extractUsername_WithExpiredToken_ShouldThrowException() {
        // When & Then
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.extractUsername(expiredToken);
        });
    }

    @Test
    void extractExpiration_WithValidToken_ShouldReturnExpirationDate() {
        // When
        Date expirationDate = jwtUtil.extractExpiration(validToken);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date())); // Should be in the future
    }

    @Test
    void extractExpiration_WithInvalidToken_ShouldThrowException() {
        // When & Then
        assertThrows(JwtException.class, () -> {
            jwtUtil.extractExpiration(invalidToken);
        });
    }

    @Test
    void extractClaim_WithValidToken_ShouldReturnClaim() {
        // When
        String subject = jwtUtil.extractClaim(validToken, Claims::getSubject);
        Date issuedAt = jwtUtil.extractClaim(validToken, Claims::getIssuedAt);

        // Then
        assertEquals("testuser", subject);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date())); // Should be in the past
    }

    @Test
    void extractClaim_WithInvalidToken_ShouldThrowException() {
        // When & Then
        assertThrows(JwtException.class, () -> {
            jwtUtil.extractClaim(invalidToken, Claims::getSubject);
        });
    }

    @Test
    void validateToken_WithValidTokenAndUserDetails_ShouldReturnTrue() {
        // When
        Boolean isValid = jwtUtil.validateToken(validToken, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithExpiredTokenAndUserDetails_ShouldThrowException() {
        // When & Then
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(expiredToken, userDetails);
        });
    }

    @Test
    void validateToken_WithValidTokenButWrongUser_ShouldReturnFalse() {
        // Given
        UserDetails wrongUser = new User("wronguser", "password", Collections.emptyList());

        // When
        Boolean isValid = jwtUtil.validateToken(validToken, wrongUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidTokenAndUserDetails_ShouldThrowException() {
        // When & Then
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateToken(invalidToken, userDetails);
        });
    }

    @Test
    void validateToken_WithValidTokenOnly_ShouldReturnTrue() {
        // When
        Boolean isValid = jwtUtil.validateToken(validToken);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithExpiredTokenOnly_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.validateToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidTokenOnly_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.validateToken("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "not.a.valid.jwt";

        // When
        Boolean isValid = jwtUtil.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithWrongSignature_ShouldReturnFalse() {
        // Given
        String wrongSignatureToken = createTokenWithWrongSignature();

        // When
        Boolean isValid = jwtUtil.validateToken(wrongSignatureToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        // When
        Boolean isExpired = ReflectionTestUtils.invokeMethod(jwtUtil, "isTokenExpired", validToken);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldThrowException() {
        // When & Then
        assertThrows(ExpiredJwtException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtUtil, "isTokenExpired", expiredToken);
        });
    }

    @Test
    void getSigningKey_ShouldReturnValidKey() {
        // When
        Object signingKey = ReflectionTestUtils.invokeMethod(jwtUtil, "getSigningKey");

        // Then
        assertNotNull(signingKey);
    }

    @Test
    void createToken_WithValidClaims_ShouldReturnToken() {
        // Given
        String subject = "testuser";

        // When
        String token = ReflectionTestUtils.invokeMethod(jwtUtil, "createToken", 
                new java.util.HashMap<String, Object>(), subject);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }

    @Test
    void extractAllClaims_WithValidToken_ShouldReturnClaims() {
        // When
        Claims claims = ReflectionTestUtils.invokeMethod(jwtUtil, "extractAllClaims", validToken);

        // Then
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractAllClaims_WithInvalidToken_ShouldThrowException() {
        // When & Then
        assertThrows(JwtException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtUtil, "extractAllClaims", invalidToken);
        });
    }

    @Test
    void generateToken_WithNullUserDetails_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            jwtUtil.generateToken(null);
        });
    }

    @Test
    void extractUsername_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractUsername(null);
        });
    }

    @Test
    void extractExpiration_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractExpiration(null);
        });
    }

    @Test
    void extractClaim_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractClaim(null, Claims::getSubject);
        });
    }

    @Test
    void extractClaim_WithNullClaimsResolver_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            jwtUtil.extractClaim(validToken, null);
        });
    }

    @Test
    void validateToken_WithNullUserDetails_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            jwtUtil.validateToken(validToken, null);
        });
    }

    // Helper methods for creating test tokens
    private String createExpiredToken() {
        // Create a token with expiration in the past
        long pastTime = System.currentTimeMillis() - 3600000; // 1 hour ago
        javax.crypto.SecretKey signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes());
        
        return io.jsonwebtoken.Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(pastTime))
                .setExpiration(new Date(pastTime + 1000)) // 1 second after issued
                .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }

    private String createTokenWithWrongSignature() {
        // Create a token with wrong signature
        String wrongSecret = "wrongSecretKey123456789012345678901234567890";
        javax.crypto.SecretKey wrongKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(wrongSecret.getBytes());
        
        return io.jsonwebtoken.Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(wrongKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}
