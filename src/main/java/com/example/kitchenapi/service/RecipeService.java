package com.example.kitchenapi.service;

import com.example.kitchenapi.dto.RecipeDto;
import com.example.kitchenapi.entity.IngredientEntity;
import com.example.kitchenapi.entity.RecipeEntity;
import com.example.kitchenapi.entity.RecipeIngredientEntity;
import com.example.kitchenapi.repository.RecipeIngredientRepository;
import com.example.kitchenapi.repository.RecipeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * レシピ管理のサービス層
 * レシピのCRUD操作と検索機能を処理します。
 */
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final IngredientService ingredientService;
    private final EntityManager entityManager;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository recipeIngredientRepository,
                         IngredientService ingredientService,
                         EntityManager entityManager) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.ingredientService = ingredientService;
        this.entityManager = entityManager;
    }

    /**
     * 材料を含む新しいレシピを作成します。
     *
     * @param ownerId レシピの所有者のID
     * @param title レシピのタイトル
     * @param steps 調理手順
     * @param cookTimeMin 調理時間（分単位）
     * @param tags レシピのタグ（カンマ区切り）
     * @param ingredients 数量を含む材料のリスト
     * @return 作成されたRecipeEntity
     * @throws IllegalArgumentException 必須フィールドがnullまたは空白の場合
     */
    @Transactional
    public RecipeEntity create(Long ownerId, String title, String steps, Integer cookTimeMin,
                               String tags, List<RecipeDto.IngredientItem> ingredients) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (steps == null || steps.isBlank()) {
            throw new IllegalArgumentException("Steps are required");
        }
        if (cookTimeMin == null || cookTimeMin <= 0) {
            throw new IllegalArgumentException("Cook time must be positive");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients are required");
        }

        // レシピエンティティを作成
        RecipeEntity recipe = new RecipeEntity(title, steps, cookTimeMin, tags, ownerId);
        RecipeEntity savedRecipe = recipeRepository.save(recipe);

        // レシピIDが生成されることを確実にするためにフラッシュ
        entityManager.flush();

        // レシピ材料を作成（セッション問題を回避するため1つずつ保存）
        List<RecipeIngredientEntity> recipeIngredients = new ArrayList<>();
        for (RecipeDto.IngredientItem item : ingredients) {
            IngredientEntity ingredient = ingredientService.findOrCreate(item.name());

            RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();
            recipeIngredient.setRecipe(savedRecipe);
            recipeIngredient.setIngredient(ingredient);
            recipeIngredient.setQuantity(item.quantity());

            // 重複キーの問題を回避するため個別に保存
            RecipeIngredientEntity saved = recipeIngredientRepository.save(recipeIngredient);
            recipeIngredients.add(saved);
        }

        // 材料がロードされたレシピを再取得
        return recipeRepository.findById(savedRecipe.getId()).orElse(savedRecipe);
    }

    /**
     * IDでレシピを検索します。
     *
     * @param id レシピID
     * @return RecipeEntity
     * @throws ResponseStatusException レシピが見つからない場合は404
     */
    @Transactional(readOnly = true)
    public RecipeEntity findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Recipe ID is required");
        }

        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }

    /**
     * フィルターとページネーションを使用してレシピを検索します。
     * 所有者、タイトル（部分一致）、最大調理時間、材料名によるフィルタリングをサポートします。
     *
     * @param ownerId 所有者ID（オプション）
     * @param q タイトル検索クエリ（オプション）
     * @param maxTime 最大調理時間（分単位、オプション）
     * @param ingredient 材料名（部分一致、オプション）
     * @param pageable ページネーションパラメータ
     * @return 条件に一致するRecipeEntityのページ
     */
    @Transactional(readOnly = true)
    public Page<RecipeEntity> search(Long ownerId, String q, Integer maxTime, String ingredient, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RecipeEntity> query = cb.createQuery(RecipeEntity.class);
        Root<RecipeEntity> recipe = query.from(RecipeEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // 所有者でフィルタリング
        if (ownerId != null) {
            predicates.add(cb.equal(recipe.get("ownerId"), ownerId));
        }

        // タイトルでフィルタリング（部分一致、大文字小文字を区別しない）
        if (q != null && !q.isBlank()) {
            predicates.add(cb.like(cb.lower(recipe.get("title")), "%" + q.toLowerCase() + "%"));
        }

        // 最大調理時間でフィルタリング
        if (maxTime != null) {
            predicates.add(cb.lessThanOrEqualTo(recipe.get("cookTimeMin"), maxTime));
        }

        // 材料名でフィルタリング（部分一致）
        if (ingredient != null && !ingredient.isBlank()) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<RecipeIngredientEntity> recipeIngredient = subquery.from(RecipeIngredientEntity.class);
            subquery.select(recipeIngredient.get("recipe").get("id"));
            subquery.where(cb.like(cb.lower(recipeIngredient.get("ingredient").get("name")),
                    "%" + ingredient.toLowerCase() + "%"));
            predicates.add(recipe.get("id").in(subquery));
        }

        query.where(predicates.toArray(new Predicate[0]));

        // Pageableからソートを適用
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(recipe.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(recipe.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // ページネーションを適用してクエリを実行
        TypedQuery<RecipeEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<RecipeEntity> results = typedQuery.getResultList();

        // 総件数をカウント
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<RecipeEntity> countRoot = countQuery.from(RecipeEntity.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = new ArrayList<>();
        if (ownerId != null) {
            countPredicates.add(cb.equal(countRoot.get("ownerId"), ownerId));
        }
        if (q != null && !q.isBlank()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("title")), "%" + q.toLowerCase() + "%"));
        }
        if (maxTime != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("cookTimeMin"), maxTime));
        }
        if (ingredient != null && !ingredient.isBlank()) {
            Subquery<Long> countSubquery = countQuery.subquery(Long.class);
            Root<RecipeIngredientEntity> countRecipeIngredient = countSubquery.from(RecipeIngredientEntity.class);
            countSubquery.select(countRecipeIngredient.get("recipe").get("id"));
            countSubquery.where(cb.like(cb.lower(countRecipeIngredient.get("ingredient").get("name")),
                    "%" + ingredient.toLowerCase() + "%"));
            countPredicates.add(countRoot.get("id").in(countSubquery));
        }
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * 既存のレシピを更新します。
     * リクエスト内のnullでないフィールドのみが更新されます。
     *
     * @param id レシピID
     * @param ownerId 所有者ID（認可チェック用）
     * @param req 更新するフィールドを含む更新リクエスト
     * @return 更新されたRecipeEntity
     * @throws ResponseStatusException レシピが見つからない場合は404、所有者でない場合は403
     */
    @Transactional
    public RecipeEntity update(Long id, Long ownerId, RecipeDto.UpdateRequest req) {
        if (id == null) {
            throw new IllegalArgumentException("Recipe ID is required");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        if (req == null) {
            throw new IllegalArgumentException("Update request is required");
        }

        RecipeEntity recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        // 認可チェック: 所有者のみが更新可能
        if (!recipe.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this recipe");
        }

        // 提供されたフィールドを更新
        if (req.title() != null && !req.title().isBlank()) {
            recipe.setTitle(req.title());
        }
        if (req.steps() != null && !req.steps().isBlank()) {
            recipe.setSteps(req.steps());
        }
        if (req.cookTimeMin() != null) {
            if (req.cookTimeMin() <= 0) {
                throw new IllegalArgumentException("Cook time must be positive");
            }
            recipe.setCookTimeMin(req.cookTimeMin());
        }
        if (req.tags() != null) {
            recipe.setTags(req.tags());
        }

        return recipeRepository.save(recipe);
    }

    /**
     * レシピを削除します。
     *
     * @param id レシピID
     * @param ownerId 所有者ID（認可チェック用）
     * @throws ResponseStatusException レシピが見つからない場合は404、所有者でない場合は403
     */
    @Transactional
    public void delete(Long id, Long ownerId) {
        if (id == null) {
            throw new IllegalArgumentException("Recipe ID is required");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }

        RecipeEntity recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        // 認可チェック: 所有者のみが削除可能
        if (!recipe.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this recipe");
        }

        recipeRepository.delete(recipe);
    }
}
