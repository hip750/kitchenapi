package com.example.kitchenapi.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecipeDto 単体テスト")
class RecipeDtoTest {

    @Test
    @DisplayName("RecipeDto - コンストラクタが動作する")
    void recipeDto_ConstructorWorks() {
        // When
        RecipeDto recipeDto = new RecipeDto();

        // Then
        assertThat(recipeDto).isNotNull();
    }

    @Test
    @DisplayName("IngredientItem - 正しくインスタンス化できる")
    void ingredientItem_CanBeInstantiated() {
        // Given
        String name = "たまねぎ";
        String quantity = "2個";

        // When
        RecipeDto.IngredientItem item = new RecipeDto.IngredientItem(name, quantity);

        // Then
        assertThat(item).isNotNull();
        assertThat(item.name()).isEqualTo(name);
        assertThat(item.quantity()).isEqualTo(quantity);
    }

    @Test
    @DisplayName("IngredientItem - equals()とhashCode()が正しく動作する")
    void ingredientItem_EqualsAndHashCode() {
        // Given
        RecipeDto.IngredientItem item1 = new RecipeDto.IngredientItem("たまねぎ", "2個");
        RecipeDto.IngredientItem item2 = new RecipeDto.IngredientItem("たまねぎ", "2個");
        RecipeDto.IngredientItem item3 = new RecipeDto.IngredientItem("にんじん", "3本");

        // Then
        assertThat(item1).isEqualTo(item2);
        assertThat(item1).isNotEqualTo(item3);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    @DisplayName("IngredientItem - toString()が動作する")
    void ingredientItem_ToString() {
        // Given
        RecipeDto.IngredientItem item = new RecipeDto.IngredientItem("たまねぎ", "2個");

        // When
        String result = item.toString();

        // Then
        assertThat(result).contains("たまねぎ");
        assertThat(result).contains("2個");
    }

    @Test
    @DisplayName("CreateRequest - 正しくインスタンス化できる")
    void createRequest_CanBeInstantiated() {
        // Given
        String title = "カレーライス";
        String steps = "1. 材料を切る\\n2. 煮込む";
        Integer cookTimeMin = 30;
        String tags = "簡単,おいしい";
        List<RecipeDto.IngredientItem> ingredients = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個"),
                new RecipeDto.IngredientItem("にんじん", "1本")
        );

        // When
        RecipeDto.CreateRequest request = new RecipeDto.CreateRequest(title, steps, cookTimeMin, tags, ingredients);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.title()).isEqualTo(title);
        assertThat(request.steps()).isEqualTo(steps);
        assertThat(request.cookTimeMin()).isEqualTo(cookTimeMin);
        assertThat(request.tags()).isEqualTo(tags);
        assertThat(request.ingredients()).hasSize(2);
        assertThat(request.ingredients().get(0).name()).isEqualTo("たまねぎ");
    }

    @Test
    @DisplayName("CreateRequest - equals()とhashCode()が正しく動作する")
    void createRequest_EqualsAndHashCode() {
        // Given
        List<RecipeDto.IngredientItem> ingredients1 = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個")
        );
        List<RecipeDto.IngredientItem> ingredients2 = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個")
        );
        List<RecipeDto.IngredientItem> ingredients3 = Arrays.asList(
                new RecipeDto.IngredientItem("にんじん", "1本")
        );

        RecipeDto.CreateRequest request1 = new RecipeDto.CreateRequest("Recipe1", "Steps1", 30, "tag1", ingredients1);
        RecipeDto.CreateRequest request2 = new RecipeDto.CreateRequest("Recipe1", "Steps1", 30, "tag1", ingredients2);
        RecipeDto.CreateRequest request3 = new RecipeDto.CreateRequest("Recipe2", "Steps2", 40, "tag2", ingredients3);

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("CreateRequest - toString()が動作する")
    void createRequest_ToString() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個")
        );
        RecipeDto.CreateRequest request = new RecipeDto.CreateRequest("カレーライス", "煮込む", 30, "簡単", ingredients);

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("カレーライス");
        assertThat(result).contains("煮込む");
    }

    @Test
    @DisplayName("UpdateRequest - 正しくインスタンス化できる")
    void updateRequest_CanBeInstantiated() {
        // Given
        String title = "Updated Title";
        String steps = "Updated Steps";
        Integer cookTimeMin = 45;
        String tags = "updated,tags";

        // When
        RecipeDto.UpdateRequest request = new RecipeDto.UpdateRequest(title, steps, cookTimeMin, tags);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.title()).isEqualTo(title);
        assertThat(request.steps()).isEqualTo(steps);
        assertThat(request.cookTimeMin()).isEqualTo(cookTimeMin);
        assertThat(request.tags()).isEqualTo(tags);
    }

    @Test
    @DisplayName("UpdateRequest - null値を許容する")
    void updateRequest_AllowsNullValues() {
        // When
        RecipeDto.UpdateRequest request = new RecipeDto.UpdateRequest(null, null, null, null);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.title()).isNull();
        assertThat(request.steps()).isNull();
        assertThat(request.cookTimeMin()).isNull();
        assertThat(request.tags()).isNull();
    }

    @Test
    @DisplayName("UpdateRequest - equals()とhashCode()が正しく動作する")
    void updateRequest_EqualsAndHashCode() {
        // Given
        RecipeDto.UpdateRequest request1 = new RecipeDto.UpdateRequest("Title1", "Steps1", 30, "tag1");
        RecipeDto.UpdateRequest request2 = new RecipeDto.UpdateRequest("Title1", "Steps1", 30, "tag1");
        RecipeDto.UpdateRequest request3 = new RecipeDto.UpdateRequest("Title2", "Steps2", 40, "tag2");

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("UpdateRequest - toString()が動作する")
    void updateRequest_ToString() {
        // Given
        RecipeDto.UpdateRequest request = new RecipeDto.UpdateRequest("Updated Title", "Updated Steps", 45, "updated");

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("Updated Title");
        assertThat(result).contains("Updated Steps");
    }

    @Test
    @DisplayName("RecipeView - 正しくインスタンス化できる")
    void recipeView_CanBeInstantiated() {
        // Given
        Long id = 1L;
        String title = "カレーライス";
        String steps = "煮込む";
        Integer cookTimeMin = 30;
        String tags = "簡単";
        List<RecipeDto.IngredientItem> ingredients = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個"),
                new RecipeDto.IngredientItem("にんじん", "1本")
        );

        // When
        RecipeDto.RecipeView view = new RecipeDto.RecipeView(id, title, steps, cookTimeMin, tags, ingredients);

        // Then
        assertThat(view).isNotNull();
        assertThat(view.id()).isEqualTo(id);
        assertThat(view.title()).isEqualTo(title);
        assertThat(view.steps()).isEqualTo(steps);
        assertThat(view.cookTimeMin()).isEqualTo(cookTimeMin);
        assertThat(view.tags()).isEqualTo(tags);
        assertThat(view.ingredients()).hasSize(2);
    }

    @Test
    @DisplayName("RecipeView - equals()とhashCode()が正しく動作する")
    void recipeView_EqualsAndHashCode() {
        // Given
        List<RecipeDto.IngredientItem> ingredients1 = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個")
        );
        List<RecipeDto.IngredientItem> ingredients2 = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個")
        );
        List<RecipeDto.IngredientItem> ingredients3 = Arrays.asList(
                new RecipeDto.IngredientItem("にんじん", "1本")
        );

        RecipeDto.RecipeView view1 = new RecipeDto.RecipeView(1L, "Recipe1", "Steps1", 30, "tag1", ingredients1);
        RecipeDto.RecipeView view2 = new RecipeDto.RecipeView(1L, "Recipe1", "Steps1", 30, "tag1", ingredients2);
        RecipeDto.RecipeView view3 = new RecipeDto.RecipeView(2L, "Recipe2", "Steps2", 40, "tag2", ingredients3);

        // Then
        assertThat(view1).isEqualTo(view2);
        assertThat(view1).isNotEqualTo(view3);
        assertThat(view1.hashCode()).isEqualTo(view2.hashCode());
    }

    @Test
    @DisplayName("RecipeView - toString()が動作する")
    void recipeView_ToString() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "2個")
        );
        RecipeDto.RecipeView view = new RecipeDto.RecipeView(1L, "カレーライス", "煮込む", 30, "簡単", ingredients);

        // When
        String result = view.toString();

        // Then
        assertThat(result).contains("カレーライス");
        assertThat(result).contains("煮込む");
    }
}
