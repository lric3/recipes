package com.lric3.recipes.service;

import com.lric3.recipes.entity.User;
import com.lric3.recipes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        Authentication authentication = authenticateUser();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }

    public User getCurrentUser() {
        Authentication authentication = authenticateUser();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public String getCurrentUsername() {
        Authentication authentication = authenticateUser();
        return authentication.getName();
    }

    private Authentication authenticateUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated") {};
        }
        return authentication;
    }
}
