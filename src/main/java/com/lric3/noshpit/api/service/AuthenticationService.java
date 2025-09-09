package com.lric3.noshpit.api.service;

import com.lric3.noshpit.api.dto.AuthRequest;
import com.lric3.noshpit.api.dto.AuthResponse;
import com.lric3.noshpit.api.dto.UserDto;
import com.lric3.noshpit.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UserService userService;

    public AuthResponse authenticate(AuthRequest authRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsernameOrEmail(),
                        authRequest.getPassword()
                )
        );

        // Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        // Get token expiration
        Date expirationDate = jwtUtil.extractExpiration(token);
        LocalDateTime expiresAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Get user details
        UserDto user = userService.getUserByUsername(userDetails.getUsername());

        return new AuthResponse(token, expiresAt, user);
    }
}
