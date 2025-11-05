package com.example.kitchenapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecipeIngredientEntity 単体テスト")
class RecipeIngredientEntityTest {

    @Test
    @DisplayName("デフォルトコンストラクタでインスタンス化できる")
    void defaultConstructor() {
        // When
        RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();

        // Then
        assertThat(recipeIngredient).isNotNull();
        assertThat(recipeIngredient.getId()).isNotNull();
    }

    @Test
    @DisplayName("パラメータ付きコンストラクタで正しくフィールドが設定される")
    void parameterizedConstructor() {
        // Given
        RecipeEntity recipe = new RecipeEntity("カレーライス", "煮込む", 30, "簡単", 1L);
        recipe.setId(1L);

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        ingredient.setId(1L);

        String quantity = "1個";

        // When
        RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity(recipe, ingredient, quantity);

        // Then
        assertThat(recipeIngredient.getRecipe()).isEqualTo(recipe);
        assertThat(recipeIngredient.getIngredient()).isEqualTo(ingredient);
        assertThat(recipeIngredient.getQuantity()).isEqualTo(quantity);
        assertThat(recipeIngredient.getId().getRecipeId()).isEqualTo(1L);
        assertThat(recipeIngredient.getId().getIngredientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("セッターで値を設定できる")
    void setters() {
        // Given
        RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();

        RecipeEntity recipe = new RecipeEntity("パスタ", "茹でる", 15, "イタリアン", 2L);
        recipe.setId(2L);

        IngredientEntity ingredient = new IngredientEntity("にんじん");
        ingredient.setId(2L);

        RecipeIngredientKey key = new RecipeIngredientKey(2L, 2L);

        // When
        recipeIngredient.setId(key);
        recipeIngredient.setRecipe(recipe);
        recipeIngredient.setIngredient(ingredient);
        recipeIngredient.setQuantity("2本");

        // Then
        assertThat(recipeIngredient.getId()).isEqualTo(key);
        assertThat(recipeIngredient.getRecipe()).isEqualTo(recipe);
        assertThat(recipeIngredient.getIngredient()).isEqualTo(ingredient);
        assertThat(recipeIngredient.getQuantity()).isEqualTo("2本");
    }

    @Test
    @DisplayName("equals()とhashCode()が正しく動作する")
    void equalsAndHashCode() {
        // Given
        RecipeIngredientKey key1 = new RecipeIngredientKey(1L, 1L);
        RecipeIngredientKey key2 = new RecipeIngredientKey(1L, 1L);
        RecipeIngredientKey key3 = new RecipeIngredientKey(2L, 2L);

        RecipeIngredientEntity entity1 = new RecipeIngredientEntity();
        entity1.setId(key1);

        RecipeIngredientEntity entity2 = new RecipeIngredientEntity();
        entity2.setId(key2);

        RecipeIngredientEntity entity3 = new RecipeIngredientEntity();
        entity3.setId(key3);

        // Then
        assertThat(entity1).isEqualTo(entity2); // 同じID
        assertThat(entity1).isNotEqualTo(entity3); // 異なるID
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("equals() - 同じインスタンス")
    void equals_SameInstance() {
        // Given
        RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();
        recipeIngredient.setId(new RecipeIngredientKey(1L, 1L));

        // Then
        assertThat(recipeIngredient).isEqualTo(recipeIngredient);
    }

    @Test
    @DisplayName("equals() - nullと比較")
    void equals_Null() {
        // Given
        RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();
        recipeIngredient.setId(new RecipeIngredientKey(1L, 1L));

        // Then
        assertThat(recipeIngredient).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals() - 異なるクラスと比較")
    void equals_DifferentClass() {
        // Given
        RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();
        recipeIngredient.setId(new RecipeIngredientKey(1L, 1L));

        // Then
        assertThat(recipeIngredient).isNotEqualTo("string");
    }
}
