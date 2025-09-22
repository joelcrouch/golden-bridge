package com.goldenbridge.app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String testSecret = "thisIsAVeryLongAndSecureSecretKeyForTestingHS512AlgorithmWhichNeedsAtLeast64Bytes";
    private long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject values into the fields that would normally be @Value injected
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        // Given
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User("testuser", "password", java.util.Collections.emptyList());

        // When
        String token = jwtTokenProvider.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        // Further validation can be done by parsing the token
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void getUsernameFromToken_shouldExtractCorrectUsername() {
        // Given
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User("anotheruser", "password", java.util.Collections.emptyList());
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("anotheruser");
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        // Given
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User("validuser", "password", java.util.Collections.emptyList());
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Given
        // Temporarily set a very short expiration for testing expired token
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 1L); // 1 millisecond
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User("expireduser", "password", java.util.Collections.emptyList());
        String token = jwtTokenProvider.generateToken(userDetails);

        // Wait for token to expire
        try { Thread.sleep(50); } catch (InterruptedException e) { /* ignore */ }

        // When
        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidUser() {
        // Given
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User("user1", "password", java.util.Collections.emptyList());
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        org.springframework.security.core.userdetails.User wrongUserDetails = 
            new org.springframework.security.core.userdetails.User("wronguser", "password", java.util.Collections.emptyList());
        boolean isValid = jwtTokenProvider.validateToken(token, wrongUserDetails);

        // Then
        assertThat(isValid).isFalse();
    }
}
