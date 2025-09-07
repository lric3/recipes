package com.lric3.recipes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "prep_time")
    @Positive(message = "Prep time must be positive")
    private Integer prepTime; // in minutes

    @Column(name = "cook_time")
    @Positive(message = "Cook time must be positive")
    private Integer cookTime; // in minutes

    @Column(name = "total_time")
    private Integer totalTime; // in minutes

    @Column(name = "servings")
    @Positive(message = "Servings must be positive")
    private Integer servings;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(name = "cuisine_type")
    private String cuisineType;

    @Column(name = "meal_type")
    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @Column(name = "dietary_restrictions")
    @ElementCollection
    @CollectionTable(name = "recipe_dietary_restrictions", joinColumns = @JoinColumn(name = "recipe_id"))
    @Enumerated(EnumType.STRING)
    private List<DietaryRestriction> dietaryRestrictions = new ArrayList<>();

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "is_public")
    private boolean isPublic = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<Instruction> instructions = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalTime();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalTime();
    }

    private void calculateTotalTime() {
        if (prepTime != null && cookTime != null) {
            this.totalTime = prepTime + cookTime;
        }
    }

    public Recipe() {}

    public Recipe(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }

    // Helper methods
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setRecipe(null);
    }

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
        instruction.setRecipe(this);
    }

    public void removeInstruction(Instruction instruction) {
        instructions.remove(instruction);
        instruction.setRecipe(null);
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setRecipe(this);
        updateRating();
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        review.setRecipe(null);
        updateRating();
    }

    private void updateRating() {
        if (reviews.isEmpty()) {
            this.rating = 0.0;
            this.ratingCount = 0;
        } else {
            double totalRating = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .sum();
            this.rating = totalRating / reviews.size();
            this.ratingCount = reviews.size();
        }
    }

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD, EXPERT
    }

    public enum MealType {
        BREAKFAST, LUNCH, DINNER, SNACK, DESSERT, APPETIZER, SOUP, SALAD, MAIN_COURSE, SIDE_DISH
    }

    public enum DietaryRestriction {
        VEGETARIAN, VEGAN, GLUTEN_FREE, DAIRY_FREE, NUT_FREE, LOW_CARB, KETO, PALEO, HALAL, KOSHER
    }
}
