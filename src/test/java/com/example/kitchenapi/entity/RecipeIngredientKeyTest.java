package com.example.kitchenapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecipeIngredientKey 単体テスト")
class RecipeIngredientKeyTest {

    @Test
    @DisplayName("デフォルトコンストラクタでインスタンス化できる")
    void defaultConstructor() {
        // When
        RecipeIngredientKey key = new RecipeIngredientKey();

        // Then
        assertThat(key).isNotNull();
    }

    @Test
    @DisplayName("パラメータ付きコンストラクタで正しくフィールドが設定される")
    void parameterizedConstructor() {
        // Given
        Long recipeId = 1L;
        Long ingredientId = 2L;

        // When
        RecipeIngredientKey key = new RecipeIngredientKey(recipeId, ingredientId);

        // Then
        assertThat(key.getRecipeId()).isEqualTo(recipeId);
        assertThat(key.getIngredientId()).isEqualTo(ingredientId);
    }

    @Test
    @DisplayName("セッターで値を設定できる")
    void setters() {
        // Given
        RecipeIngredientKey key = new RecipeIngredientKey();

        // When
        key.setRecipeId(10L);
        key.setIngredientId(20L);

        // Then
        assertThat(key.getRecipeId()).isEqualTo(10L);
        assertThat(key.getIngredientId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("equals()とhashCode()が正しく動作する")
    void equalsAndHashCode() {
        // Given
        RecipeIngredientKey key1 = new RecipeIngredientKey(1L, 2L);
        RecipeIngredientKey key2 = new RecipeIngredientKey(1L, 2L);
        RecipeIngredientKey key3 = new RecipeIngredientKey(1L, 3L);
        RecipeIngredientKey key4 = new RecipeIngredientKey(2L, 2L);

        // Then
        assertThat(key1).isEqualTo(key2);
        assertThat(key1).isNotEqualTo(key3);
        assertThat(key1).isNotEqualTo(key4);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("equals() - 同じインスタンス")
    void equals_SameInstance() {
        // Given
        RecipeIngredientKey key = new RecipeIngredientKey(1L, 2L);

        // Then
        assertThat(key).isEqualTo(key);
    }

    @Test
    @DisplayName("equals() - nullと比較")
    void equals_Null() {
        // Given
        RecipeIngredientKey key = new RecipeIngredientKey(1L, 2L);

        // Then
        assertThat(key).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals() - 異なるクラスと比較")
    void equals_DifferentClass() {
        // Given
        RecipeIngredientKey key = new RecipeIngredientKey(1L, 2L);

        // Then
        assertThat(key).isNotEqualTo("string");
    }
}
