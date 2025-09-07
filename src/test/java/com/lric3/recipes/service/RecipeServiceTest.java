package com.lric3.recipes.service;

import com.lric3.recipes.entity.Recipe;
import com.lric3.recipes.entity.User;
import com.lric3.recipes.repository.RecipeRepository;
import com.lric3.recipes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecipeService recipeService;

    private User testUser;
    private Recipe testRecipe;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
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

        // Create pageable
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createRecipe_WithValidUser_ShouldReturnSavedRecipe() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        Recipe result = recipeService.createRecipe(testRecipe, 1L);

        // Then
        assertNotNull(result);
        assertEquals(testRecipe.getId(), result.getId());
        assertEquals(testRecipe.getTitle(), result.getTitle());
        assertEquals(testUser, result.getUser());

        verify(userRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void createRecipe_WithInvalidUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            recipeService.createRecipe(testRecipe, 999L));

        assertEquals("User not found with id: 999", exception.getMessage());

        verify(userRepository, times(1)).findById(999L);
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void getRecipeById_WithValidId_ShouldReturnRecipe() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // When
        Recipe result = recipeService.getRecipeById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testRecipe.getId(), result.getId());
        assertEquals(testRecipe.getTitle(), result.getTitle());

        verify(recipeRepository, times(1)).findById(1L);
    }

    @Test
    void getRecipeById_WithInvalidId_ShouldThrowException() {
        // Given
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            recipeService.getRecipeById(999L));

        assertEquals("Recipe not found with id: 999", exception.getMessage());

        verify(recipeRepository, times(1)).findById(999L);
    }

    @Test
    void getAllPublicRecipes_ShouldReturnPagedRecipes() {
        // Given
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findByIsPublicTrue(pageable)).thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.getAllPublicRecipes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testRecipe.getTitle(), result.getContent().get(0).getTitle());

        verify(recipeRepository, times(1)).findByIsPublicTrue(pageable);
    }

    @Test
    void getRecipesByUser_ShouldReturnPagedRecipes() {
        // Given
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findByUserId(1L, pageable)).thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.getRecipesByUser(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testRecipe.getTitle(), result.getContent().get(0).getTitle());

        verify(recipeRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    void searchRecipesByTitle_ShouldReturnMatchingRecipes() {
        // Given
        String searchTitle = "Test";
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findByIsPublicTrueAndTitleContainingIgnoreCase(searchTitle, pageable))
                .thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.searchRecipesByTitle(searchTitle, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testRecipe.getTitle(), result.getContent().get(0).getTitle());

        verify(recipeRepository, times(1))
                .findByIsPublicTrueAndTitleContainingIgnoreCase(searchTitle, pageable);
    }

    @Test
    void getRecipesByCuisineType_ShouldReturnMatchingRecipes() {
        // Given
        String cuisineType = "Italian";
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findByIsPublicTrueAndCuisineType(cuisineType, pageable))
                .thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.getRecipesByCuisineType(cuisineType, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testRecipe.getCuisineType(), result.getContent().get(0).getCuisineType());

        verify(recipeRepository, times(1))
                .findByIsPublicTrueAndCuisineType(cuisineType, pageable);
    }

    @Test
    void getRecipesByMealType_ShouldReturnMatchingRecipes() {
        // Given
        Recipe.MealType mealType = Recipe.MealType.DINNER;
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findByIsPublicTrueAndMealType(mealType, pageable))
                .thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.getRecipesByMealType(mealType, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testRecipe.getMealType(), result.getContent().get(0).getMealType());

        verify(recipeRepository, times(1))
                .findByIsPublicTrueAndMealType(mealType, pageable);
    }

    @Test
    void getRecipesByDifficultyLevel_ShouldReturnMatchingRecipes() {
        // Given
        Recipe.DifficultyLevel difficultyLevel = Recipe.DifficultyLevel.EASY;
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findByIsPublicTrueAndDifficultyLevel(difficultyLevel, pageable))
                .thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.getRecipesByDifficultyLevel(difficultyLevel, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testRecipe.getDifficultyLevel(), result.getContent().get(0).getDifficultyLevel());

        verify(recipeRepository, times(1))
                .findByIsPublicTrueAndDifficultyLevel(difficultyLevel, pageable);
    }

    @Test
    void getRecipesWithFilters_ShouldReturnFilteredRecipes() {
        // Given
        String cuisineType = "Italian";
        Recipe.MealType mealType = Recipe.MealType.DINNER;
        Recipe.DifficultyLevel difficultyLevel = Recipe.DifficultyLevel.EASY;
        Integer maxPrepTime = 30;
        Integer maxCookTime = 60;

        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findRecipesWithFilters(
                cuisineType, mealType, difficultyLevel, maxPrepTime, maxCookTime, pageable))
                .thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.getRecipesWithFilters(
                cuisineType, mealType, difficultyLevel, maxPrepTime, maxCookTime, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(recipeRepository, times(1))
                .findRecipesWithFilters(cuisineType, mealType, difficultyLevel, maxPrepTime, maxCookTime, pageable);
    }

    @Test
    void getRecipesByDietaryRestrictions_ShouldReturnMatchingRecipes() {
        // Given
        List<Recipe.DietaryRestriction> dietaryRestrictions =
                List.of(Recipe.DietaryRestriction.VEGETARIAN);
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
        when(recipeRepository.findByDietaryRestrictions(dietaryRestrictions, pageable))
                .thenReturn(recipePage);

        // When
        Page<Recipe> result = recipeService.getRecipesByDietaryRestrictions(dietaryRestrictions, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(recipeRepository, times(1))
                .findByDietaryRestrictions(dietaryRestrictions, pageable);
    }

    @Test
    void getTopRatedRecipes_ShouldReturnTopRatedRecipes() {
        // Given
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        when(recipeRepository.findTop10ByIsPublicTrueOrderByRatingDesc()).thenReturn(recipes);

        // When
        List<Recipe> result = recipeService.getTopRatedRecipes();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRecipe.getTitle(), result.get(0).getTitle());

        verify(recipeRepository, times(1)).findTop10ByIsPublicTrueOrderByRatingDesc();
    }

    @Test
    void getTopFavoriteRecipes_ShouldReturnTopFavoriteRecipes() {
        // Given
        List<Recipe> recipes = Collections.singletonList(testRecipe);
        when(recipeRepository.findTop10ByIsPublicTrueOrderByFavoriteCountDesc()).thenReturn(recipes);

        // When
        List<Recipe> result = recipeService.getTopFavoriteRecipes();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRecipe.getTitle(), result.get(0).getTitle());

        verify(recipeRepository, times(1)).findTop10ByIsPublicTrueOrderByFavoriteCountDesc();
    }

    @Test
    void updateRecipe_WithValidOwner_ShouldReturnUpdatedRecipe() {
        // Given
        Recipe updatedRecipe = new Recipe();
        updatedRecipe.setTitle("Updated Recipe");
        updatedRecipe.setDescription("Updated description");
        updatedRecipe.setPrepTime(20);
        updatedRecipe.setCookTime(45);
        updatedRecipe.setServings(6);
        updatedRecipe.setDifficultyLevel(Recipe.DifficultyLevel.MEDIUM);
        updatedRecipe.setCuisineType("French");
        updatedRecipe.setMealType(Recipe.MealType.LUNCH);
        updatedRecipe.setDietaryRestrictions(Collections.singletonList(Recipe.DietaryRestriction.VEGAN));
        updatedRecipe.setImageUrl("http://example.com/updated-image.jpg");
        updatedRecipe.setVideoUrl("http://example.com/updated-video.mp4");
        updatedRecipe.setPublic(false);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        Recipe result = recipeService.updateRecipe(1L, updatedRecipe, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Updated Recipe", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertEquals(20, result.getPrepTime());
        assertEquals(45, result.getCookTime());
        assertEquals(6, result.getServings());
        assertEquals(Recipe.DifficultyLevel.MEDIUM, result.getDifficultyLevel());
        assertEquals("French", result.getCuisineType());
        assertEquals(Recipe.MealType.LUNCH, result.getMealType());
        assertEquals(Collections.singletonList(Recipe.DietaryRestriction.VEGAN), result.getDietaryRestrictions());
        assertEquals("http://example.com/updated-image.jpg", result.getImageUrl());
        assertEquals("http://example.com/updated-video.mp4", result.getVideoUrl());
        assertFalse(result.isPublic());

        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void updateRecipe_WithInvalidRecipeId_ShouldThrowException() {
        // Given
        Recipe updatedRecipe = new Recipe();
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            recipeService.updateRecipe(999L, updatedRecipe, 1L));

        assertEquals("Recipe not found with id: 999", exception.getMessage());

        verify(recipeRepository, times(1)).findById(999L);
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void updateRecipe_WithNonOwner_ShouldThrowException() {
        // Given
        Recipe updatedRecipe = new Recipe();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            recipeService.updateRecipe(1L, updatedRecipe, 2L); // Different user ID
        });

        assertEquals("You can only update your own recipes", exception.getMessage());

        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void deleteRecipe_WithValidOwner_ShouldDeleteRecipe() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // When
        recipeService.deleteRecipe(1L, 1L);

        // Then
        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRecipe_WithInvalidRecipeId_ShouldThrowException() {
        // Given
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            recipeService.deleteRecipe(999L, 1L));

        assertEquals("Recipe not found with id: 999", exception.getMessage());

        verify(recipeRepository, times(1)).findById(999L);
        verify(recipeRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRecipe_WithNonOwner_ShouldThrowException() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            recipeService.deleteRecipe(1L, 2L); // Different user ID
        });

        assertEquals("You can only delete your own recipes", exception.getMessage());

        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, never()).deleteById(anyLong());
    }

    @Test
    void toggleFavorite_WithValidRecipeId_ShouldIncrementFavoriteCount() {
        // Given
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        // When
        recipeService.toggleFavorite(1L);

        // Then
        verify(recipeRepository, times(1)).findById(1L);
        verify(recipeRepository, times(1)).save(testRecipe);
    }

    @Test
    void toggleFavorite_WithInvalidRecipeId_ShouldThrowException() {
        // Given
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            recipeService.toggleFavorite(999L));

        assertEquals("Recipe not found with id: 999", exception.getMessage());

        verify(recipeRepository, times(1)).findById(999L);
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void createRecipe_WithNullRecipe_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(NullPointerException.class, () ->
            recipeService.createRecipe(null, 1L));

        verify(userRepository, times(1)).findById(1L);
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void createRecipe_WithNullUserId_ShouldThrowException() {
        // Given
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            recipeService.createRecipe(testRecipe, null));

        verify(userRepository, times(1)).findById(null);
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void getRecipeById_WithNullId_ShouldThrowException() {
        // Given
        when(recipeRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            recipeService.getRecipeById(null));

        verify(recipeRepository, times(1)).findById(null);
    }

    @Test
    void getAllPublicRecipes_WithNullPageable_ShouldPassNullToRepository() {
        // Given
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(recipeRepository.findByIsPublicTrue(null)).thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.getAllPublicRecipes(null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(recipeRepository, times(1)).findByIsPublicTrue(null);
    }

    @Test
    void searchRecipesByTitle_WithNullTitle_ShouldPassNullToRepository() {
        // Given
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(recipeRepository.findByIsPublicTrueAndTitleContainingIgnoreCase(null, pageable))
                .thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.searchRecipesByTitle(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(recipeRepository, times(1))
                .findByIsPublicTrueAndTitleContainingIgnoreCase(null, pageable);
    }

    @Test
    void searchRecipesByTitle_WithEmptyTitle_ShouldReturnEmptyResults() {
        // Given
        String emptyTitle = "";
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(recipeRepository.findByIsPublicTrueAndTitleContainingIgnoreCase(emptyTitle, pageable))
                .thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.searchRecipesByTitle(emptyTitle, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(recipeRepository, times(1))
                .findByIsPublicTrueAndTitleContainingIgnoreCase(emptyTitle, pageable);
    }

    @Test
    void getRecipesByCuisineType_WithNullCuisineType_ShouldPassNullToRepository() {
        // Given
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(recipeRepository.findByIsPublicTrueAndCuisineType(null, pageable))
                .thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.getRecipesByCuisineType(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(recipeRepository, times(1)).findByIsPublicTrueAndCuisineType(null, pageable);
    }

    @Test
    void getRecipesByMealType_WithNullMealType_ShouldPassNullToRepository() {
        // Given
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(recipeRepository.findByIsPublicTrueAndMealType(null, pageable))
                .thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.getRecipesByMealType(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(recipeRepository, times(1)).findByIsPublicTrueAndMealType(null, pageable);
    }

    @Test
    void getRecipesByDifficultyLevel_WithNullDifficultyLevel_ShouldPassNullToRepository() {
        // Given
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(recipeRepository.findByIsPublicTrueAndDifficultyLevel(null, pageable))
                .thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.getRecipesByDifficultyLevel(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(recipeRepository, times(1)).findByIsPublicTrueAndDifficultyLevel(null, pageable);
    }

    @Test
    void getRecipesByDietaryRestrictions_WithNullRestrictions_ShouldPassNullToRepository() {
        // Given
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(recipeRepository.findByDietaryRestrictions(null, pageable))
                .thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.getRecipesByDietaryRestrictions(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(recipeRepository, times(1)).findByDietaryRestrictions(null, pageable);
    }

    @Test
    void getRecipesByDietaryRestrictions_WithEmptyRestrictions_ShouldReturnEmptyResults() {
        // Given
        List<Recipe.DietaryRestriction> emptyRestrictions = Collections.emptyList();
        Page<Recipe> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(recipeRepository.findByDietaryRestrictions(emptyRestrictions, pageable))
                .thenReturn(emptyPage);

        // When
        Page<Recipe> result = recipeService.getRecipesByDietaryRestrictions(emptyRestrictions, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(recipeRepository, times(1)).findByDietaryRestrictions(emptyRestrictions, pageable);
    }
}
