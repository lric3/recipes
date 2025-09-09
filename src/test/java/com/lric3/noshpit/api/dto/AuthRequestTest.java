package com.lric3.noshpit.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidAuthRequest() {
        AuthRequest authRequest = new AuthRequest("testuser", "ValidPass123!");
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);
        assertTrue(violations.isEmpty(), "Valid AuthRequest should not have violations");
    }

    @Test
    void testInvalidPassword() {
        AuthRequest authRequest = new AuthRequest("testuser", "weak");
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);
        assertFalse(violations.isEmpty(), "AuthRequest with weak password should have violations");
        
        // Check that the violation is related to password
        boolean hasPasswordViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordViolation, "Should have password validation violation");
    }

    @Test
    void testMissingUsernameOrEmail() {
        AuthRequest authRequest = new AuthRequest("", "ValidPass123!");
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);
        assertFalse(violations.isEmpty(), "AuthRequest with empty username should have violations");
        
        // Check that the violation is related to usernameOrEmail
        boolean hasUsernameViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("usernameOrEmail"));
        assertTrue(hasUsernameViolation, "Should have username validation violation");
    }

    @Test
    void testMissingPassword() {
        AuthRequest authRequest = new AuthRequest("testuser", "");
        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);
        assertFalse(violations.isEmpty(), "AuthRequest with empty password should have violations");
        
        // Check that the violation is related to password
        boolean hasPasswordViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordViolation, "Should have password validation violation");
    }

    @Test
    void testPasswordComplexityRequirements() {
        // Test various invalid passwords
        String[] invalidPasswords = {
            "short",           // Too short
            "nouppercase123!", // No uppercase
            "NOLOWERCASE123!", // No lowercase
            "NoDigits!",       // No digits
            "NoSpecial123",    // No special characters
            "ValidPass123#",   // Invalid special character
        };

        for (String invalidPassword : invalidPasswords) {
            AuthRequest authRequest = new AuthRequest("testuser", invalidPassword);
            Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);
            assertFalse(violations.isEmpty(), 
                "Password '" + invalidPassword + "' should be invalid");
        }
    }

    @Test
    void testValidPasswordVariations() {
        // Test various valid passwords
        String[] validPasswords = {
            "ValidPass123!",
            "MySecure1@",
            "Test123$",
            "Password1%",
            "SecurePass2*",
            "MyPass123?",
            "ValidPass123&",
        };

        for (String validPassword : validPasswords) {
            AuthRequest authRequest = new AuthRequest("testuser", validPassword);
            Set<ConstraintViolation<AuthRequest>> violations = validator.validate(authRequest);
            assertTrue(violations.isEmpty(), 
                "Password '" + validPassword + "' should be valid");
        }
    }
}
