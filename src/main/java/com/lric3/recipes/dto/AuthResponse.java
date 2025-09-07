package com.lric3.recipes.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private String token;
    public static final String TOKEN_TYPE = "Bearer";
    private LocalDateTime expiresAt;
    private UserDto user;

    public AuthResponse() {}

    // Custom constructor for the specific usage in AuthenticationService
    public AuthResponse(String token, LocalDateTime expiresAt, UserDto user) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.user = user;
    }
}
