package com.example.kitchenapi.service;

import com.example.kitchenapi.entity.IngredientEntity;
import com.example.kitchenapi.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 材料管理のサービス層
 * 材料の作成と取得操作を処理します。
 */
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    /**
     * 名前で材料を検索し、存在しない場合は作成します。
     * このメソッドは材料の一意性を保証し、既存の材料を再利用します。
     *
     * @param name 材料名
     * @return 見つかったまたは新しく作成されたIngredientEntity
     * @throws IllegalArgumentException 名前がnullまたは空白の場合
     */
    @Transactional
    public IngredientEntity findOrCreate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ingredient name is required");
        }

        // 既存の材料を検索
        return ingredientRepository.findByName(name)
                .orElseGet(() -> {
                    // 見つからない場合は新しい材料を作成
                    IngredientEntity newIngredient = new IngredientEntity(name);
                    return ingredientRepository.save(newIngredient);
                });
    }
}
