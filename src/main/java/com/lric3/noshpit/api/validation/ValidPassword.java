package com.lric3.noshpit.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for password complexity rules.
 * 
 * Password requirements:
 * - Minimum 8 characters, maximum 128 characters
 * - At least one lowercase letter (a-z)
 * - At least one uppercase letter (A-Z)
 * - At least one digit (0-9)
 * - At least one special character (@$!%*?&)
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    
    String message() default "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@$!%*?&)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
