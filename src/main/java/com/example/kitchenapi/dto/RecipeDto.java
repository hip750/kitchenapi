package com.example.kitchenapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * レシピ関連のDTO。
 * すべてのネストされたレコードはRecipeControllerのリクエスト/レスポンスで使用されます。
 */
public class RecipeDto {

    /**
     * 材料名と分量を持つ材料アイテム。
     * レシピのリクエストとレスポンスでネストされたコンポーネントとして使用されます。
     */
    public record IngredientItem(
            @NotBlank(message = "Ingredient name is required")
            String name,

            @NotBlank(message = "Ingredient quantity is required")
            String quantity
    ) {}

    /**
     * 新しいレシピを作成するためのリクエストDTO。
     * POST /recipes で使用されます。
     */
    public record CreateRequest(
            @NotBlank(message = "Title is required")
            String title,

            @NotBlank(message = "Steps are required")
            String steps,

            @NotNull(message = "Cook time is required")
            @Positive(message = "Cook time must be positive")
            Integer cookTimeMin,

            String tags,

            @NotNull(message = "Ingredients are required")
            @Valid
            List<IngredientItem> ingredients
    ) {}

    /**
     * 既存のレシピを更新するためのリクエストDTO。
     * すべてのフィールドは任意です（部分更新）。
     * PATCH /recipes/{id} で使用されます。
     */
    public record UpdateRequest(
            String title,
            String steps,
            @Positive(message = "Cook time must be positive")
            Integer cookTimeMin,
            String tags
    ) {}

    /**
     * レシピビューのためのレスポンスDTO。
     * POST /recipes、GET /recipes/{id}、GET /recipes、PATCH /recipes/{id} のレスポンスで使用されます。
     */
    public record RecipeView(
            Long id,
            String title,
            String steps,
            Integer cookTimeMin,
            String tags,
            List<IngredientItem> ingredients
    ) {}
}
