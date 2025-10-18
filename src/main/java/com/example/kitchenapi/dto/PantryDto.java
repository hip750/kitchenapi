package com.example.kitchenapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * パントリー関連のDTO。
 * すべてのネストされたレコードはPantryControllerのリクエスト/レスポンスで使用されます。
 */
public class PantryDto {

    /**
     * 新しいパントリーアイテムを作成するためのリクエストDTO。
     * POST /pantry で使用されます。
     */
    public record CreateRequest(
            @NotBlank(message = "Ingredient name is required")
            String ingredientName,

            @NotBlank(message = "Amount is required")
            String amount,

            @NotNull(message = "Expiration date is required")
            LocalDate expiresOn
    ) {}

    /**
     * 既存のパントリーアイテムを更新するためのリクエストDTO。
     * すべてのフィールドは任意です（部分更新）。
     * PATCH /pantry/{id} で使用されます。
     */
    public record UpdateRequest(
            String amount,
            LocalDate expiresOn
    ) {}

    /**
     * パントリーアイテムビューのためのレスポンスDTO。
     * POST /pantry、GET /pantry、PATCH /pantry/{id} のレスポンスで使用されます。
     */
    public record PantryView(
            Long id,
            String ingredientName,
            String amount,
            LocalDate expiresOn
    ) {}
}
