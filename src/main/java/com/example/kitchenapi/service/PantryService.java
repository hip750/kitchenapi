package com.example.kitchenapi.service;

import com.example.kitchenapi.entity.IngredientEntity;
import com.example.kitchenapi.entity.PantryItemEntity;
import com.example.kitchenapi.repository.PantryRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * パントリー項目管理のサービス層
 * パントリーのCRUD操作と賞味期限追跡を処理します。
 */
@Service
public class PantryService {

    private final PantryRepository pantryRepository;
    private final IngredientService ingredientService;
    private final EntityManager entityManager;

    public PantryService(PantryRepository pantryRepository,
                         IngredientService ingredientService,
                         EntityManager entityManager) {
        this.pantryRepository = pantryRepository;
        this.ingredientService = ingredientService;
        this.entityManager = entityManager;
    }

    /**
     * ユーザーに新しいパントリー項目を追加します。
     *
     * @param userId ユーザーID
     * @param ingredientName 材料名
     * @param amount 量/数量
     * @param expiresOn 賞味期限
     * @return 作成されたPantryItemEntity
     * @throws IllegalArgumentException 必須フィールドがnullまたは空白の場合
     */
    @Transactional
    public PantryItemEntity add(Long userId, String ingredientName, String amount, LocalDate expiresOn) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (ingredientName == null || ingredientName.isBlank()) {
            throw new IllegalArgumentException("Ingredient name is required");
        }
        if (amount == null || amount.isBlank()) {
            throw new IllegalArgumentException("Amount is required");
        }

        // 材料を検索または作成
        IngredientEntity ingredient = ingredientService.findOrCreate(ingredientName);

        // パントリー項目を作成
        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, amount, expiresOn);
        return pantryRepository.save(pantryItem);
    }

    /**
     * オプションのフィルターとページネーションを使用してユーザーのパントリー項目を検索します。
     *
     * @param userId ユーザーID
     * @param ingredient 材料名フィルター（部分一致、オプション）
     * @param expFrom 賞味期限の開始日フィルター（オプション）
     * @param expTo 賞味期限の終了日フィルター（オプション）
     * @param pageable ページネーションパラメータ
     * @return 条件に一致するPantryItemEntityのページ
     */
    @Transactional(readOnly = true)
    public Page<PantryItemEntity> findByUserId(Long userId, String ingredient,
                                                LocalDate expFrom, LocalDate expTo, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PantryItemEntity> query = cb.createQuery(PantryItemEntity.class);
        Root<PantryItemEntity> pantry = query.from(PantryItemEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // ユーザーIDでフィルタリング
        predicates.add(cb.equal(pantry.get("userId"), userId));

        // 材料名でフィルタリング（部分一致、大文字小文字を区別しない）
        if (ingredient != null && !ingredient.isBlank()) {
            predicates.add(cb.like(cb.lower(pantry.get("ingredient").get("name")),
                    "%" + ingredient.toLowerCase() + "%"));
        }

        // 賞味期限の範囲でフィルタリング
        if (expFrom != null && expTo != null) {
            predicates.add(cb.between(pantry.get("expiresOn"), expFrom, expTo));
        } else if (expFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(pantry.get("expiresOn"), expFrom));
        } else if (expTo != null) {
            predicates.add(cb.lessThanOrEqualTo(pantry.get("expiresOn"), expTo));
        }

        query.where(predicates.toArray(new Predicate[0]));

        // Pageableからソートを適用
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(pantry.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(pantry.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // ページネーションを適用してクエリを実行
        TypedQuery<PantryItemEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<PantryItemEntity> results = typedQuery.getResultList();

        // 総件数をカウント
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PantryItemEntity> countRoot = countQuery.from(PantryItemEntity.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countRoot.get("userId"), userId));
        if (ingredient != null && !ingredient.isBlank()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("ingredient").get("name")),
                    "%" + ingredient.toLowerCase() + "%"));
        }
        if (expFrom != null && expTo != null) {
            countPredicates.add(cb.between(countRoot.get("expiresOn"), expFrom, expTo));
        } else if (expFrom != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("expiresOn"), expFrom));
        } else if (expTo != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("expiresOn"), expTo));
        }
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * パントリー項目を更新します。
     *
     * @param id パントリー項目ID
     * @param userId ユーザーID（認可チェック用）
     * @param amount 新しい数量（オプション）
     * @param expiresOn 新しい賞味期限（オプション）
     * @return 更新されたPantryItemEntity
     * @throws ResponseStatusException パントリー項目が見つからない場合は404、所有者でない場合は403
     */
    @Transactional
    public PantryItemEntity update(Long id, Long userId, String amount, LocalDate expiresOn) {
        if (id == null) {
            throw new IllegalArgumentException("Pantry item ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        PantryItemEntity pantryItem = pantryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pantry item not found"));

        // 認可チェック: 所有者のみが更新可能
        if (!pantryItem.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this pantry item");
        }

        // 提供されたフィールドを更新
        if (amount != null && !amount.isBlank()) {
            pantryItem.setAmount(amount);
        }
        if (expiresOn != null) {
            pantryItem.setExpiresOn(expiresOn);
        }

        return pantryRepository.save(pantryItem);
    }

    /**
     * パントリー項目を削除します。
     *
     * @param id パントリー項目ID
     * @param userId ユーザーID（認可チェック用）
     * @throws ResponseStatusException パントリー項目が見つからない場合は404、所有者でない場合は403
     */
    @Transactional
    public void delete(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("Pantry item ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        PantryItemEntity pantryItem = pantryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pantry item not found"));

        // 認可チェック: 所有者のみが削除可能
        if (!pantryItem.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this pantry item");
        }

        pantryRepository.delete(pantryItem);
    }

    /**
     * 間もなく期限切れになるパントリー項目を検索します（日付範囲内）。
     * このメソッドは、リマインダー通知が必要な項目を見つけるためにスケジュールされたジョブで使用されます。
     *
     * @param from 範囲の開始日
     * @param to 範囲の終了日
     * @return 日付範囲内に期限切れになるPantryItemEntityのリスト
     */
    @Transactional(readOnly = true)
    public List<PantryItemEntity> findExpiringSoon(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new IllegalArgumentException("From date is required");
        }
        if (to == null) {
            throw new IllegalArgumentException("To date is required");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PantryItemEntity> query = cb.createQuery(PantryItemEntity.class);
        Root<PantryItemEntity> pantry = query.from(PantryItemEntity.class);

        // fromとtoの日付間で期限切れになる項目を検索
        Predicate datePredicate = cb.between(pantry.get("expiresOn"), from, to);
        query.where(datePredicate);

        // ユーザーIDと賞味期限で並び替え
        query.orderBy(cb.asc(pantry.get("userId")), cb.asc(pantry.get("expiresOn")));

        return entityManager.createQuery(query).getResultList();
    }
}
