package com.lric3.noshpit.api.service;

import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserContextServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserContextService userContextService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getCurrentUserId_WithValidAuthentication_ShouldReturnUserId() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // When
            Long result = userContextService.getCurrentUserId();

            // Then
            assertNotNull(result);
            assertEquals(1L, result);

            verify(userRepository, times(1)).findByUsername("testuser");
        }
    }

    @Test
    void getCurrentUserId_WithNullAuthentication_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUserId();
            });

            assertEquals("User not authenticated", exception.getMessage());

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUserId_WithUnauthenticatedUser_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUserId();
            });

            assertEquals("User not authenticated", exception.getMessage());

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUserId_WithUserNotFound_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("nonexistentuser");
            when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUserId();
            });

            assertEquals("User not found: nonexistentuser", exception.getMessage());

            verify(userRepository, times(1)).findByUsername("nonexistentuser");
        }
    }

    @Test
    void getCurrentUser_WithValidAuthentication_ShouldReturnUser() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // When
            User result = userContextService.getCurrentUser();

            // Then
            assertNotNull(result);
            assertEquals(testUser.getId(), result.getId());
            assertEquals(testUser.getUsername(), result.getUsername());
            assertEquals(testUser.getEmail(), result.getEmail());
            assertEquals(testUser.getFirstName(), result.getFirstName());
            assertEquals(testUser.getLastName(), result.getLastName());
            assertEquals(testUser.getRole(), result.getRole());

            verify(userRepository, times(1)).findByUsername("testuser");
        }
    }

    @Test
    void getCurrentUser_WithNullAuthentication_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUser();
            });

            assertEquals("User not authenticated", exception.getMessage());

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUser_WithUnauthenticatedUser_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUser();
            });

            assertEquals("User not authenticated", exception.getMessage());

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUser_WithUserNotFound_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("nonexistentuser");
            when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUser();
            });

            assertEquals("User not found: nonexistentuser", exception.getMessage());

            verify(userRepository, times(1)).findByUsername("nonexistentuser");
        }
    }

    @Test
    void getCurrentUsername_WithValidAuthentication_ShouldReturnUsername() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");

            // When
            String result = userContextService.getCurrentUsername();

            // Then
            assertNotNull(result);
            assertEquals("testuser", result);

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUsername_WithNullAuthentication_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUsername();
            });

            assertEquals("User not authenticated", exception.getMessage());

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUsername_WithUnauthenticatedUser_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userContextService.getCurrentUsername();
            });

            assertEquals("User not authenticated", exception.getMessage());

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUsername_WithEmptyUsername_ShouldReturnEmptyString() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("");

            // When
            String result = userContextService.getCurrentUsername();

            // Then
            assertNotNull(result);
            assertEquals("", result);

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUsername_WithNullUsername_ShouldReturnNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(null);

            // When
            String result = userContextService.getCurrentUsername();

            // Then
            assertNull(result);

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUserId_WithDifferentUserRoles_ShouldReturnCorrectUserId() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            User adminUser = new User();
            adminUser.setId(2L);
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(User.Role.ADMIN);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            // When
            Long result = userContextService.getCurrentUserId();

            // Then
            assertNotNull(result);
            assertEquals(2L, result);

            verify(userRepository, times(1)).findByUsername("admin");
        }
    }

    @Test
    void getCurrentUser_WithDifferentUserRoles_ShouldReturnCorrectUser() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            User adminUser = new User();
            adminUser.setId(2L);
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(User.Role.ADMIN);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            // When
            User result = userContextService.getCurrentUser();

            // Then
            assertNotNull(result);
            assertEquals(adminUser.getId(), result.getId());
            assertEquals(adminUser.getUsername(), result.getUsername());
            assertEquals(adminUser.getRole(), result.getRole());
            assertEquals(User.Role.ADMIN, result.getRole());

            verify(userRepository, times(1)).findByUsername("admin");
        }
    }

    @Test
    void getCurrentUserId_WithSpecialCharactersInUsername_ShouldHandleCorrectly() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            String specialUsername = "user@domain.com";
            User specialUser = new User();
            specialUser.setId(3L);
            specialUser.setUsername(specialUsername);
            specialUser.setEmail("user@domain.com");
            specialUser.setFirstName("Special");
            specialUser.setLastName("User");
            specialUser.setRole(User.Role.USER);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(specialUsername);
            when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(specialUser));

            // When
            Long result = userContextService.getCurrentUserId();

            // Then
            assertNotNull(result);
            assertEquals(3L, result);

            verify(userRepository, times(1)).findByUsername(specialUsername);
        }
    }

    @Test
    void getCurrentUser_WithSpecialCharactersInUsername_ShouldHandleCorrectly() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            String specialUsername = "user@domain.com";
            User specialUser = new User();
            specialUser.setId(3L);
            specialUser.setUsername(specialUsername);
            specialUser.setEmail("user@domain.com");
            specialUser.setFirstName("Special");
            specialUser.setLastName("User");
            specialUser.setRole(User.Role.USER);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(specialUsername);
            when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(specialUser));

            // When
            User result = userContextService.getCurrentUser();

            // Then
            assertNotNull(result);
            assertEquals(specialUser.getId(), result.getId());
            assertEquals(specialUser.getUsername(), result.getUsername());
            assertEquals(specialUser.getEmail(), result.getEmail());

            verify(userRepository, times(1)).findByUsername(specialUsername);
        }
    }

    @Test
    void getCurrentUsername_WithSpecialCharacters_ShouldReturnCorrectUsername() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            String specialUsername = "user@domain.com";
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(specialUsername);

            // When
            String result = userContextService.getCurrentUsername();

            // Then
            assertNotNull(result);
            assertEquals(specialUsername, result);

            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void getCurrentUserId_WithLongUsername_ShouldHandleCorrectly() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            String longUsername = "verylongusernamethatexceedsnormallimitsandshouldstillworkcorrectly";
            User longUsernameUser = new User();
            longUsernameUser.setId(4L);
            longUsernameUser.setUsername(longUsername);
            longUsernameUser.setEmail("longuser@example.com");
            longUsernameUser.setFirstName("Long");
            longUsernameUser.setLastName("User");
            longUsernameUser.setRole(User.Role.USER);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(longUsername);
            when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(longUsernameUser));

            // When
            Long result = userContextService.getCurrentUserId();

            // Then
            assertNotNull(result);
            assertEquals(4L, result);

            verify(userRepository, times(1)).findByUsername(longUsername);
        }
    }

    @Test
    void getCurrentUser_WithLongUsername_ShouldHandleCorrectly() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            String longUsername = "verylongusernamethatexceedsnormallimitsandshouldstillworkcorrectly";
            User longUsernameUser = new User();
            longUsernameUser.setId(4L);
            longUsernameUser.setUsername(longUsername);
            longUsernameUser.setEmail("longuser@example.com");
            longUsernameUser.setFirstName("Long");
            longUsernameUser.setLastName("User");
            longUsernameUser.setRole(User.Role.USER);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(longUsername);
            when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(longUsernameUser));

            // When
            User result = userContextService.getCurrentUser();

            // Then
            assertNotNull(result);
            assertEquals(longUsernameUser.getId(), result.getId());
            assertEquals(longUsernameUser.getUsername(), result.getUsername());
            assertEquals(longUsernameUser.getEmail(), result.getEmail());

            verify(userRepository, times(1)).findByUsername(longUsername);
        }
    }

    @Test
    void getCurrentUsername_WithLongUsername_ShouldReturnCorrectUsername() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Given
            String longUsername = "verylongusernamethatexceedsnormallimitsandshouldstillworkcorrectly";
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(longUsername);

            // When
            String result = userContextService.getCurrentUsername();

            // Then
            assertNotNull(result);
            assertEquals(longUsername, result);

            verify(userRepository, never()).findByUsername(anyString());
        }
    }
}
