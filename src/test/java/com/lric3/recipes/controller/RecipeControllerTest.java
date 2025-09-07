package com.lric3.recipes.controller;

import com.lric3.recipes.entity.Recipe;
import com.lric3.recipes.entity.User;
import com.lric3.recipes.service.RecipeService;
import com.lric3.recipes.service.UserContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private RecipeController recipeController;

    @Test
    void createRecipe_WithValidData_ShouldReturnCreatedRecipe() {
        // Given
        Long userId = 1L;
        Recipe recipe = createTestRecipe();
        Recipe savedRecipe = createTestRecipe();
        savedRecipe.setId(1L);

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(recipeService.createRecipe(any(Recipe.class), eq(userId))).thenReturn(savedRecipe);

        // When
        ResponseEntity<Recipe> response = recipeController.createRecipe(recipe);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Recipe", response.getBody().getTitle());
        assertEquals("Test Description", response.getBody().getDescription());

        verify(userContextService, times(1)).getCurrentUserId();
        verify(recipeService, times(1)).createRecipe(recipe, userId);
    }

    @Test
    void getRecipeById_WithValidId_ShouldReturnRecipe() {
        // Given
        Long recipeId = 1L;
        Recipe recipe = createTestRecipe();
        recipe.setId(recipeId);

        when(recipeService.getRecipeById(recipeId)).thenReturn(recipe);

        // When
        ResponseEntity<Recipe> response = recipeController.getRecipeById(recipeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(recipeId, response.getBody().getId());
        assertEquals("Test Recipe", response.getBody().getTitle());

        verify(recipeService, times(1)).getRecipeById(recipeId);
    }

    @Test
    void getRecipeById_WithInvalidId_ShouldThrowException() {
        // Given
        Long recipeId = 999L;

        when(recipeService.getRecipeById(recipeId))
                .thenThrow(new IllegalArgumentException("Recipe not found with id: " + recipeId));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            recipeController.getRecipeById(recipeId));

        verify(recipeService, times(1)).getRecipeById(recipeId);
    }

    @Test
    void getAllRecipes_WithDefaultParameters_ShouldReturnPagedRecipes() {
        // Given
        List<Recipe> recipes = Arrays.asList(createTestRecipe(), createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 2);

        when(recipeService.getAllPublicRecipes(any(Pageable.class))).thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.getAllRecipes(0, 10, "createdAt", "desc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());

        verify(recipeService, times(1)).getAllPublicRecipes(any(Pageable.class));
    }

    @Test
    void getAllRecipes_WithCustomParameters_ShouldReturnPagedRecipes() {
        // Given
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(1, 5), 6);

        when(recipeService.getAllPublicRecipes(any(Pageable.class))).thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.getAllRecipes(1, 5, "title", "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(6, response.getBody().getTotalElements());

        verify(recipeService, times(1)).getAllPublicRecipes(any(Pageable.class));
    }

    @Test
    void searchRecipes_WithValidTitle_ShouldReturnPagedRecipes() {
        // Given
        String searchTitle = "pasta";
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 1);

        when(recipeService.searchRecipesByTitle(eq(searchTitle), any(Pageable.class))).thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.searchRecipes(searchTitle, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(recipeService, times(1)).searchRecipesByTitle(eq(searchTitle), any(Pageable.class));
    }

    @Test
    void getRecipesByCuisineType_WithValidCuisine_ShouldReturnPagedRecipes() {
        // Given
        String cuisineType = "Italian";
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 1);

        when(recipeService.getRecipesByCuisineType(eq(cuisineType), any(Pageable.class))).thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.getRecipesByCuisineType(cuisineType, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(recipeService, times(1)).getRecipesByCuisineType(eq(cuisineType), any(Pageable.class));
    }

    @Test
    void getRecipesByMealType_WithValidMealType_ShouldReturnPagedRecipes() {
        // Given
        Recipe.MealType mealType = Recipe.MealType.DINNER;
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 1);

        when(recipeService.getRecipesByMealType(eq(mealType), any(Pageable.class))).thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.getRecipesByMealType(mealType, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(recipeService, times(1)).getRecipesByMealType(eq(mealType), any(Pageable.class));
    }

    @Test
    void getRecipesByDifficultyLevel_WithValidDifficulty_ShouldReturnPagedRecipes() {
        // Given
        Recipe.DifficultyLevel difficultyLevel = Recipe.DifficultyLevel.EASY;
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 1);

        when(recipeService.getRecipesByDifficultyLevel(eq(difficultyLevel), any(Pageable.class))).thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.getRecipesByDifficultyLevel(difficultyLevel, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(recipeService, times(1)).getRecipesByDifficultyLevel(eq(difficultyLevel), any(Pageable.class));
    }

    @Test
    void filterRecipes_WithAllFilters_ShouldReturnPagedRecipes() {
        // Given
        String cuisineType = "Italian";
        Recipe.MealType mealType = Recipe.MealType.DINNER;
        Recipe.DifficultyLevel difficultyLevel = Recipe.DifficultyLevel.MEDIUM;
        Integer maxPrepTime = 30;
        Integer maxCookTime = 60;
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 1);

        when(recipeService.getRecipesWithFilters(
                eq(cuisineType), eq(mealType), eq(difficultyLevel), 
                eq(maxPrepTime), eq(maxCookTime), any(Pageable.class)))
                .thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.filterRecipes(
                cuisineType, mealType, difficultyLevel, maxPrepTime, maxCookTime, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(recipeService, times(1)).getRecipesWithFilters(
                eq(cuisineType), eq(mealType), eq(difficultyLevel), eq(maxPrepTime), eq(maxCookTime), any(Pageable.class));
    }

    @Test
    void filterRecipes_WithPartialFilters_ShouldReturnPagedRecipes() {
        // Given
        String cuisineType = "Italian";
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 1);

        when(recipeService.getRecipesWithFilters(
                eq(cuisineType), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.filterRecipes(
                cuisineType, null, null, null, null, 0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(recipeService, times(1)).getRecipesWithFilters(
                eq(cuisineType), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getTopRatedRecipes_ShouldReturnTopRatedRecipes() {
        // Given
        List<Recipe> recipes = Arrays.asList(createTestRecipe(), createTestRecipe());
        recipes.get(0).setRating(4.8);
        recipes.get(1).setRating(4.5);

        when(recipeService.getTopRatedRecipes()).thenReturn(recipes);

        // When
        ResponseEntity<List<Recipe>> response = recipeController.getTopRatedRecipes();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(4.8, response.getBody().get(0).getRating());

        verify(recipeService, times(1)).getTopRatedRecipes();
    }

    @Test
    void getTopFavoriteRecipes_ShouldReturnTopFavoriteRecipes() {
        // Given
        List<Recipe> recipes = Arrays.asList(createTestRecipe(), createTestRecipe());
        recipes.get(0).setFavoriteCount(150);
        recipes.get(1).setFavoriteCount(120);

        when(recipeService.getTopFavoriteRecipes()).thenReturn(recipes);

        // When
        ResponseEntity<List<Recipe>> response = recipeController.getTopFavoriteRecipes();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(150, response.getBody().get(0).getFavoriteCount());

        verify(recipeService, times(1)).getTopFavoriteRecipes();
    }

    @Test
    void getMyRecipes_WithValidUser_ShouldReturnUserRecipes() {
        // Given
        Long userId = 1L;
        List<Recipe> recipes = List.of(createTestRecipe());
        Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(0, 10), 1);

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(recipeService.getRecipesByUser(eq(userId), any(Pageable.class))).thenReturn(recipePage);

        // When
        ResponseEntity<Page<Recipe>> response = recipeController.getMyRecipes(0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(userContextService, times(1)).getCurrentUserId();
        verify(recipeService, times(1)).getRecipesByUser(eq(userId), any(Pageable.class));
    }

    @Test
    void updateRecipe_WithValidData_ShouldReturnUpdatedRecipe() {
        // Given
        Long recipeId = 1L;
        Long userId = 1L;
        Recipe recipeDetails = createTestRecipe();
        recipeDetails.setTitle("Updated Recipe Title");
        Recipe updatedRecipe = createTestRecipe();
        updatedRecipe.setId(recipeId);
        updatedRecipe.setTitle("Updated Recipe Title");

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(recipeService.updateRecipe(eq(recipeId), eq(recipeDetails), eq(userId))).thenReturn(updatedRecipe);

        // When
        ResponseEntity<Recipe> response = recipeController.updateRecipe(recipeId, recipeDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(recipeId, response.getBody().getId());
        assertEquals("Updated Recipe Title", response.getBody().getTitle());

        verify(userContextService, times(1)).getCurrentUserId();
        verify(recipeService, times(1)).updateRecipe(recipeId, recipeDetails, userId);
    }

    @Test
    void updateRecipe_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long userId = 1L;
        Recipe recipeDetails = createTestRecipe();

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        when(recipeService.updateRecipe(eq(recipeId), eq(recipeDetails), eq(userId)))
                .thenThrow(new IllegalArgumentException("You can only update your own recipes"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            recipeController.updateRecipe(recipeId, recipeDetails));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(recipeService, times(1)).updateRecipe(recipeId, recipeDetails, userId);
    }

    @Test
    void deleteRecipe_WithValidData_ShouldReturnNoContent() {
        // Given
        Long recipeId = 1L;
        Long userId = 1L;

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        doNothing().when(recipeService).deleteRecipe(recipeId, userId);

        // When
        ResponseEntity<Void> response = recipeController.deleteRecipe(recipeId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(userContextService, times(1)).getCurrentUserId();
        verify(recipeService, times(1)).deleteRecipe(recipeId, userId);
    }

    @Test
    void deleteRecipe_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        Long recipeId = 1L;
        Long userId = 1L;

        when(userContextService.getCurrentUserId()).thenReturn(userId);
        doThrow(new IllegalArgumentException("You can only delete your own recipes"))
                .when(recipeService).deleteRecipe(recipeId, userId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            recipeController.deleteRecipe(recipeId));

        verify(userContextService, times(1)).getCurrentUserId();
        verify(recipeService, times(1)).deleteRecipe(recipeId, userId);
    }

    @Test
    void toggleFavorite_WithValidData_ShouldReturnOk() {
        // Given
        Long recipeId = 1L;

        doNothing().when(recipeService).toggleFavorite(recipeId);

        // When
        ResponseEntity<Void> response = recipeController.toggleFavorite(recipeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(recipeService, times(1)).toggleFavorite(recipeId);
    }

    @Test
    void toggleFavorite_WithInvalidRecipeId_ShouldThrowException() {
        // Given
        Long recipeId = 999L;

        doThrow(new IllegalArgumentException("Recipe not found with id: " + recipeId))
                .when(recipeService).toggleFavorite(recipeId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            recipeController.toggleFavorite(recipeId));

        verify(recipeService, times(1)).toggleFavorite(recipeId);
    }

    // Helper methods
    private Recipe createTestRecipe() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setDescription("Test Description");
        recipe.setPrepTime(15);
        recipe.setCookTime(30);
        recipe.setServings(4);
        recipe.setDifficultyLevel(Recipe.DifficultyLevel.EASY);
        recipe.setCuisineType("Italian");
        recipe.setMealType(Recipe.MealType.DINNER);
        recipe.setPublic(true);
        recipe.setRating(4.5);
        recipe.setRatingCount(10);
        recipe.setFavoriteCount(25);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        
        // Create a mock user
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        recipe.setUser(user);
        
        return recipe;
    }
}
