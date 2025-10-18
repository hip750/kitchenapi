package com.example.kitchenapi.repository;

import com.example.kitchenapi.entity.RecipeIngredientEntity;
import com.example.kitchenapi.entity.RecipeIngredientKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredientEntity, RecipeIngredientKey> {

    /**
     * Find all recipe ingredients for a specific recipe.
     *
     * @param recipeId the ID of the recipe
     * @return a list of recipe ingredients
     */
    List<RecipeIngredientEntity> findByRecipeId(Long recipeId);

    /**
     * Find all recipe ingredients that use a specific ingredient.
     *
     * @param ingredientId the ID of the ingredient
     * @return a list of recipe ingredients
     */
    List<RecipeIngredientEntity> findByIngredientId(Long ingredientId);

    /**
     * Delete all recipe ingredients for a specific recipe.
     *
     * @param recipeId the ID of the recipe
     */
    void deleteByRecipeId(Long recipeId);

    /**
     * Count the number of recipes that use a specific ingredient.
     *
     * @param ingredientId the ID of the ingredient
     * @return the count of recipes using the ingredient
     */
    @Query("SELECT COUNT(DISTINCT ri.recipe.id) FROM RecipeIngredientEntity ri WHERE ri.ingredient.id = :ingredientId")
    long countRecipesByIngredientId(@Param("ingredientId") Long ingredientId);
}
