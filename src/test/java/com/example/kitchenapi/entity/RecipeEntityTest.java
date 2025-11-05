package com.example.kitchenapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecipeEntity 単体テスト")
class RecipeEntityTest {

    @Test
    @DisplayName("デフォルトコンストラクタでインスタンス化できる")
    void defaultConstructor() {
        // When
        RecipeEntity recipe = new RecipeEntity();

        // Then
        assertThat(recipe).isNotNull();
        assertThat(recipe.getCreatedAt()).isNotNull();
        assertThat(recipe.getIngredients()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("パラメータ付きコンストラクタで正しくフィールドが設定される")
    void parameterizedConstructor() {
        // Given
        String title = "カレーライス";
        String steps = "1. 材料を切る\n2. 煮込む";
        Integer cookTimeMin = 30;
        String tags = "簡単,おいしい";
        Long ownerId = 1L;

        // When
        RecipeEntity recipe = new RecipeEntity(title, steps, cookTimeMin, tags, ownerId);

        // Then
        assertThat(recipe.getTitle()).isEqualTo(title);
        assertThat(recipe.getSteps()).isEqualTo(steps);
        assertThat(recipe.getCookTimeMin()).isEqualTo(cookTimeMin);
        assertThat(recipe.getTags()).isEqualTo(tags);
        assertThat(recipe.getOwnerId()).isEqualTo(ownerId);
        assertThat(recipe.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("セッターで値を設定できる")
    void setters() {
        // Given
        RecipeEntity recipe = new RecipeEntity();
        Instant now = Instant.now();
        List<RecipeIngredientEntity> ingredients = new ArrayList<>();

        // When
        recipe.setId(1L);
        recipe.setTitle("パスタ");
        recipe.setSteps("茹でる");
        recipe.setCookTimeMin(15);
        recipe.setTags("イタリアン");
        recipe.setOwnerId(2L);
        recipe.setCreatedAt(now);
        recipe.setIngredients(ingredients);

        // Then
        assertThat(recipe.getId()).isEqualTo(1L);
        assertThat(recipe.getTitle()).isEqualTo("パスタ");
        assertThat(recipe.getSteps()).isEqualTo("茹でる");
        assertThat(recipe.getCookTimeMin()).isEqualTo(15);
        assertThat(recipe.getTags()).isEqualTo("イタリアン");
        assertThat(recipe.getOwnerId()).isEqualTo(2L);
        assertThat(recipe.getCreatedAt()).isEqualTo(now);
        assertThat(recipe.getIngredients()).isEqualTo(ingredients);
    }

    @Test
    @DisplayName("equals()とhashCode()が正しく動作する")
    void equalsAndHashCode() {
        // Given
        RecipeEntity recipe1 = new RecipeEntity("Recipe1", "Steps1", 30, "tag1", 1L);
        recipe1.setId(1L);

        RecipeEntity recipe2 = new RecipeEntity("Recipe2", "Steps2", 40, "tag2", 2L);
        recipe2.setId(1L);

        RecipeEntity recipe3 = new RecipeEntity("Recipe1", "Steps1", 30, "tag1", 1L);
        recipe3.setId(2L);

        // Then
        assertThat(recipe1).isEqualTo(recipe2); // 同じID
        assertThat(recipe1).isNotEqualTo(recipe3); // 異なるID
        assertThat(recipe1.hashCode()).isEqualTo(recipe2.hashCode());
    }

    @Test
    @DisplayName("equals() - 同じインスタンス")
    void equals_SameInstance() {
        // Given
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(1L);

        // Then
        assertThat(recipe).isEqualTo(recipe);
    }

    @Test
    @DisplayName("equals() - nullと比較")
    void equals_Null() {
        // Given
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(1L);

        // Then
        assertThat(recipe).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals() - 異なるクラスと比較")
    void equals_DifferentClass() {
        // Given
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(1L);

        // Then
        assertThat(recipe).isNotEqualTo("string");
    }

    @Test
    @DisplayName("材料リストを追加・取得できる")
    void ingredientsList() {
        // Given
        RecipeEntity recipe = new RecipeEntity("Recipe", "Steps", 30, "tags", 1L);
        recipe.setId(1L);

        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        ingredient1.setId(1L);

        RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();
        recipeIngredient.setRecipe(recipe);
        recipeIngredient.setIngredient(ingredient1);
        recipeIngredient.setQuantity("1個");

        // When
        recipe.getIngredients().add(recipeIngredient);

        // Then
        assertThat(recipe.getIngredients()).hasSize(1);
        assertThat(recipe.getIngredients().get(0).getQuantity()).isEqualTo("1個");
    }
}
