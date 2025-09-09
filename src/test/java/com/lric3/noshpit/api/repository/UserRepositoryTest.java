package com.lric3.noshpit.api.repository;

import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = createUser("john_doe", "john@example.com", "John", "Doe", TestConstants.TEST_USER_PASSWORD);
        user2 = createUser("jane_smith", "jane@example.com", "Jane", "Smith", "password456");
        user3 = createUser("bob_wilson", "bob@example.com", "Bob", "Wilson", "password789");

        // Persist users
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        entityManager.clear();
    }

    @Test
    void findByUsername_WithExistingUsername_ShouldReturnUser() {
        // When
        Optional<User> result = userRepository.findByUsername("john_doe");

        // Then
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
        assertEquals("john@example.com", result.get().getEmail());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
    }

    @Test
    void findByUsername_WithNonExistingUsername_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByUsername("non_existing_user");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_WithNullUsername_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByUsername(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // When
        Optional<User> result = userRepository.findByEmail("jane@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("jane_smith", result.get().getUsername());
        assertEquals("jane@example.com", result.get().getEmail());
        assertEquals("Jane", result.get().getFirstName());
        assertEquals("Smith", result.get().getLastName());
    }

    @Test
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByEmail("non_existing@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_WithNullEmail_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByEmail(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void existsByUsername_WithExistingUsername_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByUsername("bob_wilson");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByUsername_WithNonExistingUsername_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("non_existing_user");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByUsername_WithNullUsername_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername(null);

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByEmail("john@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("non_existing@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_WithNullEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail(null);

        // Then
        assertFalse(exists);
    }

    @Test
    void save_ShouldCreateNewUser() {
        // Given
        User newUser = createUser("new_user", "new@example.com", "New", "User", TestConstants.TEST_USER_PASSWORD);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("new_user", savedUser.getUsername());
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("New", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void save_ShouldUpdateExistingUser() {
        // Given
        User user = userRepository.findById(user1.getId()).orElseThrow();
        user.setFirstName("UpdatedJohn");
        user.setLastName("UpdatedDoe");

        // When
        User updatedUser = userRepository.save(user);

        // Then
        assertEquals(user1.getId(), updatedUser.getId());
        assertEquals("UpdatedJohn", updatedUser.getFirstName());
        assertEquals("UpdatedDoe", updatedUser.getLastName());
        assertEquals("john_doe", updatedUser.getUsername());
        assertEquals("john@example.com", updatedUser.getEmail());
    }

    @Test
    void findById_WithExistingId_ShouldReturnUser() {
        // When
        Optional<User> result = userRepository.findById(user2.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("jane_smith", result.get().getUsername());
        assertEquals("jane@example.com", result.get().getEmail());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // When
        List<User> users = userRepository.findAll();

        // Then
        assertEquals(3, users.size());
        assertTrue(users.stream().anyMatch(user -> "john_doe".equals(user.getUsername())));
        assertTrue(users.stream().anyMatch(user -> "jane_smith".equals(user.getUsername())));
        assertTrue(users.stream().anyMatch(user -> "bob_wilson".equals(user.getUsername())));
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        // Given
        Long userId = user3.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void findByUsername_ShouldBeCaseSensitive() {
        // When
        Optional<User> result = userRepository.findByUsername("JOHN_DOE");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_ShouldBeCaseSensitive() {
        // When
        Optional<User> result = userRepository.findByEmail("JANE@EXAMPLE.COM");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void existsByUsername_ShouldBeCaseSensitive() {
        // When
        boolean exists = userRepository.existsByUsername("BOB_WILSON");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByEmail_ShouldBeCaseSensitive() {
        // When
        boolean exists = userRepository.existsByEmail("JOHN@EXAMPLE.COM");

        // Then
        assertFalse(exists);
    }

    private User createUser(String username, String email, String firstName, String lastName, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setRole(User.Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
