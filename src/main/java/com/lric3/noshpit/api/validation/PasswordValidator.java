package com.lric3.noshpit.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for password complexity rules.
 * 
 * Validates that a password meets the following requirements:
 * - Minimum 8 characters, maximum 128 characters
 * - At least one lowercase letter (a-z)
 * - At least one uppercase letter (A-Z)
 * - At least one digit (0-9)
 * - At least one special character (@$!%*?&)
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$";
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }
        
        // Check length first
        if (password.length() < 8 || password.length() > 128) {
            return false;
        }
        
        // Check pattern
        return password.matches(PASSWORD_PATTERN);
    }
}
