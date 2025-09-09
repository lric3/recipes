package com.lric3.noshpit.api.service;

import com.lric3.noshpit.api.entity.Recipe;
import com.lric3.noshpit.api.entity.Review;
import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.repository.RecipeRepository;
import com.lric3.noshpit.api.repository.ReviewRepository;
import com.lric3.noshpit.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Recipe testRecipe;
    private Review testReview;

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

        // Create test recipe
        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setTitle("Test Recipe");
        testRecipe.setDescription("A test recipe description");
        testRecipe.setPrepTime(15);
        testRecipe.setCookTime(30);
        testRecipe.setServings(4);
        testRecipe.setDifficultyLevel(Recipe.DifficultyLevel.EASY);
        testRecipe.setCuisineType("Italian");
        testRecipe.setMealType(Recipe.MealType.DINNER);
        testRecipe.setDietaryRestrictions(Collections.singletonList(Recipe.DietaryRestriction.VEGETARIAN));
        testRecipe.setImageUrl("http://example.com/image.jpg");
        testRecipe.setVideoUrl("http://example.com/video.mp4");
        testRecipe.setPublic(true);
        testRecipe.setRating(4.5);
        testRecipe.setRatingCount(10);
        testRecipe.setFavoriteCount(5);
        testRecipe.setUser(testUser);
        testRecipe.setCreatedAt(LocalDateTime.now().minusDays(1));
        testRecipe.setUpdatedAt(LocalDateTime.now());

        // Create test review
        testReview = new Review();
        testReview.setId(1L);
        testReview.setRating(5);
        testReview.setComment("Excellent recipe! Very tasty and easy to make.");
        testReview.setUser(testUser);
        testReview.setRecipe(testRecipe);
        testReview.setCreatedAt(LocalDateTime.now().minusHours(1));
        testReview.setUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    void createReview_WithValidData_ShouldReturnSavedReview() {
        // Given
        Review newReview = new Review();
        newReview.setRating(4);
        newReview.setComment("Great recipe!");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        Review result = reviewService.createReview(newReview, 1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(testReview.getId(), result.getId());
        assertEquals(testReview.getRating(), result.getRating());
        assertEquals(testReview.getComment(), result.getComment());
        assertEquals(testUser, result.getUser());
        assertEquals(testRecipe, result.getRecipe());

        verify(recipeRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void createReview_WithInvalidRecipeId_ShouldThrowException() {
        // Given
        Review newReview = new Review();
        newReview.setRating(4);
        newReview.setComment("Great recipe!");

        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(newReview, 999L, 1L);
        });

        assertEquals("Recipe not found with id: 999", exception.getMessage());

        verify(recipeRepository, times(1)).findById(999L);
        verify(userRepository, never()).findById(anyLong());
        verify(reviewRepository, never()).save(any(Review.class));
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void createReview_WithInvalidUserId_ShouldThrowException() {
        // Given
        Review newReview = new Review();
        newReview.setRating(4);
        newReview.setComment("Great recipe!");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(newReview, 1L, 999L);
        });

        assertEquals("User not found with id: 999", exception.getMessage());

        verify(recipeRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(999L);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void createReview_WithNullReview_ShouldThrowException() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            reviewService.createReview(null, 1L, 1L);
        });

        verify(recipeRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void getReviewById_WithValidId_ShouldReturnReview() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        Review result = reviewService.getReviewById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testReview.getId(), result.getId());
        assertEquals(testReview.getRating(), result.getRating());
        assertEquals(testReview.getComment(), result.getComment());

        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    void getReviewById_WithInvalidId_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getReviewById(999L);
        });

        assertEquals("Review not found with id: 999", exception.getMessage());

        verify(reviewRepository, times(1)).findById(999L);
    }

    @Test
    void getReviewById_WithNullId_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.getReviewById(null);
        });

        assertEquals("Review not found with id: null", exception.getMessage());

        verify(reviewRepository, times(1)).findById(null);
    }

    @Test
    void getReviewsByRecipeId_ShouldReturnReviewsList() {
        // Given
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByRecipeId(1L)).thenReturn(reviews);

        // When
        List<Review> result = reviewService.getReviewsByRecipeId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testReview.getId(), result.get(0).getId());
        assertEquals(testReview.getRating(), result.get(0).getRating());

        verify(reviewRepository, times(1)).findByRecipeId(1L);
    }

    @Test
    void getReviewsByRecipeId_WithNoReviews_ShouldReturnEmptyList() {
        // Given
        when(reviewRepository.findByRecipeId(1L)).thenReturn(Collections.emptyList());

        // When
        List<Review> result = reviewService.getReviewsByRecipeId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(reviewRepository, times(1)).findByRecipeId(1L);
    }

    @Test
    void getReviewsByRecipeId_WithNullRecipeId_ShouldPassNullToRepository() {
        // Given
        when(reviewRepository.findByRecipeId(null)).thenReturn(Collections.emptyList());

        // When
        List<Review> result = reviewService.getReviewsByRecipeId(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(reviewRepository, times(1)).findByRecipeId(null);
    }

    @Test
    void updateReview_WithValidOwner_ShouldReturnUpdatedReview() {
        // Given
        Review updatedReviewDetails = new Review();
        updatedReviewDetails.setRating(3);
        updatedReviewDetails.setComment("Updated comment - not as good as expected.");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        Review result = reviewService.updateReview(1L, updatedReviewDetails, 1L);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getRating());
        assertEquals("Updated comment - not as good as expected.", result.getComment());

        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(testReview);
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void updateReview_WithInvalidReviewId_ShouldThrowException() {
        // Given
        Review updatedReviewDetails = new Review();
        updatedReviewDetails.setRating(3);
        updatedReviewDetails.setComment("Updated comment");

        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.updateReview(999L, updatedReviewDetails, 1L);
        });

        assertEquals("Review not found with id: 999", exception.getMessage());

        verify(reviewRepository, times(1)).findById(999L);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void updateReview_WithNonOwner_ShouldThrowException() {
        // Given
        Review updatedReviewDetails = new Review();
        updatedReviewDetails.setRating(3);
        updatedReviewDetails.setComment("Updated comment");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.updateReview(1L, updatedReviewDetails, 2L); // Different user ID
        });

        assertEquals("You can only update your own reviews", exception.getMessage());

        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void updateReview_WithNullReviewDetails_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            reviewService.updateReview(1L, null, 1L);
        });

        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, never()).save(any(Review.class));
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void deleteReview_WithValidOwner_ShouldDeleteReview() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        reviewService.deleteReview(1L, 1L);

        // Then
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).deleteById(1L);
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void deleteReview_WithInvalidReviewId_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(999L, 1L);
        });

        assertEquals("Review not found with id: 999", exception.getMessage());

        verify(reviewRepository, times(1)).findById(999L);
        verify(reviewRepository, never()).deleteById(anyLong());
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void deleteReview_WithNonOwner_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(1L, 2L); // Different user ID
        });

        assertEquals("You can only delete your own reviews", exception.getMessage());

        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, never()).deleteById(anyLong());
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void deleteReview_WithNullReviewId_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(null, 1L);
        });

        assertEquals("Review not found with id: null", exception.getMessage());

        verify(reviewRepository, times(1)).findById(null);
        verify(reviewRepository, never()).deleteById(anyLong());
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void createReview_ShouldUpdateRecipeRating() {
        // Given
        Review newReview = new Review();
        newReview.setRating(4);
        newReview.setComment("Great recipe!");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        reviewService.createReview(newReview, 1L, 1L);

        // Then
        verify(recipeRepository, times(1)).save(testRecipe); // Once for rating update
    }

    @Test
    void updateReview_ShouldUpdateRecipeRating() {
        // Given
        Review updatedReviewDetails = new Review();
        updatedReviewDetails.setRating(3);
        updatedReviewDetails.setComment("Updated comment");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        reviewService.updateReview(1L, updatedReviewDetails, 1L);

        // Then
        verify(recipeRepository, times(1)).save(testRecipe); // For rating update
    }

    @Test
    void deleteReview_ShouldUpdateRecipeRating() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        reviewService.deleteReview(1L, 1L);

        // Then
        verify(recipeRepository, times(1)).save(testRecipe); // For rating update
    }

    @Test
    void createReview_WithMultipleReviews_ShouldUpdateRecipeRatingCorrectly() {
        // Given
        Review newReview = new Review();
        newReview.setRating(5);
        newReview.setComment("Amazing recipe!");

        // Create a recipe with existing reviews
        Recipe recipeWithReviews = new Recipe();
        recipeWithReviews.setId(1L);
        recipeWithReviews.setTitle("Test Recipe");
        recipeWithReviews.setRating(4.0);
        recipeWithReviews.setRatingCount(2);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipeWithReviews));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipeWithReviews);

        // When
        reviewService.createReview(newReview, 1L, 1L);

        // Then
        verify(recipeRepository, times(1)).save(recipeWithReviews);
    }

    @Test
    void createReview_WithInvalidRating_ShouldStillSaveReview() {
        // Given
        Review newReview = new Review();
        newReview.setRating(0); // Invalid rating
        newReview.setComment("Invalid rating test");

        // Create a review with the expected values
        Review expectedReview = new Review();
        expectedReview.setId(1L);
        expectedReview.setRating(0);
        expectedReview.setComment("Invalid rating test");
        expectedReview.setUser(testUser);
        expectedReview.setRecipe(testRecipe);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(expectedReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        Review result = reviewService.createReview(newReview, 1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getRating());
        assertEquals("Invalid rating test", result.getComment());

        verify(recipeRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void createReview_WithEmptyComment_ShouldSaveReview() {
        // Given
        Review newReview = new Review();
        newReview.setRating(4);
        newReview.setComment(""); // Empty comment

        // Create a review with the expected values
        Review expectedReview = new Review();
        expectedReview.setId(1L);
        expectedReview.setRating(4);
        expectedReview.setComment("");
        expectedReview.setUser(testUser);
        expectedReview.setRecipe(testRecipe);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(expectedReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        Review result = reviewService.createReview(newReview, 1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("", result.getComment());

        verify(recipeRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void createReview_WithNullComment_ShouldSaveReview() {
        // Given
        Review newReview = new Review();
        newReview.setRating(4);
        newReview.setComment(null); // Null comment

        // Create a review with the expected values
        Review expectedReview = new Review();
        expectedReview.setId(1L);
        expectedReview.setRating(4);
        expectedReview.setComment(null);
        expectedReview.setUser(testUser);
        expectedReview.setRecipe(testRecipe);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(expectedReview);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        Review result = reviewService.createReview(newReview, 1L, 1L);

        // Then
        assertNotNull(result);
        assertNull(result.getComment());

        verify(recipeRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(recipeRepository, times(1)).save(testRecipe);
    }
}
