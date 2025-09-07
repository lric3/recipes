package com.lric3.recipes.util;

import com.lric3.recipes.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBuilder {

    @Value("${test.admin.password}")
    private String testAdminPassword;

    @Value("${test.user.password}")
    private String testUserPassword;

    @Value("${DEFAULT_ADMIN_USERNAME:admin}")
    private String defaultAdminUsername;

    @Value("${DEFAULT_ADMIN_EMAIL:admin@recipes.com}")
    private String defaultAdminEmail;

    @Value("${DEFAULT_ADMIN_FIRST_NAME:Admin}")
    private String defaultAdminFirstName;

    @Value("${DEFAULT_ADMIN_LAST_NAME:User}")
    private String defaultAdminLastName;

    @Value("${DEFAULT_USER_USERNAME:chef}")
    private String defaultUserUsername;

    @Value("${DEFAULT_USER_EMAIL:chef@recipes.com}")
    private String defaultUserEmail;

    @Value("${DEFAULT_USER_FIRST_NAME:Master}")
    private String defaultUserFirstName;

    @Value("${DEFAULT_USER_LAST_NAME:Chef}")
    private String defaultUserLastName;

    private final PasswordEncoder passwordEncoder;

    public User buildAdminUser() {
        User admin = new User();
        admin.setUsername(defaultAdminUsername);
        admin.setEmail(defaultAdminEmail);
        admin.setPassword(passwordEncoder.encode(testAdminPassword));
        admin.setFirstName(defaultAdminFirstName);
        admin.setLastName(defaultAdminLastName);
        admin.setRole(User.Role.ADMIN);

        return admin;
    }

    public User buildUser() {
        User user = new User();
        user.setUsername(defaultUserUsername);
        user.setEmail(defaultUserEmail);
        user.setPassword(passwordEncoder.encode(testUserPassword));
        user.setFirstName(defaultUserFirstName);
        user.setLastName(defaultUserLastName);
        user.setRole(User.Role.USER);

        return user;
    }
}
