package com.lric3.noshpit.api.controller;

import com.lric3.noshpit.api.dto.AuthRequest;
import com.lric3.noshpit.api.dto.AuthResponse;
import com.lric3.noshpit.api.dto.UserDto;
import com.lric3.noshpit.api.dto.UserRegistrationDto;
import com.lric3.noshpit.api.util.TestConstants;
import com.lric3.noshpit.api.service.AuthenticationService;
import com.lric3.noshpit.api.service.UserContextService;
import com.lric3.noshpit.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static com.lric3.noshpit.api.dto.AuthResponse.TOKEN_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private AuthenticationController authenticationController;


    // Direct unit tests (without web layer)
    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Given
        AuthRequest authRequest = new AuthRequest("testUser", TestConstants.TEST_USER_PASSWORD);
        UserDto userDto = createTestUserDto();
        AuthResponse authResponse = new AuthResponse("jwt-token", LocalDateTime.now().plusHours(1), userDto);

        when(authenticationService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authenticationController.login(authRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("Bearer", TOKEN_TYPE);
        assertEquals("testUser", response.getBody().getUser().getUsername());
        assertEquals("test@example.com", response.getBody().getUser().getEmail());

        verify(authenticationService, times(1)).authenticate(authRequest);
    }

    @Test
    void register_WithValidData_ShouldReturnCreatedUser() {
        // Given
        UserRegistrationDto registrationDto = createTestUserRegistrationDto();
        UserDto userDto = createTestUserDto();

        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = authenticationController.register(registrationDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testUser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test", response.getBody().getFirstName());
        assertEquals("User", response.getBody().getLastName());
        assertEquals("USER", response.getBody().getRole());

        verify(userService, times(1)).registerUser(registrationDto);
    }

    @Test
    void getCurrentUser_WithValidAuthentication_ShouldReturnUser() {
        // Given
        String username = "testUser";
        UserDto userDto = createTestUserDto();

        when(userContextService.getCurrentUsername()).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> response = authenticationController.getCurrentUser();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testUser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("Test", response.getBody().getFirstName());
        assertEquals("User", response.getBody().getLastName());
        assertEquals("USER", response.getBody().getRole());

        verify(userContextService, times(1)).getCurrentUsername();
        verify(userService, times(1)).getUserByUsername(username);
    }

    @Test
    void register_WithMismatchedPasswords_ShouldThrowException() {
        // Given
        UserRegistrationDto registrationDto = createTestUserRegistrationDto();
        registrationDto.setConfirmPassword("differentPassword");

        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new IllegalArgumentException("Password and confirmation password do not match"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            authenticationController.register(registrationDto));

        verify(userService, times(1)).registerUser(registrationDto);
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Given
        UserRegistrationDto registrationDto = createTestUserRegistrationDto();

        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            authenticationController.register(registrationDto));

        verify(userService, times(1)).registerUser(registrationDto);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        UserRegistrationDto registrationDto = createTestUserRegistrationDto();

        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            authenticationController.register(registrationDto));

        verify(userService, times(1)).registerUser(registrationDto);
    }

    @Test
    void getCurrentUser_WithUserNotFound_ShouldThrowException() {
        // Given
        String username = "nonExistentUser";

        when(userContextService.getCurrentUsername()).thenReturn(username);
        when(userService.getUserByUsername(username))
                .thenThrow(new IllegalArgumentException("User not found with username: " + username));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            authenticationController.getCurrentUser());

        verify(userContextService, times(1)).getCurrentUsername();
        verify(userService, times(1)).getUserByUsername(username);
    }

    @Test
    void getCurrentUser_WithAuthenticationFailure_ShouldThrowException() {
        // Given
        when(userContextService.getCurrentUsername())
                .thenThrow(new RuntimeException("User not authenticated"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
            authenticationController.getCurrentUser());

        verify(userContextService, times(1)).getCurrentUsername();
        verify(userService, never()).getUserByUsername(anyString());
    }

    // Helper methods
    private UserDto createTestUserDto() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testUser");
        userDto.setEmail("test@example.com");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setRole("USER");
        userDto.setCreatedAt(LocalDateTime.now());
        userDto.setUpdatedAt(LocalDateTime.now());
        return userDto;
    }

    private UserRegistrationDto createTestUserRegistrationDto() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testUser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword(TestConstants.TEST_USER_PASSWORD);
        registrationDto.setConfirmPassword(TestConstants.TEST_USER_PASSWORD);
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
        return registrationDto;
    }
}
