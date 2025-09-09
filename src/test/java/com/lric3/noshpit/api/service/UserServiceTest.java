package com.lric3.noshpit.api.service;

import com.lric3.noshpit.api.dto.UserDto;
import com.lric3.noshpit.api.dto.UserRegistrationDto;
import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.repository.UserRepository;
import com.lric3.noshpit.api.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationDto testRegistrationDto;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        testUser.setUpdatedAt(LocalDateTime.now());

        // Create test registration DTO
        testRegistrationDto = new UserRegistrationDto();
        testRegistrationDto.setUsername("newuser");
        testRegistrationDto.setEmail("newuser@example.com");
        testRegistrationDto.setPassword(TestConstants.TEST_USER_PASSWORD);
        testRegistrationDto.setConfirmPassword(TestConstants.TEST_USER_PASSWORD);
        testRegistrationDto.setFirstName("New");
        testRegistrationDto.setLastName("User");
    }

    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userService.loadUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository, times(1)).findByUsername("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_WithInvalidUsernameOrEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });

        assertEquals("User not found with username or email: nonexistent", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(userRepository, times(1)).findByEmail("nonexistent");
    }

    @Test
    void loadUserByUsername_WithNullInput_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(null);
        });

        assertEquals("User not found with username or email: null", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(null);
        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    void loadUserByUsername_WithEmptyString_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("");
        });

        assertEquals("User not found with username or email: ", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("");
        verify(userRepository, times(1)).findByEmail("");
    }

    @Test
    void registerUser_WithValidData_ShouldReturnUserDto() {
        // Given
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setRole(User.Role.USER);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(TestConstants.TEST_USER_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDto result = userService.registerUser(testRegistrationDto);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode(TestConstants.TEST_USER_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WithPasswordMismatch_ShouldThrowException() {
        // Given
        testRegistrationDto.setConfirmPassword("differentPassword");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(testRegistrationDto);
        });

        assertEquals("Password and confirmation password do not match", exception.getMessage());

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingUsername_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(testRegistrationDto);
        });

        assertEquals("Username already exists", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(testRegistrationDto);
        });

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithNullRegistrationDto_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            userService.registerUser(null);
        });

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUserDto() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals("User not found with id: 999", exception.getMessage());

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getUserById_WithNullId_ShouldThrowException() {
        // Given
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(null);
        });

        assertEquals("User not found with id: null", exception.getMessage());

        verify(userRepository, times(1)).findById(null);
    }

    @Test
    void getUserByUsername_WithValidUsername_ShouldReturnUserDto() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_WithInvalidUsername_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });

        assertEquals("User not found with username: nonexistent", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void getUserByUsername_WithNullUsername_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByUsername(null);
        });

        assertEquals("User not found with username: null", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(null);
    }

    @Test
    void getUserByUsername_WithEmptyUsername_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByUsername("");
        });

        assertEquals("User not found with username: ", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUserDto() {
        // Given
        UserDto updateDto = new UserDto();
        updateDto.setId(1L);
        updateDto.setUsername("testuser");
        updateDto.setEmail("test@example.com");
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setPassword("encodedPassword");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setRole(User.Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserDto result = userService.updateUser(1L, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WithInvalidId_ShouldThrowException() {
        // Given
        UserDto updateDto = new UserDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(999L, updateDto);
        });

        assertEquals("User not found with id: 999", exception.getMessage());

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithNullId_ShouldThrowException() {
        // Given
        UserDto updateDto = new UserDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");

        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(null, updateDto);
        });

        assertEquals("User not found with id: null", exception.getMessage());

        verify(userRepository, times(1)).findById(null);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithNullUserDto_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            userService.updateUser(1L, null);
        });

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(999L);
        });

        assertEquals("User not found with id: 999", exception.getMessage());

        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_WithNullId_ShouldThrowException() {
        // Given
        when(userRepository.existsById(null)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(null);
        });

        assertEquals("User not found with id: null", exception.getMessage());

        verify(userRepository, times(1)).existsById(null);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void registerUser_WithSpecialCharactersInUsername_ShouldHandleCorrectly() {
        // Given
        testRegistrationDto.setUsername("user@domain.com");
        testRegistrationDto.setEmail("user@domain.com");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("user@domain.com");
        savedUser.setEmail("user@domain.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setRole(User.Role.USER);

        when(userRepository.existsByUsername("user@domain.com")).thenReturn(false);
        when(userRepository.existsByEmail("user@domain.com")).thenReturn(false);
        when(passwordEncoder.encode(TestConstants.TEST_USER_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDto result = userService.registerUser(testRegistrationDto);

        // Then
        assertNotNull(result);
        assertEquals("user@domain.com", result.getUsername());
        assertEquals("user@domain.com", result.getEmail());

        verify(userRepository, times(1)).existsByUsername("user@domain.com");
        verify(userRepository, times(1)).existsByEmail("user@domain.com");
        verify(passwordEncoder, times(1)).encode(TestConstants.TEST_USER_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WithLongUsername_ShouldHandleCorrectly() {
        // Given
        String longUsername = "verylongusernamethatexceedsnormallimitsandshouldstillworkcorrectly";
        testRegistrationDto.setUsername(longUsername);

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername(longUsername);
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setRole(User.Role.USER);

        when(userRepository.existsByUsername(longUsername)).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(TestConstants.TEST_USER_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDto result = userService.registerUser(testRegistrationDto);

        // Then
        assertNotNull(result);
        assertEquals(longUsername, result.getUsername());

        verify(userRepository, times(1)).existsByUsername(longUsername);
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode(TestConstants.TEST_USER_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WithEmptyFields_ShouldHandleCorrectly() {
        // Given
        testRegistrationDto.setFirstName("");
        testRegistrationDto.setLastName("");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName("");
        savedUser.setLastName("");
        savedUser.setRole(User.Role.USER);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(TestConstants.TEST_USER_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDto result = userService.registerUser(testRegistrationDto);

        // Then
        assertNotNull(result);
        assertEquals("", result.getFirstName());
        assertEquals("", result.getLastName());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode(TestConstants.TEST_USER_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WithNullFields_ShouldHandleCorrectly() {
        // Given
        testRegistrationDto.setFirstName(null);
        testRegistrationDto.setLastName(null);

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName(null);
        savedUser.setLastName(null);
        savedUser.setRole(User.Role.USER);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(TestConstants.TEST_USER_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDto result = userService.registerUser(testRegistrationDto);

        // Then
        assertNotNull(result);
        assertNull(result.getFirstName());
        assertNull(result.getLastName());

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode(TestConstants.TEST_USER_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }
}
