package com.lric3.noshpit.api.repository;

import com.lric3.noshpit.api.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRecipeId(Long recipeId);

    List<Review> findByUserId(Long userId);

    List<Review> findByRecipeIdAndUserId(Long recipeId, Long userId);
}
