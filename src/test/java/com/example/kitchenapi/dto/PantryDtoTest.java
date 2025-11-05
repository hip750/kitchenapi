package com.example.kitchenapi.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PantryDto 単体テスト")
class PantryDtoTest {

    @Test
    @DisplayName("PantryDto - コンストラクタが動作する")
    void pantryDto_ConstructorWorks() {
        // When
        PantryDto pantryDto = new PantryDto();

        // Then
        assertThat(pantryDto).isNotNull();
    }

    @Test
    @DisplayName("CreateRequest - 正しくインスタンス化できる")
    void createRequest_CanBeInstantiated() {
        // Given
        String ingredientName = "たまねぎ";
        String amount = "2個";
        LocalDate expiresOn = LocalDate.of(2025, 12, 31);

        // When
        PantryDto.CreateRequest request = new PantryDto.CreateRequest(ingredientName, amount, expiresOn);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.ingredientName()).isEqualTo(ingredientName);
        assertThat(request.amount()).isEqualTo(amount);
        assertThat(request.expiresOn()).isEqualTo(expiresOn);
    }

    @Test
    @DisplayName("CreateRequest - equals()とhashCode()が正しく動作する")
    void createRequest_EqualsAndHashCode() {
        // Given
        LocalDate date = LocalDate.of(2025, 12, 31);
        PantryDto.CreateRequest request1 = new PantryDto.CreateRequest("たまねぎ", "2個", date);
        PantryDto.CreateRequest request2 = new PantryDto.CreateRequest("たまねぎ", "2個", date);
        PantryDto.CreateRequest request3 = new PantryDto.CreateRequest("にんじん", "3本", LocalDate.of(2026, 1, 1));

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("CreateRequest - toString()が動作する")
    void createRequest_ToString() {
        // Given
        PantryDto.CreateRequest request = new PantryDto.CreateRequest("たまねぎ", "2個", LocalDate.of(2025, 12, 31));

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("たまねぎ");
        assertThat(result).contains("2個");
        assertThat(result).contains("2025-12-31");
    }

    @Test
    @DisplayName("UpdateRequest - 正しくインスタンス化できる")
    void updateRequest_CanBeInstantiated() {
        // Given
        String amount = "3個";
        LocalDate expiresOn = LocalDate.of(2026, 1, 1);

        // When
        PantryDto.UpdateRequest request = new PantryDto.UpdateRequest(amount, expiresOn);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.amount()).isEqualTo(amount);
        assertThat(request.expiresOn()).isEqualTo(expiresOn);
    }

    @Test
    @DisplayName("UpdateRequest - null値を許容する")
    void updateRequest_AllowsNullValues() {
        // When
        PantryDto.UpdateRequest request = new PantryDto.UpdateRequest(null, null);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.amount()).isNull();
        assertThat(request.expiresOn()).isNull();
    }

    @Test
    @DisplayName("UpdateRequest - equals()とhashCode()が正しく動作する")
    void updateRequest_EqualsAndHashCode() {
        // Given
        LocalDate date = LocalDate.of(2026, 1, 1);
        PantryDto.UpdateRequest request1 = new PantryDto.UpdateRequest("3個", date);
        PantryDto.UpdateRequest request2 = new PantryDto.UpdateRequest("3個", date);
        PantryDto.UpdateRequest request3 = new PantryDto.UpdateRequest("5本", LocalDate.of(2026, 2, 1));

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("UpdateRequest - toString()が動作する")
    void updateRequest_ToString() {
        // Given
        PantryDto.UpdateRequest request = new PantryDto.UpdateRequest("3個", LocalDate.of(2026, 1, 1));

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("3個");
        assertThat(result).contains("2026-01-01");
    }

    @Test
    @DisplayName("PantryView - 正しくインスタンス化できる")
    void pantryView_CanBeInstantiated() {
        // Given
        Long id = 1L;
        String ingredientName = "たまねぎ";
        String amount = "2個";
        LocalDate expiresOn = LocalDate.of(2025, 12, 31);

        // When
        PantryDto.PantryView view = new PantryDto.PantryView(id, ingredientName, amount, expiresOn);

        // Then
        assertThat(view).isNotNull();
        assertThat(view.id()).isEqualTo(id);
        assertThat(view.ingredientName()).isEqualTo(ingredientName);
        assertThat(view.amount()).isEqualTo(amount);
        assertThat(view.expiresOn()).isEqualTo(expiresOn);
    }

    @Test
    @DisplayName("PantryView - equals()とhashCode()が正しく動作する")
    void pantryView_EqualsAndHashCode() {
        // Given
        LocalDate date = LocalDate.of(2025, 12, 31);
        PantryDto.PantryView view1 = new PantryDto.PantryView(1L, "たまねぎ", "2個", date);
        PantryDto.PantryView view2 = new PantryDto.PantryView(1L, "たまねぎ", "2個", date);
        PantryDto.PantryView view3 = new PantryDto.PantryView(2L, "にんじん", "3本", LocalDate.of(2026, 1, 1));

        // Then
        assertThat(view1).isEqualTo(view2);
        assertThat(view1).isNotEqualTo(view3);
        assertThat(view1.hashCode()).isEqualTo(view2.hashCode());
    }

    @Test
    @DisplayName("PantryView - toString()が動作する")
    void pantryView_ToString() {
        // Given
        PantryDto.PantryView view = new PantryDto.PantryView(1L, "たまねぎ", "2個", LocalDate.of(2025, 12, 31));

        // When
        String result = view.toString();

        // Then
        assertThat(result).contains("たまねぎ");
        assertThat(result).contains("2個");
        assertThat(result).contains("2025-12-31");
    }
}
