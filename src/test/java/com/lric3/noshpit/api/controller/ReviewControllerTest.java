package com.lric3.noshpit.api.controller;

import com.lric3.noshpit.api.entity.Recipe;
import com.lric3.noshpit.api.entity.Review;
import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.service.ReviewService;
import com.lric3.noshpit.api.service.UserContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private ReviewController reviewController;

    @Test
    void createReview_WithValidData_ShouldReturnCreatedReview() {
        // Given
        Long recipeId = 1L;
        Long userId = 1L;
        Review review = createTestReview();
        Review savedReview = createTestReview();
        savedReview.setId(1L);

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(reviewService.createReview(any(Review.class), eq(recipeId), eq(userId))).thenReturn(savedReview);

        // When
        ResponseEntity<Review> response = reviewController.createReview(recipeId, review);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great recipe!", response.getBody().getComment());

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).createReview(review, recipeId, userId);
    }

    @Test
    void createReview_WithInvalidRecipeId_ShouldThrowException() {
        // Given
        Long recipeId = 999L;
        Long userId = 1L;
        Review review = createTestReview();

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(reviewService.createReview(any(Review.class), eq(recipeId), eq(userId)))
                .thenThrow(new IllegalArgumentException("Recipe not found with id: " + recipeId));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            reviewController.createReview(recipeId, review));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).createReview(review, recipeId, userId);
    }

    @Test
    void createReview_WithInvalidUserId_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long userId = 999L;
        Review review = createTestReview();

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(reviewService.createReview(any(Review.class), eq(recipeId), eq(userId)))
                .thenThrow(new IllegalArgumentException("User not found with id: " + userId));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            reviewController.createReview(recipeId, review));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).createReview(review, recipeId, userId);
    }

    @Test
    void getRecipeReviews_WithValidRecipeId_ShouldReturnReviews() {
        // Given
        Long recipeId = 1L;
        List<Review> reviews = List.of(createTestReview(), createTestReview());
        reviews.get(0).setId(1L);
        reviews.get(0).setRating(5);
        reviews.get(1).setId(2L);
        reviews.get(1).setRating(4);

        when(reviewService.getReviewsByRecipeId(recipeId)).thenReturn(reviews);

        // When
        ResponseEntity<List<Review>> response = reviewController.getRecipeReviews(recipeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(5, response.getBody().get(0).getRating());
        assertEquals(4, response.getBody().get(1).getRating());

        verify(reviewService, times(1)).getReviewsByRecipeId(recipeId);
    }

    @Test
    void getRecipeReviews_WithNoReviews_ShouldReturnEmptyList() {
        // Given
        Long recipeId = 1L;
        List<Review> reviews = List.of();

        when(reviewService.getReviewsByRecipeId(recipeId)).thenReturn(reviews);

        // When
        ResponseEntity<List<Review>> response = reviewController.getRecipeReviews(recipeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(reviewService, times(1)).getReviewsByRecipeId(recipeId);
    }

    @Test
    void getReviewById_WithValidIds_ShouldReturnReview() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 1L;
        Review review = createTestReview();
        review.setId(reviewId);

        when(reviewService.getReviewById(reviewId)).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.getReviewById(recipeId, reviewId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(reviewId, response.getBody().getId());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great recipe!", response.getBody().getComment());

        verify(reviewService, times(1)).getReviewById(reviewId);
    }

    @Test
    void getReviewById_WithInvalidReviewId_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 999L;

        when(reviewService.getReviewById(reviewId))
                .thenThrow(new IllegalArgumentException("Review not found with id: " + reviewId));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            reviewController.getReviewById(recipeId, reviewId));

        verify(reviewService, times(1)).getReviewById(reviewId);
    }

    @Test
    void updateReview_WithValidData_ShouldReturnUpdatedReview() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 1L;
        Long userId = 1L;
        Review reviewDetails = createTestReview();
        reviewDetails.setRating(4);
        reviewDetails.setComment("Updated comment");
        Review updatedReview = createTestReview();
        updatedReview.setId(reviewId);
        updatedReview.setRating(4);
        updatedReview.setComment("Updated comment");

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(reviewService.updateReview(eq(reviewId), eq(reviewDetails), eq(userId))).thenReturn(updatedReview);

        // When
        ResponseEntity<Review> response = reviewController.updateReview(recipeId, reviewId, reviewDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(reviewId, response.getBody().getId());
        assertEquals(4, response.getBody().getRating());
        assertEquals("Updated comment", response.getBody().getComment());

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).updateReview(reviewId, reviewDetails, userId);
    }

    @Test
    void updateReview_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 1L;
        Long userId = 1L;
        Review reviewDetails = createTestReview();

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(reviewService.updateReview(eq(reviewId), eq(reviewDetails), eq(userId)))
                .thenThrow(new IllegalArgumentException("You can only update your own reviews"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            reviewController.updateReview(recipeId, reviewId, reviewDetails));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).updateReview(reviewId, reviewDetails, userId);
    }

    @Test
    void updateReview_WithInvalidReviewId_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 999L;
        Long userId = 1L;
        Review reviewDetails = createTestReview();

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(reviewService.updateReview(eq(reviewId), eq(reviewDetails), eq(userId)))
                .thenThrow(new IllegalArgumentException("Review not found with id: " + reviewId));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            reviewController.updateReview(recipeId, reviewId, reviewDetails));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).updateReview(reviewId, reviewDetails, userId);
    }

    @Test
    void deleteReview_WithValidData_ShouldReturnNoContent() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 1L;
        Long userId = 1L;

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        doNothing().when(reviewService).deleteReview(reviewId, userId);

        // When
        ResponseEntity<Void> response = reviewController.deleteReview(recipeId, reviewId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).deleteReview(reviewId, userId);
    }

    @Test
    void deleteReview_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 1L;
        Long userId = 1L;

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        doThrow(new IllegalArgumentException("You can only delete your own reviews"))
                .when(reviewService).deleteReview(reviewId, userId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            reviewController.deleteReview(recipeId, reviewId));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).deleteReview(reviewId, userId);
    }

    @Test
    void deleteReview_WithInvalidReviewId_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long reviewId = 999L;
        Long userId = 1L;

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        doThrow(new IllegalArgumentException("Review not found with id: " + reviewId))
                .when(reviewService).deleteReview(reviewId, userId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            reviewController.deleteReview(recipeId, reviewId));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(reviewService, times(1)).deleteReview(reviewId, userId);
    }

    // Helper methods
    private Review createTestReview() {
        Review review = new Review();
        review.setRating(5);
        review.setComment("Great recipe!");
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        // Create mock user
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        review.setUser(user);
        
        // Create mock recipe
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Test Recipe");
        recipe.setDescription("Test Description");
        review.setRecipe(recipe);
        
        return review;
    }
}
