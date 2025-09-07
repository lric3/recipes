package com.lric3.recipes.repository;

import com.lric3.recipes.entity.Recipe;
import com.lric3.recipes.entity.Review;
import com.lric3.recipes.entity.User;
import com.lric3.recipes.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private User testUser1;
    private User testUser2;
    private Recipe testRecipe1;
    private Recipe testRecipe2;
    private Review review1;
    private Review review2;
    private Review review3;
    private Review review4;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = createUser("user1", "user1@example.com", TestConstants.TEST_USER_PASSWORD);
        testUser2 = createUser("user2", "user2@example.com", "password456");

        // Create test recipes
        testRecipe1 = createRecipe("Recipe 1", "Description 1", true, "ITALIAN", 
                Recipe.MealType.DINNER, Recipe.DifficultyLevel.EASY, 4.0, 5);
        testRecipe2 = createRecipe("Recipe 2", "Description 2", true, "CHINESE", 
                Recipe.MealType.LUNCH, Recipe.DifficultyLevel.MEDIUM, 3.5, 3);

        // Create test reviews
        review1 = createReview(5, "Excellent recipe!", testUser1, testRecipe1);
        review2 = createReview(4, "Good recipe", testUser2, testRecipe1);
        review3 = createReview(3, "Average recipe", testUser1, testRecipe2);
        review4 = createReview(5, "Amazing!", testUser2, testRecipe2);

        // Persist all entities
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(testRecipe1);
        entityManager.persistAndFlush(testRecipe2);
        entityManager.persistAndFlush(review1);
        entityManager.persistAndFlush(review2);
        entityManager.persistAndFlush(review3);
        entityManager.persistAndFlush(review4);
        entityManager.clear();
    }

    @Test
    void findByRecipeId_WithValidRecipeId_ShouldReturnAllReviewsForRecipe() {
        // When
        List<Review> reviews = reviewRepository.findByRecipeId(testRecipe1.getId());

        // Then
        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().allMatch(review -> 
            review.getRecipe().getId().equals(testRecipe1.getId())));
        
        // Verify specific reviews are included
        assertTrue(reviews.stream().anyMatch(review -> 
            review.getRating().equals(5) && review.getComment().equals("Excellent recipe!")));
        assertTrue(reviews.stream().anyMatch(review -> 
            review.getRating().equals(4) && review.getComment().equals("Good recipe")));
    }

    @Test
    void findByRecipeId_WithNonExistentRecipeId_ShouldReturnEmptyList() {
        // When
        List<Review> reviews = reviewRepository.findByRecipeId(999L);

        // Then
        assertTrue(reviews.isEmpty());
    }

    @Test
    void findByUserId_WithValidUserId_ShouldReturnAllReviewsByUser() {
        // When
        List<Review> reviews = reviewRepository.findByUserId(testUser1.getId());

        // Then
        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().allMatch(review -> 
            review.getUser().getId().equals(testUser1.getId())));
        
        // Verify specific reviews are included
        assertTrue(reviews.stream().anyMatch(review -> 
            review.getRating().equals(5) && review.getComment().equals("Excellent recipe!")));
        assertTrue(reviews.stream().anyMatch(review -> 
            review.getRating().equals(3) && review.getComment().equals("Average recipe")));
    }

    @Test
    void findByUserId_WithNonExistentUserId_ShouldReturnEmptyList() {
        // When
        List<Review> reviews = reviewRepository.findByUserId(999L);

        // Then
        assertTrue(reviews.isEmpty());
    }

    @Test
    void findByRecipeIdAndUserId_WithValidIds_ShouldReturnSpecificReview() {
        // When
        List<Review> reviews = reviewRepository.findByRecipeIdAndUserId(
                testRecipe1.getId(), testUser1.getId());

        // Then
        assertEquals(1, reviews.size());
        Review review = reviews.get(0);
        assertEquals(testRecipe1.getId(), review.getRecipe().getId());
        assertEquals(testUser1.getId(), review.getUser().getId());
        assertEquals(5, review.getRating());
        assertEquals("Excellent recipe!", review.getComment());
    }

    @Test
    void findByRecipeIdAndUserId_WithNonExistentIds_ShouldReturnEmptyList() {
        // When
        List<Review> reviews = reviewRepository.findByRecipeIdAndUserId(999L, 999L);

        // Then
        assertTrue(reviews.isEmpty());
    }

    @Test
    void findByRecipeIdAndUserId_WithValidRecipeIdButInvalidUserId_ShouldReturnEmptyList() {
        // When
        List<Review> reviews = reviewRepository.findByRecipeIdAndUserId(
                testRecipe1.getId(), 999L);

        // Then
        assertTrue(reviews.isEmpty());
    }

    @Test
    void findByRecipeIdAndUserId_WithValidUserIdButInvalidRecipeId_ShouldReturnEmptyList() {
        // When
        List<Review> reviews = reviewRepository.findByRecipeIdAndUserId(
                999L, testUser1.getId());

        // Then
        assertTrue(reviews.isEmpty());
    }

    @Test
    void save_WithValidReview_ShouldPersistReview() {
        // Given
        User newUser = createUser("newUser", "newuser@example.com", "password789");
        Recipe newRecipe = createRecipe("New Recipe", "New Description", true, "AMERICAN", 
                Recipe.MealType.BREAKFAST, Recipe.DifficultyLevel.EASY, 4.2, 8);
        Review newReview = createReview(4, "Great new recipe!", newUser, newRecipe);
        
        entityManager.persistAndFlush(newUser);
        entityManager.persistAndFlush(newRecipe);

        // When
        Review savedReview = reviewRepository.save(newReview);

        // Then
        assertNotNull(savedReview.getId());
        assertEquals(4, savedReview.getRating());
        assertEquals("Great new recipe!", savedReview.getComment());
        assertEquals(newUser.getId(), savedReview.getUser().getId());
        assertEquals(newRecipe.getId(), savedReview.getRecipe().getId());
        assertNotNull(savedReview.getCreatedAt());
        assertNotNull(savedReview.getUpdatedAt());
    }

    @Test
    void findById_WithValidId_ShouldReturnReview() {
        // When
        Optional<Review> reviewOpt = reviewRepository.findById(review1.getId());

        // Then
        assertTrue(reviewOpt.isPresent());
        Review review = reviewOpt.get();
        assertEquals(5, review.getRating());
        assertEquals("Excellent recipe!", review.getComment());
        assertEquals(testUser1.getId(), review.getUser().getId());
        assertEquals(testRecipe1.getId(), review.getRecipe().getId());
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmptyOptional() {
        // When
        Optional<Review> reviewOpt = reviewRepository.findById(999L);

        // Then
        assertFalse(reviewOpt.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllReviews() {
        // When
        List<Review> allReviews = reviewRepository.findAll();

        // Then
        assertEquals(4, allReviews.size());
    }

    @Test
    void deleteById_WithValidId_ShouldRemoveReview() {
        // Given
        Long reviewId = review1.getId();

        // When
        reviewRepository.deleteById(reviewId);

        // Then
        Optional<Review> deletedReview = reviewRepository.findById(reviewId);
        assertFalse(deletedReview.isPresent());
        
        // Verify other reviews still exist
        List<Review> remainingReviews = reviewRepository.findAll();
        assertEquals(3, remainingReviews.size());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = reviewRepository.count();

        // Then
        assertEquals(4, count);
    }

    @Test
    void existsById_WithValidId_ShouldReturnTrue() {
        // When
        boolean exists = reviewRepository.existsById(review1.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_WithInvalidId_ShouldReturnFalse() {
        // When
        boolean exists = reviewRepository.existsById(999L);

        // Then
        assertFalse(exists);
    }

    @Test
    void updateReview_ShouldModifyExistingReview() {
        // Given
        Review review = reviewRepository.findById(review1.getId()).orElseThrow();
        LocalDateTime originalUpdatedAt = review.getUpdatedAt();
        review.setRating(3);
        review.setComment("Updated comment");

        // When
        Review updatedReview = reviewRepository.save(review);

        // Then
        assertEquals(3, updatedReview.getRating());
        assertEquals("Updated comment", updatedReview.getComment());
        assertEquals(review1.getId(), updatedReview.getId());
        assertTrue(updatedReview.getUpdatedAt().isAfter(originalUpdatedAt) || 
                   updatedReview.getUpdatedAt().equals(originalUpdatedAt));
    }

    @Test
    void findByRecipeId_WithMultipleRecipes_ShouldReturnCorrectReviews() {
        // When
        List<Review> recipe1Reviews = reviewRepository.findByRecipeId(testRecipe1.getId());
        List<Review> recipe2Reviews = reviewRepository.findByRecipeId(testRecipe2.getId());

        // Then
        assertEquals(2, recipe1Reviews.size());
        assertEquals(2, recipe2Reviews.size());
        
        // Verify no overlap
        assertTrue(recipe1Reviews.stream().noneMatch(review -> 
            review.getRecipe().getId().equals(testRecipe2.getId())));
        assertTrue(recipe2Reviews.stream().noneMatch(review -> 
            review.getRecipe().getId().equals(testRecipe1.getId())));
    }

    @Test
    void findByUserId_WithMultipleUsers_ShouldReturnCorrectReviews() {
        // When
        List<Review> user1Reviews = reviewRepository.findByUserId(testUser1.getId());
        List<Review> user2Reviews = reviewRepository.findByUserId(testUser2.getId());

        // Then
        assertEquals(2, user1Reviews.size());
        assertEquals(2, user2Reviews.size());
        
        // Verify no overlap
        assertTrue(user1Reviews.stream().noneMatch(review -> 
            review.getUser().getId().equals(testUser2.getId())));
        assertTrue(user2Reviews.stream().noneMatch(review -> 
            review.getUser().getId().equals(testUser1.getId())));
    }

    // Helper methods
    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(User.Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private Recipe createRecipe(String title, String description, boolean isPublic, 
                               String cuisineType, Recipe.MealType mealType, 
                               Recipe.DifficultyLevel difficultyLevel, double rating, int favoriteCount) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setPublic(isPublic);
        recipe.setCuisineType(cuisineType);
        recipe.setMealType(mealType);
        recipe.setDifficultyLevel(difficultyLevel);
        recipe.setRating(rating);
        recipe.setFavoriteCount(favoriteCount);
        recipe.setPrepTime(15);
        recipe.setCookTime(30);
        recipe.setServings(4);
        recipe.setUser(testUser1); // Will be updated after user is persisted
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        return recipe;
    }

    private Review createReview(Integer rating, String comment, User user, Recipe recipe) {
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setUser(user);
        review.setRecipe(recipe);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        return review;
    }
}
