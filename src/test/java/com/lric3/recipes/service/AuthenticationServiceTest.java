package com.lric3.recipes.service;

import com.lric3.recipes.dto.AuthRequest;
import com.lric3.recipes.dto.AuthResponse;
import com.lric3.recipes.dto.UserDto;
import com.lric3.recipes.security.JwtUtil;
import com.lric3.recipes.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static com.lric3.recipes.dto.AuthResponse.TOKEN_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthRequest authRequest;
    private UserDetails userDetails;
    private UserDto userDto;
    private Authentication authentication;
    private String jwtToken;
    private Date expirationDate;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        // Create test data
        authRequest = new AuthRequest(TestConstants.TEST_USERNAME, TestConstants.TEST_USER_PASSWORD);
        
        userDetails = new User(TestConstants.TEST_USERNAME, TestConstants.TEST_USER_PASSWORD, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setRole("USER");
        userDto.setCreatedAt(LocalDateTime.now().minusDays(1));
        userDto.setUpdatedAt(LocalDateTime.now());
        
        authentication = mock(Authentication.class);
        jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.jwt.token";
        expirationDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        expiresAt = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnAuthResponse() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken)).thenReturn(expirationDate);
        when(userService.getUserByUsername("testuser")).thenReturn(userDto);

        // When
        AuthResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("Bearer", TOKEN_TYPE);
        assertEquals(expiresAt, response.getExpiresAt());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("Test", response.getUser().getFirstName());
        assertEquals("User", response.getUser().getLastName());
        assertEquals("USER", response.getUser().getRole());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(jwtUtil, times(1)).extractExpiration(jwtToken);
        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    void authenticate_WithEmailCredentials_ShouldReturnAuthResponse() {
        // Given
        AuthRequest emailRequest = new AuthRequest(TestConstants.TEST_EMAIL, TestConstants.TEST_USER_PASSWORD);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken)).thenReturn(expirationDate);
        when(userService.getUserByUsername("testuser")).thenReturn(userDto);

        // When
        AuthResponse response = authenticationService.authenticate(emailRequest);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("Bearer", TOKEN_TYPE);
        assertEquals(expiresAt, response.getExpiresAt());
        assertNotNull(response.getUser());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(jwtUtil, times(1)).extractExpiration(jwtToken);
        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(authRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
        verify(jwtUtil, never()).extractExpiration(anyString());
        verify(userService, never()).getUserByUsername(anyString());
    }

    @Test
    void authenticate_WithNullAuthRequest_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            authenticationService.authenticate(null);
        });

        // Verify no interactions
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).extractExpiration(any());
        verify(userService, never()).getUserByUsername(any());
    }

    @Test
    void authenticate_WithNullUsernameOrEmail_ShouldThrowException() {
        // Given
        AuthRequest nullUsernameRequest = new AuthRequest(null, TestConstants.TEST_USER_PASSWORD);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // AuthenticationManager returns null for invalid credentials

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            authenticationService.authenticate(nullUsernameRequest);
        });

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).extractExpiration(any());
        verify(userService, never()).getUserByUsername(any());
    }

    @Test
    void authenticate_WithNullPassword_ShouldThrowException() {
        // Given
        AuthRequest nullPasswordRequest = new AuthRequest("testuser", null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // AuthenticationManager returns null for invalid credentials

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            authenticationService.authenticate(nullPasswordRequest);
        });

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).extractExpiration(any());
        verify(userService, never()).getUserByUsername(any());
    }

    @Test
    void authenticate_WithEmptyUsernameOrEmail_ShouldThrowException() {
        // Given
        AuthRequest emptyUsernameRequest = new AuthRequest("", TestConstants.TEST_USER_PASSWORD);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // AuthenticationManager returns null for invalid credentials

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            authenticationService.authenticate(emptyUsernameRequest);
        });

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).extractExpiration(any());
        verify(userService, never()).getUserByUsername(any());
    }

    @Test
    void authenticate_WithEmptyPassword_ShouldThrowException() {
        // Given
        AuthRequest emptyPasswordRequest = new AuthRequest("testuser", "");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // AuthenticationManager returns null for invalid credentials

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            authenticationService.authenticate(emptyPasswordRequest);
        });

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).extractExpiration(any());
        verify(userService, never()).getUserByUsername(any());
    }

    @Test
    void authenticate_WithUserNotFound_ShouldReturnAuthResponseWithNullUser() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken)).thenReturn(expirationDate);
        when(userService.getUserByUsername("testuser")).thenReturn(null);

        // When
        AuthResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("Bearer", TOKEN_TYPE);
        assertEquals(expiresAt, response.getExpiresAt());
        assertNull(response.getUser()); // User should be null when not found

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(jwtUtil, times(1)).extractExpiration(jwtToken);
        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    void authenticate_WithJwtGenerationFailure_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenThrow(new RuntimeException("JWT generation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(authRequest);
        });

        assertEquals("JWT generation failed", exception.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(jwtUtil, never()).extractExpiration(anyString());
        verify(userService, never()).getUserByUsername(anyString());
    }

    @Test
    void authenticate_WithTokenExpirationExtractionFailure_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken)).thenThrow(new RuntimeException("Token expiration extraction failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(authRequest);
        });

        assertEquals("Token expiration extraction failed", exception.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(jwtUtil, times(1)).extractExpiration(jwtToken);
        verify(userService, never()).getUserByUsername(anyString());
    }

    @Test
    void authenticate_WithDifferentUserRoles_ShouldReturnCorrectRole() {
        // Given
        UserDetails adminUserDetails = new User("admin", TestConstants.TEST_USER_PASSWORD, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        UserDto adminUserDto = new UserDto();
        adminUserDto.setId(2L);
        adminUserDto.setUsername("admin");
        adminUserDto.setEmail("admin@example.com");
        adminUserDto.setFirstName("Admin");
        adminUserDto.setLastName("User");
        adminUserDto.setRole("ADMIN");
        adminUserDto.setCreatedAt(LocalDateTime.now().minusDays(1));
        adminUserDto.setUpdatedAt(LocalDateTime.now());

        AuthRequest adminRequest = new AuthRequest("admin", TestConstants.TEST_USER_PASSWORD);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);
        when(jwtUtil.generateToken(adminUserDetails)).thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken)).thenReturn(expirationDate);
        when(userService.getUserByUsername("admin")).thenReturn(adminUserDto);

        // When
        AuthResponse response = authenticationService.authenticate(adminRequest);

        // Then
        assertNotNull(response);
        assertEquals("ADMIN", response.getUser().getRole());
        assertEquals("admin", response.getUser().getUsername());
        assertEquals("admin@example.com", response.getUser().getEmail());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(adminUserDetails);
        verify(jwtUtil, times(1)).extractExpiration(jwtToken);
        verify(userService, times(1)).getUserByUsername("admin");
    }

    @Test
    void authenticate_WithLongUsername_ShouldHandleCorrectly() {
        // Given
        String longUsername = "verylongusernamethatexceedsnormallength";
        AuthRequest longUsernameRequest = new AuthRequest(longUsername, TestConstants.TEST_USER_PASSWORD);
        
        UserDetails longUsernameUserDetails = new User(longUsername, TestConstants.TEST_USER_PASSWORD, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        
        UserDto longUsernameUserDto = new UserDto();
        longUsernameUserDto.setId(3L);
        longUsernameUserDto.setUsername(longUsername);
        longUsernameUserDto.setEmail("longuser@example.com");
        longUsernameUserDto.setFirstName("Long");
        longUsernameUserDto.setLastName("User");
        longUsernameUserDto.setRole("USER");
        longUsernameUserDto.setCreatedAt(LocalDateTime.now().minusDays(1));
        longUsernameUserDto.setUpdatedAt(LocalDateTime.now());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(longUsernameUserDetails);
        when(jwtUtil.generateToken(longUsernameUserDetails)).thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken)).thenReturn(expirationDate);
        when(userService.getUserByUsername(longUsername)).thenReturn(longUsernameUserDto);

        // When
        AuthResponse response = authenticationService.authenticate(longUsernameRequest);

        // Then
        assertNotNull(response);
        assertEquals(longUsername, response.getUser().getUsername());
        assertEquals("longuser@example.com", response.getUser().getEmail());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(longUsernameUserDetails);
        verify(jwtUtil, times(1)).extractExpiration(jwtToken);
        verify(userService, times(1)).getUserByUsername(longUsername);
    }

    @Test
    void authenticate_WithSpecialCharactersInUsername_ShouldHandleCorrectly() {
        // Given
        String specialUsername = "user.name+tag@domain";
        AuthRequest specialUsernameRequest = new AuthRequest(specialUsername, TestConstants.TEST_USER_PASSWORD);
        
        UserDetails specialUsernameUserDetails = new User(specialUsername, TestConstants.TEST_USER_PASSWORD, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        
        UserDto specialUsernameUserDto = new UserDto();
        specialUsernameUserDto.setId(4L);
        specialUsernameUserDto.setUsername(specialUsername);
        specialUsernameUserDto.setEmail("special@example.com");
        specialUsernameUserDto.setFirstName("Special");
        specialUsernameUserDto.setLastName("User");
        specialUsernameUserDto.setRole("USER");
        specialUsernameUserDto.setCreatedAt(LocalDateTime.now().minusDays(1));
        specialUsernameUserDto.setUpdatedAt(LocalDateTime.now());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(specialUsernameUserDetails);
        when(jwtUtil.generateToken(specialUsernameUserDetails)).thenReturn(jwtToken);
        when(jwtUtil.extractExpiration(jwtToken)).thenReturn(expirationDate);
        when(userService.getUserByUsername(specialUsername)).thenReturn(specialUsernameUserDto);

        // When
        AuthResponse response = authenticationService.authenticate(specialUsernameRequest);

        // Then
        assertNotNull(response);
        assertEquals(specialUsername, response.getUser().getUsername());
        assertEquals("special@example.com", response.getUser().getEmail());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(specialUsernameUserDetails);
        verify(jwtUtil, times(1)).extractExpiration(jwtToken);
        verify(userService, times(1)).getUserByUsername(specialUsername);
    }
}
