package com.lric3.noshpit.api.repository;

import com.lric3.noshpit.api.entity.Recipe;
import com.lric3.noshpit.api.entity.User;
import com.lric3.noshpit.api.util.TestConstants;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RecipeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecipeRepository recipeRepository;

    private User testUser;
    private Recipe privateRecipe;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(TestConstants.TEST_USER_PASSWORD);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);
        testUser = entityManager.persistAndFlush(testUser);

        // Create public recipes
        Recipe publicRecipe1 = createRecipe("Public Recipe 1", "Description 1", true,
                "ITALIAN", Recipe.MealType.DINNER, Recipe.DifficultyLevel.EASY, 4.5, 10);
        Recipe publicRecipe2 = createRecipe("Public Recipe 2", "Description 2", true,
                "CHINESE", Recipe.MealType.LUNCH, Recipe.DifficultyLevel.MEDIUM, 4.0, 5);
        
        // Create private recipe
        privateRecipe = createRecipe("Private Recipe", "Private Description", false, 
                "MEXICAN", Recipe.MealType.DINNER, Recipe.DifficultyLevel.HARD, 3.5, 2);

        // Create recipes for specific tests
        Recipe italianRecipe = createRecipe("Italian Pasta", "Delicious pasta", true,
                "ITALIAN", Recipe.MealType.DINNER, Recipe.DifficultyLevel.MEDIUM, 4.8, 15);

        Recipe chineseRecipe = createRecipe("Chinese Stir Fry", "Quick stir fry", true,
                "CHINESE", Recipe.MealType.LUNCH, Recipe.DifficultyLevel.EASY, 4.2, 8);

        Recipe easyRecipe = createRecipe("Easy Soup", "Simple soup recipe", true,
                "AMERICAN", Recipe.MealType.LUNCH, Recipe.DifficultyLevel.EASY, 3.8, 12);

        Recipe hardRecipe = createRecipe("Complex Dessert", "Advanced dessert", true,
                "FRENCH", Recipe.MealType.DESSERT, Recipe.DifficultyLevel.HARD, 4.9, 3);

        Recipe dinnerRecipe = createRecipe("Dinner Special", "Special dinner", true,
                "AMERICAN", Recipe.MealType.DINNER, Recipe.DifficultyLevel.MEDIUM, 4.3, 7);

        Recipe breakfastRecipe = createRecipe("Morning Pancakes", "Fluffy pancakes", true,
                "AMERICAN", Recipe.MealType.BREAKFAST, Recipe.DifficultyLevel.EASY, 4.1, 20);

        // Add dietary restrictions to some recipes
        italianRecipe.setDietaryRestrictions(List.of(Recipe.DietaryRestriction.VEGETARIAN));
        chineseRecipe.setDietaryRestrictions(List.of(Recipe.DietaryRestriction.GLUTEN_FREE));
        easyRecipe.setDietaryRestrictions(List.of(Recipe.DietaryRestriction.VEGAN, Recipe.DietaryRestriction.GLUTEN_FREE));

        // Persist all recipes
        entityManager.persistAndFlush(publicRecipe1);
        entityManager.persistAndFlush(publicRecipe2);
        entityManager.persistAndFlush(privateRecipe);
        entityManager.persistAndFlush(italianRecipe);
        entityManager.persistAndFlush(chineseRecipe);
        entityManager.persistAndFlush(easyRecipe);
        entityManager.persistAndFlush(hardRecipe);
        entityManager.persistAndFlush(dinnerRecipe);
        entityManager.persistAndFlush(breakfastRecipe);
        entityManager.clear();
    }

    @Test
    void findByIsPublicTrue_ShouldReturnOnlyPublicRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrue(pageable);

        // Then
        assertEquals(8, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(Recipe::isPublic));
        assertFalse(result.getContent().contains(privateRecipe));
    }

    @Test
    void findByIsPublicTrue_WithPagination_ShouldReturnCorrectPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrue(pageable);

        // Then
        assertEquals(8, result.getTotalElements());
        assertEquals(3, result.getContent().size());
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void findByUserId_ShouldReturnUserRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByUserId(testUser.getId(), pageable);

        // Then
        assertEquals(9, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            recipe.getUser().getId().equals(testUser.getId())));
    }

    @Test
    void findByUserId_WithNonExistentUser_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByUserId(999L, pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findByIsPublicTrueAndTitleContainingIgnoreCase_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrueAndTitleContainingIgnoreCase("recipe", pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            recipe.getTitle().toLowerCase().contains("recipe")));
    }

    @Test
    void findByIsPublicTrueAndTitleContainingIgnoreCase_WithCaseInsensitive_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrueAndTitleContainingIgnoreCase("ITALIAN", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            recipe.getTitle().toLowerCase().contains("italian")));
    }

    @Test
    void findByIsPublicTrueAndCuisineType_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrueAndCuisineType("ITALIAN", pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            "ITALIAN".equals(recipe.getCuisineType())));
    }

    @Test
    void findByIsPublicTrueAndCuisineType_WithNonExistentCuisine_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrueAndCuisineType("NONEXISTENT", pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findByIsPublicTrueAndMealType_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrueAndMealType(Recipe.MealType.DINNER, pageable);

        // Then
        assertEquals(3, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            Recipe.MealType.DINNER.equals(recipe.getMealType())));
    }

    @Test
    void findByIsPublicTrueAndDifficultyLevel_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findByIsPublicTrueAndDifficultyLevel(Recipe.DifficultyLevel.EASY, pageable);

        // Then
        assertEquals(4, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            Recipe.DifficultyLevel.EASY.equals(recipe.getDifficultyLevel())));
    }

    @Test
    void findRecipesWithFilters_WithAllFilters_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findRecipesWithFilters(
                "ITALIAN", Recipe.MealType.DINNER, Recipe.DifficultyLevel.EASY, 30, 60, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        Recipe recipe = result.getContent().get(0);
        assertEquals("ITALIAN", recipe.getCuisineType());
        assertEquals(Recipe.MealType.DINNER, recipe.getMealType());
        assertEquals(Recipe.DifficultyLevel.EASY, recipe.getDifficultyLevel());
    }

    @Test
    void findRecipesWithFilters_WithPartialFilters_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findRecipesWithFilters(
                "ITALIAN", null, null, null, null, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            "ITALIAN".equals(recipe.getCuisineType())));
    }

    @Test
    void findRecipesWithFilters_WithTimeFilters_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findRecipesWithFilters(
                null, null, null, 20, 30, pageable);

        // Then
        assertTrue(result.getTotalElements() >= 0);
        assertTrue(result.getContent().stream().allMatch(recipe -> 
            recipe.getPrepTime() <= 20 && recipe.getCookTime() <= 30));
    }

    @Test
    void findRecipesWithFilters_WithNoFilters_ShouldReturnAllPublicRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Recipe> result = recipeRepository.findRecipesWithFilters(
                null, null, null, null, null, pageable);

        // Then
        assertEquals(8, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(Recipe::isPublic));
    }

    @Test
    void findByDietaryRestrictions_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Recipe.DietaryRestriction> restrictions = List.of(Recipe.DietaryRestriction.VEGETARIAN);

        // When
        Page<Recipe> result = recipeRepository.findByDietaryRestrictions(restrictions, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getDietaryRestrictions().contains(Recipe.DietaryRestriction.VEGETARIAN));
    }

    @Test
    void findByDietaryRestrictions_WithMultipleRestrictions_ShouldReturnMatchingRecipes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Recipe.DietaryRestriction> restrictions = List.of(
                Recipe.DietaryRestriction.VEGAN, Recipe.DietaryRestriction.GLUTEN_FREE);

        // When
        Page<Recipe> result = recipeRepository.findByDietaryRestrictions(restrictions, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(recipe -> 
            recipe.getDietaryRestrictions().contains(Recipe.DietaryRestriction.VEGAN) &&
            recipe.getDietaryRestrictions().contains(Recipe.DietaryRestriction.GLUTEN_FREE)));
    }

    @Test
    void findTop10ByIsPublicTrueOrderByRatingDesc_ShouldReturnTopRatedRecipes() {
        // When
        List<Recipe> result = recipeRepository.findTop10ByIsPublicTrueOrderByRatingDesc();

        // Then
        assertEquals(8, result.size());
        assertTrue(result.stream().allMatch(Recipe::isPublic));
        
        // Verify ordering (should be sorted by rating descending)
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getRating() >= result.get(i + 1).getRating());
        }
    }

    @Test
    void findTop10ByIsPublicTrueOrderByFavoriteCountDesc_ShouldReturnTopFavoriteRecipes() {
        // When
        List<Recipe> result = recipeRepository.findTop10ByIsPublicTrueOrderByFavoriteCountDesc();

        // Then
        assertEquals(8, result.size());
        assertTrue(result.stream().allMatch(Recipe::isPublic));
        
        // Verify ordering (should be sorted by favorite count descending)
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getFavoriteCount() >= result.get(i + 1).getFavoriteCount());
        }
    }

    // Helper method to create recipes
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
        recipe.setUser(testUser);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        return recipe;
    }
}
