package com.lric3.noshpit.api.service;

import com.lric3.noshpit.api.dto.UserDto;
import com.lric3.noshpit.api.dto.UserRegistrationDto;
import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(usernameOrEmail);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(usernameOrEmail);
        }

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
        }

        return user.get();
    }

    public UserDto registerUser(UserRegistrationDto registrationDto) {
        // Validate password confirmation
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirmation password do not match");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        return new UserDto(user.get());
    }

    public UserDto getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }
        return new UserDto(user.get());
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        User user = userOpt.get();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
