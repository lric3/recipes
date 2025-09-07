package com.lric3.recipes.controller;

import com.lric3.recipes.entity.Review;
import com.lric3.recipes.service.ReviewService;
import com.lric3.recipes.service.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes/{recipeId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Recipe review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    private final UserContextService userContextService;

    @PostMapping
    @Operation(summary = "Create review", description = "Create a new review for a recipe")
    public ResponseEntity<Review> createReview(
            @PathVariable Long recipeId,
            @Valid @RequestBody Review review) {

        Long userId = userContextService.getCurrentUserId();
        Review createdReview = reviewService.createReview(review, recipeId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping
    @Operation(summary = "Get recipe reviews", description = "Retrieve all reviews for a recipe")
    public ResponseEntity<List<Review>> getRecipeReviews(@PathVariable Long recipeId) {
        List<Review> reviews = reviewService.getReviewsByRecipeId(recipeId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Retrieve a specific review")
    public ResponseEntity<Review> getReviewById(
            @PathVariable Long recipeId,
            @PathVariable Long reviewId) {

        Review review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update review", description = "Update an existing review")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long recipeId,
            @PathVariable Long reviewId,
            @Valid @RequestBody Review reviewDetails) {

        Long userId = userContextService.getCurrentUserId();
        Review updatedReview = reviewService.updateReview(reviewId, reviewDetails, userId);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long recipeId,
            @PathVariable Long reviewId) {

        Long userId = userContextService.getCurrentUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
