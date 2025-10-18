package com.example.kitchenapi.controller;

import com.example.kitchenapi.dto.RecipeDto;
import com.example.kitchenapi.entity.RecipeEntity;
import com.example.kitchenapi.entity.RecipeIngredientEntity;
import com.example.kitchenapi.security.AuthUser;
import com.example.kitchenapi.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * レシピ関連のエンドポイントを管理するコントローラー。
 * レシピのCRUD操作と検索機能を処理します。
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * POST /recipes
     * 新しいレシピを作成します。
     *
     * @param req レシピ情報を含む作成リクエスト
     * @param authentication Spring Securityの認証オブジェクト
     * @return 201 作成されたレシピ情報を含むRecipeView
     */
    @PostMapping
    public ResponseEntity<RecipeDto.RecipeView> createRecipe(
            @Valid @RequestBody RecipeDto.CreateRequest req,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // レシピを作成
        RecipeEntity recipe = recipeService.create(
                authUser.getUserId(),
                req.title(),
                req.steps(),
                req.cookTimeMin(),
                req.tags(),
                req.ingredients()
        );

        // エンティティをDTOに変換
        RecipeDto.RecipeView recipeView = convertToRecipeView(recipe);

        return ResponseEntity.status(HttpStatus.CREATED).body(recipeView);
    }

    /**
     * GET /recipes/{id}
     * IDでレシピを取得します。
     *
     * @param id レシピID
     * @return 200 レシピ情報を含むRecipeView
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto.RecipeView> getRecipe(@PathVariable Long id) {
        RecipeEntity recipe = recipeService.findById(id);

        // エンティティをDTOに変換
        RecipeDto.RecipeView recipeView = convertToRecipeView(recipe);

        return ResponseEntity.ok(recipeView);
    }

    /**
     * GET /recipes
     * フィルターとページネーションを使用してレシピを検索します。
     *
     * @param q タイトル検索クエリ（任意）
     * @param maxTime 最大調理時間（分）（任意）
     * @param ingredient 材料名（任意）
     * @param page ページ番号（デフォルト: 0）
     * @param size ページサイズ（デフォルト: 20）
     * @param sort ソートパラメータ（"フィールド名,方向"の形式、デフォルト: "createdAt,desc"）
     * @param authentication Spring Securityの認証オブジェクト
     * @return 200 RecipeViewのページ
     */
    @GetMapping
    public ResponseEntity<Page<RecipeDto.RecipeView>> searchRecipes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer maxTime,
            @RequestParam(required = false) String ingredient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // ソートパラメータをパース
        Pageable pageable = createPageable(page, size, sort);

        // レシピを検索
        Page<RecipeEntity> recipePage = recipeService.search(
                authUser.getUserId(),
                q,
                maxTime,
                ingredient,
                pageable
        );

        // Page<Entity>をPage<DTO>に変換
        Page<RecipeDto.RecipeView> recipeViewPage = recipePage.map(this::convertToRecipeView);

        return ResponseEntity.ok(recipeViewPage);
    }

    /**
     * PATCH /recipes/{id}
     * 既存のレシピを更新します。
     *
     * @param id レシピID
     * @param req 更新するフィールドを含む更新リクエスト
     * @param authentication Spring Securityの認証オブジェクト
     * @return 200 更新されたレシピ情報を含むRecipeView
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RecipeDto.RecipeView> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeDto.UpdateRequest req,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // レシピを更新
        RecipeEntity recipe = recipeService.update(id, authUser.getUserId(), req);

        // エンティティをDTOに変換
        RecipeDto.RecipeView recipeView = convertToRecipeView(recipe);

        return ResponseEntity.ok(recipeView);
    }

    /**
     * DELETE /recipes/{id}
     * レシピを削除します。
     *
     * @param id レシピID
     * @param authentication Spring Securityの認証オブジェクト
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // レシピを削除
        recipeService.delete(id, authUser.getUserId());

        return ResponseEntity.noContent().build();
    }

    /**
     * RecipeEntityをRecipeView DTOに変換します。
     *
     * @param recipe レシピエンティティ
     * @return レシピビューDTO
     */
    private RecipeDto.RecipeView convertToRecipeView(RecipeEntity recipe) {
        List<RecipeDto.IngredientItem> ingredients = recipe.getIngredients().stream()
                .map(ri -> new RecipeDto.IngredientItem(
                        ri.getIngredient().getName(),
                        ri.getQuantity()
                ))
                .collect(Collectors.toList());

        return new RecipeDto.RecipeView(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSteps(),
                recipe.getCookTimeMin(),
                recipe.getTags(),
                ingredients
        );
    }

    /**
     * page、size、sortパラメータからPageableを作成します。
     *
     * @param page ページ番号
     * @param size ページサイズ
     * @param sort ソートパラメータ（"フィールド名,方向"の形式）
     * @return Pageableオブジェクト
     */
    private Pageable createPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }

        String[] sortParams = sort.split(",");
        if (sortParams.length == 2) {
            String field = sortParams[0].trim();
            String direction = sortParams[1].trim();
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            return PageRequest.of(page, size, Sort.by(sortDirection, field));
        }

        return PageRequest.of(page, size);
    }
}
