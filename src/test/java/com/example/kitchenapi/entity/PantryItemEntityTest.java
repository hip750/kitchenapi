package com.example.kitchenapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PantryItemEntity 単体テスト")
class PantryItemEntityTest {

    @Test
    @DisplayName("デフォルトコンストラクタでインスタンス化できる")
    void defaultConstructor() {
        // When
        PantryItemEntity pantryItem = new PantryItemEntity();

        // Then
        assertThat(pantryItem).isNotNull();
        assertThat(pantryItem.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("パラメータ付きコンストラクタで正しくフィールドが設定される")
    void parameterizedConstructor() {
        // Given
        Long userId = 1L;
        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        ingredient.setId(1L);
        String amount = "2個";
        LocalDate expiresOn = LocalDate.of(2025, 12, 31);

        // When
        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, amount, expiresOn);

        // Then
        assertThat(pantryItem.getUserId()).isEqualTo(userId);
        assertThat(pantryItem.getIngredient()).isEqualTo(ingredient);
        assertThat(pantryItem.getAmount()).isEqualTo(amount);
        assertThat(pantryItem.getExpiresOn()).isEqualTo(expiresOn);
        assertThat(pantryItem.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("セッターで値を設定できる")
    void setters() {
        // Given
        PantryItemEntity pantryItem = new PantryItemEntity();
        IngredientEntity ingredient = new IngredientEntity("にんじん");
        ingredient.setId(2L);
        Instant now = Instant.now();

        // When
        pantryItem.setId(1L);
        pantryItem.setUserId(2L);
        pantryItem.setIngredient(ingredient);
        pantryItem.setAmount("3本");
        pantryItem.setExpiresOn(LocalDate.of(2026, 1, 1));
        pantryItem.setCreatedAt(now);

        // Then
        assertThat(pantryItem.getId()).isEqualTo(1L);
        assertThat(pantryItem.getUserId()).isEqualTo(2L);
        assertThat(pantryItem.getIngredient()).isEqualTo(ingredient);
        assertThat(pantryItem.getAmount()).isEqualTo("3本");
        assertThat(pantryItem.getExpiresOn()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(pantryItem.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("equals()とhashCode()が正しく動作する")
    void equalsAndHashCode() {
        // Given
        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        ingredient1.setId(1L);
        IngredientEntity ingredient2 = new IngredientEntity("にんじん");
        ingredient2.setId(2L);

        PantryItemEntity item1 = new PantryItemEntity(1L, ingredient1, "2個", LocalDate.now());
        item1.setId(1L);

        PantryItemEntity item2 = new PantryItemEntity(2L, ingredient2, "3本", LocalDate.now());
        item2.setId(1L);

        PantryItemEntity item3 = new PantryItemEntity(1L, ingredient1, "2個", LocalDate.now());
        item3.setId(2L);

        // Then
        assertThat(item1).isEqualTo(item2); // 同じID
        assertThat(item1).isNotEqualTo(item3); // 異なるID
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    @DisplayName("equals() - 同じインスタンス")
    void equals_SameInstance() {
        // Given
        PantryItemEntity pantryItem = new PantryItemEntity();
        pantryItem.setId(1L);

        // Then
        assertThat(pantryItem).isEqualTo(pantryItem);
    }

    @Test
    @DisplayName("equals() - nullと比較")
    void equals_Null() {
        // Given
        PantryItemEntity pantryItem = new PantryItemEntity();
        pantryItem.setId(1L);

        // Then
        assertThat(pantryItem).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals() - 異なるクラスと比較")
    void equals_DifferentClass() {
        // Given
        PantryItemEntity pantryItem = new PantryItemEntity();
        pantryItem.setId(1L);

        // Then
        assertThat(pantryItem).isNotEqualTo("string");
    }

    @Test
    @DisplayName("賞味期限がnullでも動作する")
    void expiresOnCanBeNull() {
        // Given
        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        ingredient.setId(1L);

        // When
        PantryItemEntity pantryItem = new PantryItemEntity(1L, ingredient, "2個", null);

        // Then
        assertThat(pantryItem.getExpiresOn()).isNull();
    }
}
