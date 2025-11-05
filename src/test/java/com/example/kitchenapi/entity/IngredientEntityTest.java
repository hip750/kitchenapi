package com.example.kitchenapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IngredientEntity 単体テスト")
class IngredientEntityTest {

    @Test
    @DisplayName("デフォルトコンストラクタでインスタンス化できる")
    void defaultConstructor() {
        // When
        IngredientEntity ingredient = new IngredientEntity();

        // Then
        assertThat(ingredient).isNotNull();
    }

    @Test
    @DisplayName("パラメータ付きコンストラクタで正しくフィールドが設定される")
    void parameterizedConstructor() {
        // Given
        String name = "たまねぎ";

        // When
        IngredientEntity ingredient = new IngredientEntity(name);

        // Then
        assertThat(ingredient.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("セッターで値を設定できる")
    void setters() {
        // Given
        IngredientEntity ingredient = new IngredientEntity();

        // When
        ingredient.setId(1L);
        ingredient.setName("にんじん");

        // Then
        assertThat(ingredient.getId()).isEqualTo(1L);
        assertThat(ingredient.getName()).isEqualTo("にんじん");
    }

    @Test
    @DisplayName("equals()とhashCode()が正しく動作する")
    void equalsAndHashCode() {
        // Given
        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        ingredient1.setId(1L);

        IngredientEntity ingredient2 = new IngredientEntity("にんじん");
        ingredient2.setId(1L);

        IngredientEntity ingredient3 = new IngredientEntity("たまねぎ");
        ingredient3.setId(2L);

        // Then
        assertThat(ingredient1).isEqualTo(ingredient2); // 同じID
        assertThat(ingredient1).isNotEqualTo(ingredient3); // 異なるID
        assertThat(ingredient1.hashCode()).isEqualTo(ingredient2.hashCode());
    }

    @Test
    @DisplayName("equals() - 同じインスタンス")
    void equals_SameInstance() {
        // Given
        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        ingredient.setId(1L);

        // Then
        assertThat(ingredient).isEqualTo(ingredient);
    }

    @Test
    @DisplayName("equals() - nullと比較")
    void equals_Null() {
        // Given
        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        ingredient.setId(1L);

        // Then
        assertThat(ingredient).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals() - 異なるクラスと比較")
    void equals_DifferentClass() {
        // Given
        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        ingredient.setId(1L);

        // Then
        assertThat(ingredient).isNotEqualTo("string");
    }
}
