package com.lric3.recipes.controller;

import com.lric3.recipes.dto.AuthRequest;
import com.lric3.recipes.dto.AuthResponse;
import com.lric3.recipes.dto.UserDto;
import com.lric3.recipes.dto.UserRegistrationDto;
import com.lric3.recipes.service.AuthenticationService;
import com.lric3.recipes.service.UserContextService;
import com.lric3.recipes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final UserService userService;

    private final UserContextService userContextService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authenticationService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserDto user = userService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<UserDto> getCurrentUser() {
        String username = userContextService.getCurrentUsername();
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
}
