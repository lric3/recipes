package com.lric3.noshpit.api.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidPassword() {
        TestPasswordClass testObj = new TestPasswordClass("ValidPass123!");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertTrue(violations.isEmpty(), "Valid password should not have violations");
    }

    @Test
    void testPasswordTooShort() {
        TestPasswordClass testObj = new TestPasswordClass("Ab1!");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty(), "Short password should have violations");
        assertTrue(violations.iterator().next().getMessage().contains("lowercase letter"));
    }

    @Test
    void testPasswordTooLong() {
        String longPassword = "A".repeat(129) + "b1!";
        TestPasswordClass testObj = new TestPasswordClass(longPassword);
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty(), "Long password should have violations");
    }

    @Test
    void testPasswordMissingLowercase() {
        TestPasswordClass testObj = new TestPasswordClass("VALIDPASS123!");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty(), "Password without lowercase should have violations");
        assertTrue(violations.iterator().next().getMessage().contains("lowercase letter"));
    }

    @Test
    void testPasswordMissingUppercase() {
        TestPasswordClass testObj = new TestPasswordClass("validpass123!");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty(), "Password without uppercase should have violations");
        assertTrue(violations.iterator().next().getMessage().contains("uppercase letter"));
    }

    @Test
    void testPasswordMissingDigit() {
        TestPasswordClass testObj = new TestPasswordClass("ValidPassword!");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty(), "Password without digit should have violations");
        assertTrue(violations.iterator().next().getMessage().contains("digit"));
    }

    @Test
    void testPasswordMissingSpecialCharacter() {
        TestPasswordClass testObj = new TestPasswordClass("ValidPassword123");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty(), "Password without special character should have violations");
        assertTrue(violations.iterator().next().getMessage().contains("special character"));
    }

    @Test
    void testPasswordWithInvalidSpecialCharacter() {
        TestPasswordClass testObj = new TestPasswordClass("ValidPass123#");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty(), "Password with invalid special character should have violations");
    }

    @Test
    void testNullPassword() {
        TestPasswordClass testObj = new TestPasswordClass(null);
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertTrue(violations.isEmpty(), "Null password should be handled by @NotBlank, not @ValidPassword");
    }

    @Test
    void testEmptyPassword() {
        TestPasswordClass testObj = new TestPasswordClass("");
        Set<ConstraintViolation<TestPasswordClass>> violations = validator.validate(testObj);
        assertTrue(violations.isEmpty(), "Empty password should be handled by @NotBlank, not @ValidPassword");
    }

    // Helper class for testing
    private static class TestPasswordClass {
        @ValidPassword
        private String password;

        public TestPasswordClass(String password) {
            this.password = password;
        }
    }
}
