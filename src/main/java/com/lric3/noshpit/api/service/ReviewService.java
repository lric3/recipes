package com.lric3.noshpit.api.service;

import com.lric3.noshpit.api.entity.Recipe;
import com.lric3.noshpit.api.entity.Review;
import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.repository.RecipeRepository;
import com.lric3.noshpit.api.repository.ReviewRepository;
import com.lric3.noshpit.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    
    private final RecipeRepository recipeRepository;
    
    private final UserRepository userRepository;
    
    public Review createReview(Review review, Long recipeId, Long userId) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found with id: " + recipeId);
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        
        Recipe recipe = recipeOpt.get();
        User user = userOpt.get();
        
        review.setRecipe(recipe);
        review.setUser(user);
        
        Review savedReview = reviewRepository.save(review);
        
        // Update recipe rating
        recipe.addReview(savedReview);
        recipeRepository.save(recipe);
        
        return savedReview;
    }
    
    public Review getReviewById(Long reviewId) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new IllegalArgumentException("Review not found with id: " + reviewId);
        }
        return review.get();
    }
    
    public List<Review> getReviewsByRecipeId(Long recipeId) {
        return reviewRepository.findByRecipeId(recipeId);
    }
    
    public Review updateReview(Long reviewId, Review reviewDetails, Long userId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new IllegalArgumentException("Review not found with id: " + reviewId);
        }
        
        Review review = reviewOpt.get();
        
        // Check if user owns the review
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own reviews");
        }
        
        // Update review fields
        review.setRating(reviewDetails.getRating());
        review.setComment(reviewDetails.getComment());
        
        Review savedReview = reviewRepository.save(review);
        
        // Update recipe rating
        Recipe recipe = review.getRecipe();
        recipe.addReview(savedReview);
        recipeRepository.save(recipe);
        
        return savedReview;
    }
    
    public void deleteReview(Long reviewId, Long userId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new IllegalArgumentException("Review not found with id: " + reviewId);
        }
        
        Review review = reviewOpt.get();
        
        // Check if user owns the review
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }
        
        Recipe recipe = review.getRecipe();
        reviewRepository.deleteById(reviewId);
        
        // Update recipe rating
        recipe.removeReview(review);
        recipeRepository.save(recipe);
    }
}
