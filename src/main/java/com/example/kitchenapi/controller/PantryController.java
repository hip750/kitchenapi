package com.example.kitchenapi.controller;

import com.example.kitchenapi.dto.PantryDto;
import com.example.kitchenapi.entity.PantryItemEntity;
import com.example.kitchenapi.security.AuthUser;
import com.example.kitchenapi.service.PantryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * パントリー関連のエンドポイントを管理するコントローラー。
 * パントリーアイテムのCRUD操作と検索機能を処理します。
 */
@RestController
@RequestMapping("/api/pantry")
public class PantryController {

    private final PantryService pantryService;

    public PantryController(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    /**
     * POST /pantry
     * 新しいパントリーアイテムを作成します。
     *
     * @param req パントリーアイテム情報を含む作成リクエスト
     * @param authentication Spring Securityの認証オブジェクト
     * @return 201 作成されたパントリーアイテム情報を含むPantryView
     */
    @PostMapping
    public ResponseEntity<PantryDto.PantryView> createPantryItem(
            @Valid @RequestBody PantryDto.CreateRequest req,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // パントリーアイテムを作成
        PantryItemEntity pantryItem = pantryService.add(
                authUser.getUserId(),
                req.ingredientName(),
                req.amount(),
                req.expiresOn()
        );

        // エンティティをDTOに変換
        PantryDto.PantryView pantryView = convertToPantryView(pantryItem);

        return ResponseEntity.status(HttpStatus.CREATED).body(pantryView);
    }

    /**
     * GET /pantry
     * フィルターとページネーションを使用してパントリーアイテムを検索します。
     *
     * @param ingredient 材料名（任意）
     * @param expFrom 賞味期限の開始日フィルター（任意）
     * @param expTo 賞味期限の終了日フィルター（任意）
     * @param page ページ番号（デフォルト: 0）
     * @param size ページサイズ（デフォルト: 20）
     * @param sort ソートパラメータ（"フィールド名,方向"の形式、デフォルト: "id,desc"）
     * @param authentication Spring Securityの認証オブジェクト
     * @return 200 PantryViewのページ
     */
    @GetMapping
    public ResponseEntity<Page<PantryDto.PantryView>> searchPantryItems(
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // ソートパラメータをパース
        Pageable pageable = createPageable(page, size, sort);

        // パントリーアイテムを検索
        Page<PantryItemEntity> pantryPage = pantryService.findByUserId(
                authUser.getUserId(),
                ingredient,
                expFrom,
                expTo,
                pageable
        );

        // Page<Entity>をPage<DTO>に変換
        Page<PantryDto.PantryView> pantryViewPage = pantryPage.map(this::convertToPantryView);

        return ResponseEntity.ok(pantryViewPage);
    }

    /**
     * PATCH /pantry/{id}
     * 既存のパントリーアイテムを更新します。
     *
     * @param id パントリーアイテムID
     * @param req 更新するフィールドを含む更新リクエスト
     * @param authentication Spring Securityの認証オブジェクト
     * @return 200 更新されたパントリーアイテム情報を含むPantryView
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PantryDto.PantryView> updatePantryItem(
            @PathVariable Long id,
            @Valid @RequestBody PantryDto.UpdateRequest req,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // パントリーアイテムを更新
        PantryItemEntity pantryItem = pantryService.update(
                id,
                authUser.getUserId(),
                req.amount(),
                req.expiresOn()
        );

        // エンティティをDTOに変換
        PantryDto.PantryView pantryView = convertToPantryView(pantryItem);

        return ResponseEntity.ok(pantryView);
    }

    /**
     * DELETE /pantry/{id}
     * パントリーアイテムを削除します。
     *
     * @param id パントリーアイテムID
     * @param authentication Spring Securityの認証オブジェクト
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePantryItem(
            @PathVariable Long id,
            Authentication authentication) {

        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // パントリーアイテムを削除
        pantryService.delete(id, authUser.getUserId());

        return ResponseEntity.noContent().build();
    }

    /**
     * PantryItemEntityをPantryView DTOに変換します。
     *
     * @param pantryItem パントリーアイテムエンティティ
     * @return パントリービューDTO
     */
    private PantryDto.PantryView convertToPantryView(PantryItemEntity pantryItem) {
        return new PantryDto.PantryView(
                pantryItem.getId(),
                pantryItem.getIngredient().getName(),
                pantryItem.getAmount(),
                pantryItem.getExpiresOn()
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
