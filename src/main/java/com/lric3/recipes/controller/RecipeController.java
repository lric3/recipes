package com.lric3.recipes.controller;

import com.lric3.recipes.entity.Recipe;
import com.lric3.recipes.service.RecipeService;
import com.lric3.recipes.service.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "Recipe management APIs")
public class RecipeController {

    private final RecipeService recipeService;

    private final UserContextService userContextService;

    @PostMapping
    @Operation(summary = "Create recipe", description = "Create a new recipe")
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody Recipe recipe) {
        Long userId = userContextService.getCurrentUserId();
        Recipe createdRecipe = recipeService.createRecipe(recipe, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by ID", description = "Retrieve a recipe by its ID")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping
    @Operation(summary = "Get all public recipes", description = "Retrieve all public recipes with pagination")
    public ResponseEntity<Page<Recipe>> getAllRecipes(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Recipe> recipes = recipeService.getAllPublicRecipes(pageable);
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search recipes by title", description = "Search public recipes by title")
    public ResponseEntity<Page<Recipe>> searchRecipes(
            @Parameter(description = "Search term") @RequestParam String title,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.searchRecipesByTitle(title, pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/cuisine/{cuisineType}")
    @Operation(summary = "Get recipes by cuisine type", description = "Retrieve recipes by cuisine type")
    public ResponseEntity<Page<Recipe>> getRecipesByCuisineType(
            @PathVariable String cuisineType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipesByCuisineType(cuisineType, pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/meal-type/{mealType}")
    @Operation(summary = "Get recipes by meal type", description = "Retrieve recipes by meal type")
    public ResponseEntity<Page<Recipe>> getRecipesByMealType(
            @PathVariable Recipe.MealType mealType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipesByMealType(mealType, pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/difficulty/{difficultyLevel}")
    @Operation(summary = "Get recipes by difficulty level", description = "Retrieve recipes by difficulty level")
    public ResponseEntity<Page<Recipe>> getRecipesByDifficultyLevel(
            @PathVariable Recipe.DifficultyLevel difficultyLevel,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipesByDifficultyLevel(difficultyLevel, pageable);
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/filter")
    @Operation(summary = "Filter recipes", description = "Filter recipes by multiple criteria")
    public ResponseEntity<Page<Recipe>> filterRecipes(
            @Parameter(description = "Cuisine type") @RequestParam(required = false) String cuisineType,
            @Parameter(description = "Meal type") @RequestParam(required = false) Recipe.MealType mealType,
            @Parameter(description = "Difficulty level") @RequestParam(required = false) Recipe.DifficultyLevel difficultyLevel,
            @Parameter(description = "Maximum prep time in minutes") @RequestParam(required = false) Integer maxPrepTime,
            @Parameter(description = "Maximum cook time in minutes") @RequestParam(required = false) Integer maxCookTime,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipesWithFilters(
                cuisineType, mealType, difficultyLevel, maxPrepTime, maxCookTime, pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated recipes", description = "Retrieve top 10 rated recipes")
    public ResponseEntity<List<Recipe>> getTopRatedRecipes() {
        List<Recipe> recipes = recipeService.getTopRatedRecipes();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/top-favorites")
    @Operation(summary = "Get top favorite recipes", description = "Retrieve top 10 favorite recipes")
    public ResponseEntity<List<Recipe>> getTopFavoriteRecipes() {
        List<Recipe> recipes = recipeService.getTopFavoriteRecipes();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/my-recipes")
    @Operation(summary = "Get user's recipes", description = "Retrieve recipes created by the current user")
    public ResponseEntity<Page<Recipe>> getMyRecipes(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Long userId = userContextService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = recipeService.getRecipesByUser(userId, pageable);
        return ResponseEntity.ok(recipes);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recipe", description = "Update an existing recipe")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody Recipe recipeDetails) {

        Long userId = userContextService.getCurrentUserId();
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDetails, userId);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recipe", description = "Delete a recipe")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        Long userId = userContextService.getCurrentUserId();
        recipeService.deleteRecipe(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/favorite")
    @Operation(summary = "Toggle favorite", description = "Toggle favorite status for a recipe")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        recipeService.toggleFavorite(id);
        return ResponseEntity.ok().build();
    }
}
