package com.lric3.recipes.service;

import com.lric3.recipes.entity.Recipe;
import com.lric3.recipes.entity.User;
import com.lric3.recipes.repository.RecipeRepository;
import com.lric3.recipes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    
    private final UserRepository userRepository;
    
    public Recipe createRecipe(Recipe recipe, Long userId) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        recipe.setUser(userOpt.get());
        return recipeRepository.save(recipe);
    }
    
    public Recipe getRecipeById(Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);
        if (recipe.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found with id: " + id);
        }
        return recipe.get();
    }
    
    public Page<Recipe> getAllPublicRecipes(Pageable pageable) {
        return recipeRepository.findByIsPublicTrue(pageable);
    }
    
    public Page<Recipe> getRecipesByUser(Long userId, Pageable pageable) {
        return recipeRepository.findByUserId(userId, pageable);
    }
    
    public Page<Recipe> searchRecipesByTitle(String title, Pageable pageable) {
        return recipeRepository.findByIsPublicTrueAndTitleContainingIgnoreCase(title, pageable);
    }
    
    public Page<Recipe> getRecipesByCuisineType(String cuisineType, Pageable pageable) {
        return recipeRepository.findByIsPublicTrueAndCuisineType(cuisineType, pageable);
    }
    
    public Page<Recipe> getRecipesByMealType(Recipe.MealType mealType, Pageable pageable) {
        return recipeRepository.findByIsPublicTrueAndMealType(mealType, pageable);
    }
    
    public Page<Recipe> getRecipesByDifficultyLevel(Recipe.DifficultyLevel difficultyLevel, Pageable pageable) {
        return recipeRepository.findByIsPublicTrueAndDifficultyLevel(difficultyLevel, pageable);
    }
    
    public Page<Recipe> getRecipesWithFilters(
            String cuisineType,
            Recipe.MealType mealType,
            Recipe.DifficultyLevel difficultyLevel,
            Integer maxPrepTime,
            Integer maxCookTime,
            Pageable pageable) {
        return recipeRepository.findRecipesWithFilters(
                cuisineType, mealType, difficultyLevel, maxPrepTime, maxCookTime, pageable);
    }
    
    public Page<Recipe> getRecipesByDietaryRestrictions(
            List<Recipe.DietaryRestriction> dietaryRestrictions,
            Pageable pageable) {
        return recipeRepository.findByDietaryRestrictions(dietaryRestrictions, pageable);
    }
    
    public List<Recipe> getTopRatedRecipes() {
        return recipeRepository.findTop10ByIsPublicTrueOrderByRatingDesc();
    }
    
    public List<Recipe> getTopFavoriteRecipes() {
        return recipeRepository.findTop10ByIsPublicTrueOrderByFavoriteCountDesc();
    }
    
    public Recipe updateRecipe(Long id, Recipe recipeDetails, Long userId) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(id);
        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found with id: " + id);
        }
        
        Recipe recipe = recipeOpt.get();
        
        // Check if user owns the recipe
        if (!recipe.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own recipes");
        }
        
        // Update recipe fields
        recipe.setTitle(recipeDetails.getTitle());
        recipe.setDescription(recipeDetails.getDescription());
        recipe.setPrepTime(recipeDetails.getPrepTime());
        recipe.setCookTime(recipeDetails.getCookTime());
        recipe.setServings(recipeDetails.getServings());
        recipe.setDifficultyLevel(recipeDetails.getDifficultyLevel());
        recipe.setCuisineType(recipeDetails.getCuisineType());
        recipe.setMealType(recipeDetails.getMealType());
        recipe.setDietaryRestrictions(recipeDetails.getDietaryRestrictions());
        recipe.setImageUrl(recipeDetails.getImageUrl());
        recipe.setVideoUrl(recipeDetails.getVideoUrl());
        recipe.setPublic(recipeDetails.isPublic());
        
        return recipeRepository.save(recipe);
    }
    
    public void deleteRecipe(Long id, Long userId) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(id);
        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found with id: " + id);
        }
        
        Recipe recipe = recipeOpt.get();
        
        // Check if user owns the recipe
        if (!recipe.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own recipes");
        }
        
        recipeRepository.deleteById(id);
    }
    
    public void toggleFavorite(Long recipeId) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
        if (recipeOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipe not found with id: " + recipeId);
        }
        
        Recipe recipe = recipeOpt.get();
        recipe.setFavoriteCount(recipe.getFavoriteCount() + 1);
        recipeRepository.save(recipe);
    }
}
