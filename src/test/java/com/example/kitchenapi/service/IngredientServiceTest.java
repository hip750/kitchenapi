package com.example.kitchenapi.service;

import com.example.kitchenapi.entity.IngredientEntity;
import com.example.kitchenapi.repository.IngredientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IngredientService 単体テスト")
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    @DisplayName("findOrCreate - 正常系: 既存の材料を取得できる")
    void findOrCreate_ExistingIngredient() {
        // Given
        String name = "onion";
        IngredientEntity existingIngredient = new IngredientEntity(name);
        existingIngredient.setId(1L);

        when(ingredientRepository.findByName(name)).thenReturn(Optional.of(existingIngredient));

        // When
        IngredientEntity result = ingredientService.findOrCreate(name);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getId()).isEqualTo(1L);

        verify(ingredientRepository).findByName(name);
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreate - 正常系: 新しい材料を作成できる")
    void findOrCreate_NewIngredient() {
        // Given
        String name = "tomato";
        IngredientEntity newIngredient = new IngredientEntity(name);
        newIngredient.setId(2L);

        when(ingredientRepository.findByName(name)).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(IngredientEntity.class))).thenReturn(newIngredient);

        // When
        IngredientEntity result = ingredientService.findOrCreate(name);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getId()).isEqualTo(2L);

        verify(ingredientRepository).findByName(name);
        verify(ingredientRepository).save(any(IngredientEntity.class));
    }

    @Test
    @DisplayName("findOrCreate - 異常系: 材料名がnull")
    void findOrCreate_NullName() {
        // When & Then
        assertThatThrownBy(() -> ingredientService.findOrCreate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient name is required");

        verify(ingredientRepository, never()).findByName(any());
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreate - 異常系: 材料名が空白")
    void findOrCreate_BlankName() {
        // When & Then
        assertThatThrownBy(() -> ingredientService.findOrCreate("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient name is required");

        verify(ingredientRepository, never()).findByName(any());
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreate - 同じ材料名で複数回呼び出しても同じ材料が返される")
    void findOrCreate_MultipleCallsSameIngredient() {
        // Given
        String name = "potato";
        IngredientEntity ingredient = new IngredientEntity(name);
        ingredient.setId(3L);

        when(ingredientRepository.findByName(name)).thenReturn(Optional.of(ingredient));

        // When
        IngredientEntity result1 = ingredientService.findOrCreate(name);
        IngredientEntity result2 = ingredientService.findOrCreate(name);

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.getId()).isEqualTo(3L);

        verify(ingredientRepository, times(2)).findByName(name);
        verify(ingredientRepository, never()).save(any());
    }
}
