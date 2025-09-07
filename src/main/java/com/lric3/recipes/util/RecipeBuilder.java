package com.lric3.recipes.util;

import com.lric3.recipes.entity.Ingredient;
import com.lric3.recipes.entity.Instruction;
import com.lric3.recipes.entity.Recipe;
import com.lric3.recipes.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RecipeBuilder {

    private Recipe recipe;

    public Recipe buildCarbonaraRecipe(User user) {

        recipe = new Recipe();
        recipe.setTitle("Spaghetti Carbonara");
        recipe.setDescription("A classic Italian pasta dish with eggs, cheese, pancetta, and black pepper.");
        recipe.setPrepTime(15);
        recipe.setCookTime(20);
        recipe.setServings(4);
        recipe.setDifficultyLevel(Recipe.DifficultyLevel.MEDIUM);
        recipe.setCuisineType("Italian");
        recipe.setMealType(Recipe.MealType.MAIN_COURSE);
        recipe.setDietaryRestrictions(Arrays.asList(Recipe.DietaryRestriction.VEGETARIAN));
        recipe.setUser(user);
        recipe.setPublic(true);

        // Add ingredients
        Ingredient pasta = new Ingredient("Spaghetti", 400.0, "g", recipe);
        Ingredient eggs = new Ingredient("Large eggs", 4.0, "pieces", recipe);
        Ingredient cheese = new Ingredient("Pecorino Romano cheese", 100.0, "g", recipe);
        Ingredient pancetta = new Ingredient("Pancetta", 150.0, "g", recipe);
        Ingredient pepper = new Ingredient("Black pepper", 2.0, "tsp", recipe);

        recipe.addIngredient(pasta);
        recipe.addIngredient(eggs);
        recipe.addIngredient(cheese);
        recipe.addIngredient(pancetta);
        recipe.addIngredient(pepper);

        // Add instructions
        Instruction step1 = new Instruction(1, "Bring a large pot of salted water to boil and cook spaghetti according to package directions.", recipe);
        Instruction step2 = new Instruction(2, "Meanwhile, cook pancetta in a large skillet over medium heat until crispy, about 8 minutes.", recipe);
        Instruction step3 = new Instruction(3, "In a bowl, whisk together eggs, cheese, and pepper.", recipe);
        Instruction step4 = new Instruction(4, "Drain pasta, reserving 1 cup of pasta water.", recipe);
        Instruction step5 = new Instruction(5, "Add hot pasta to skillet with pancetta, remove from heat, and quickly stir in egg mixture.", recipe);
        Instruction step6 = new Instruction(6, "Add pasta water as needed to create a creamy sauce. Serve immediately.", recipe);

        recipe.addInstruction(step1);
        recipe.addInstruction(step2);
        recipe.addInstruction(step3);
        recipe.addInstruction(step4);
        recipe.addInstruction(step5);
        recipe.addInstruction(step6);

        return recipe;
    }

    public Recipe buildChocolateChipCookiesRecipe(User user) {

        recipe = new Recipe();
        recipe.setTitle("Classic Chocolate Chip Cookies");
        recipe.setDescription("Soft and chewy chocolate chip cookies with crispy edges.");
        recipe.setPrepTime(20);
        recipe.setCookTime(12);
        recipe.setServings(24);
        recipe.setDifficultyLevel(Recipe.DifficultyLevel.EASY);
        recipe.setCuisineType("American");
        recipe.setMealType(Recipe.MealType.DESSERT);
        recipe.setDietaryRestrictions(Arrays.asList(Recipe.DietaryRestriction.VEGETARIAN));
        recipe.setUser(user);
        recipe.setPublic(true);

        // Add ingredients
        Ingredient flour = new Ingredient("All-purpose flour", 2.25, "cups", recipe);
        Ingredient butter = new Ingredient("Unsalted butter", 1.0, "cup", recipe);
        Ingredient sugar = new Ingredient("Granulated sugar", 0.75, "cup", recipe);
        Ingredient brownSugar = new Ingredient("Brown sugar", 0.75, "cup", recipe);
        Ingredient vanilla = new Ingredient("Vanilla extract", 1.0, "tsp", recipe);
        Ingredient chocolate = new Ingredient("Chocolate chips", 2.0, "cups", recipe);

        recipe.addIngredient(flour);
        recipe.addIngredient(butter);
        recipe.addIngredient(sugar);
        recipe.addIngredient(brownSugar);
        recipe.addIngredient(vanilla);
        recipe.addIngredient(chocolate);

        // Add instructions
        Instruction cookieStep1 = new Instruction(1, "Preheat oven to 375°F (190°C). Line baking sheets with parchment paper.", recipe);
        Instruction cookieStep2 = new Instruction(2, "Cream together butter, granulated sugar, and brown sugar until light and fluffy.", recipe);
        Instruction cookieStep3 = new Instruction(3, "Beat in eggs and vanilla extract.", recipe);
        Instruction cookieStep4 = new Instruction(4, "Gradually mix in flour and salt until just combined.", recipe);
        Instruction cookieStep5 = new Instruction(5, "Stir in chocolate chips.", recipe);
        Instruction cookieStep6 = new Instruction(6, "Drop rounded tablespoons of dough onto prepared baking sheets.", recipe);
        Instruction cookieStep7 = new Instruction(7, "Bake for 10-12 minutes until golden brown. Cool on baking sheets for 5 minutes.", recipe);

        recipe.addInstruction(cookieStep1);
        recipe.addInstruction(cookieStep2);
        recipe.addInstruction(cookieStep3);
        recipe.addInstruction(cookieStep4);
        recipe.addInstruction(cookieStep5);
        recipe.addInstruction(cookieStep6);
        recipe.addInstruction(cookieStep7);

        return recipe;
    }

}
