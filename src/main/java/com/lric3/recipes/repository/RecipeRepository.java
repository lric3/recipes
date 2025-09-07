package com.lric3.recipes.repository;

import com.lric3.recipes.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByIsPublicTrue(Pageable pageable);

    Page<Recipe> findByUserId(Long userId, Pageable pageable);

    Page<Recipe> findByIsPublicTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Recipe> findByIsPublicTrueAndCuisineType(String cuisineType, Pageable pageable);

    Page<Recipe> findByIsPublicTrueAndMealType(Recipe.MealType mealType, Pageable pageable);

    Page<Recipe> findByIsPublicTrueAndDifficultyLevel(Recipe.DifficultyLevel difficultyLevel, Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.isPublic = true AND " +
           "(:cuisineType IS NULL OR r.cuisineType = :cuisineType) AND " +
           "(:mealType IS NULL OR r.mealType = :mealType) AND " +
           "(:difficultyLevel IS NULL OR r.difficultyLevel = :difficultyLevel) AND " +
           "(:maxPrepTime IS NULL OR r.prepTime <= :maxPrepTime) AND " +
           "(:maxCookTime IS NULL OR r.cookTime <= :maxCookTime)")
    Page<Recipe> findRecipesWithFilters(
            @Param("cuisineType") String cuisineType,
            @Param("mealType") Recipe.MealType mealType,
            @Param("difficultyLevel") Recipe.DifficultyLevel difficultyLevel,
            @Param("maxPrepTime") Integer maxPrepTime,
            @Param("maxCookTime") Integer maxCookTime,
            Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.isPublic = true AND " +
           "EXISTS (SELECT 1 FROM r.dietaryRestrictions dr WHERE dr IN :dietaryRestrictions)")
    Page<Recipe> findByDietaryRestrictions(
            @Param("dietaryRestrictions") List<Recipe.DietaryRestriction> dietaryRestrictions,
            Pageable pageable);

    List<Recipe> findTop10ByIsPublicTrueOrderByRatingDesc();

    List<Recipe> findTop10ByIsPublicTrueOrderByFavoriteCountDesc();
}
