package com.lric3.noshpit.api.util;

import com.lric3.noshpit.api.entity.*;
import com.lric3.noshpit.api.repository.RecipeRepository;
import com.lric3.noshpit.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final RecipeRepository recipeRepository;

    private final UserBuilder userBuilder;

    private final RecipeBuilder recipeBuilder;
    
    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {
            initializeData(); // Only initializes if no users exist
        }
    }
    
    private void initializeData() {

        User admin = userBuilder.buildAdminUser();
        userRepository.save(admin);

        User user = userBuilder.buildUser();
        userRepository.save(user);

        createSampleRecipes(user);
    }
    
    private void createSampleRecipes(User user) {

        Recipe carbonara = recipeBuilder.buildCarbonaraRecipe(user);
        recipeRepository.save(carbonara);

        Recipe cookies = recipeBuilder.buildChocolateChipCookiesRecipe(user);
        recipeRepository.save(cookies);
    }
}
