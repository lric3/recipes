package com.lric3.noshpit.api.util;

/**
 * Test constants for the recipes application.
 * These constants centralize test data and make it easier to maintain.
 */
public class TestConstants {

    // Test User Credentials
    public static final String TEST_USER_PASSWORD = "testPassword123";
    public static final String TEST_ADMIN_PASSWORD = "testAdmin123";
    public static final String TEST_CHEF_PASSWORD = "testChef123";

    // Test User Data
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_FIRST_NAME = "Test";
    public static final String TEST_LAST_NAME = "User";

    // Test Admin Data
    public static final String TEST_ADMIN_USERNAME = "testadmin";
    public static final String TEST_ADMIN_EMAIL = "admin@example.com";
    public static final String TEST_ADMIN_FIRST_NAME = "Test";
    public static final String TEST_ADMIN_LAST_NAME = "Admin";

    // Test Chef Data
    public static final String TEST_CHEF_USERNAME = "testchef";
    public static final String TEST_CHEF_EMAIL = "chef@example.com";
    public static final String TEST_CHEF_FIRST_NAME = "Test";
    public static final String TEST_CHEF_LAST_NAME = "Chef";

    // Test Recipe Data
    public static final String TEST_RECIPE_TITLE = "Test Recipe";
    public static final String TEST_RECIPE_DESCRIPTION = "A test recipe for unit testing";
    public static final int TEST_PREP_TIME = 15;
    public static final int TEST_COOK_TIME = 30;
    public static final int TEST_SERVINGS = 4;

    // Test Review Data
    public static final int TEST_RATING = 5;
    public static final String TEST_COMMENT = "Great recipe!";

    // Private constructor to prevent instantiation
    private TestConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
