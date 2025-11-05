package com.example.kitchenapi.service;

import com.example.kitchenapi.entity.IngredientEntity;
import com.example.kitchenapi.entity.PantryItemEntity;
import com.example.kitchenapi.repository.PantryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PantryService 単体テスト")
class PantryServiceTest {

    @Mock
    private PantryRepository pantryRepository;

    @Mock
    private IngredientService ingredientService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PantryService pantryService;

    @Test
    @DisplayName("add - 正常系: 新しいパントリー項目を追加できる")
    void add_Success() {
        // Given
        Long userId = 1L;
        String ingredientName = "たまねぎ";
        String amount = "2個";
        LocalDate expiresOn = LocalDate.of(2025, 12, 31);

        IngredientEntity ingredient = new IngredientEntity(ingredientName);
        ingredient.setId(1L);

        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, amount, expiresOn);
        pantryItem.setId(10L);

        when(ingredientService.findOrCreate(ingredientName)).thenReturn(ingredient);
        when(pantryRepository.save(any(PantryItemEntity.class))).thenReturn(pantryItem);

        // When
        PantryItemEntity result = pantryService.add(userId, ingredientName, amount, expiresOn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(ingredientService, times(1)).findOrCreate(ingredientName);
        verify(pantryRepository, times(1)).save(any(PantryItemEntity.class));
    }

    @Test
    @DisplayName("add - 正常系: 賞味期限がnullでも追加できる")
    void add_NullExpiresOn() {
        // Given
        Long userId = 1L;
        String ingredientName = "たまねぎ";
        String amount = "2個";

        IngredientEntity ingredient = new IngredientEntity(ingredientName);
        ingredient.setId(1L);

        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, amount, null);
        pantryItem.setId(10L);

        when(ingredientService.findOrCreate(ingredientName)).thenReturn(ingredient);
        when(pantryRepository.save(any(PantryItemEntity.class))).thenReturn(pantryItem);

        // When
        PantryItemEntity result = pantryService.add(userId, ingredientName, amount, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExpiresOn()).isNull();

        verify(ingredientService, times(1)).findOrCreate(ingredientName);
        verify(pantryRepository, times(1)).save(any(PantryItemEntity.class));
    }

    @Test
    @DisplayName("add - 異常系: userIdがnull")
    void add_NullUserId() {
        // When & Then
        assertThatThrownBy(() -> pantryService.add(null, "たまねぎ", "2個", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");

        verify(ingredientService, never()).findOrCreate(any());
        verify(pantryRepository, never()).save(any());
    }

    @Test
    @DisplayName("add - 異常系: ingredientNameがnull")
    void add_NullIngredientName() {
        // When & Then
        assertThatThrownBy(() -> pantryService.add(1L, null, "2個", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient name is required");

        verify(ingredientService, never()).findOrCreate(any());
        verify(pantryRepository, never()).save(any());
    }

    @Test
    @DisplayName("add - 異常系: ingredientNameが空白")
    void add_BlankIngredientName() {
        // When & Then
        assertThatThrownBy(() -> pantryService.add(1L, "   ", "2個", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient name is required");

        verify(ingredientService, never()).findOrCreate(any());
        verify(pantryRepository, never()).save(any());
    }

    @Test
    @DisplayName("add - 異常系: amountがnull")
    void add_NullAmount() {
        // When & Then
        assertThatThrownBy(() -> pantryService.add(1L, "たまねぎ", null, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount is required");

        verify(ingredientService, never()).findOrCreate(any());
        verify(pantryRepository, never()).save(any());
    }

    @Test
    @DisplayName("add - 異常系: amountが空白")
    void add_BlankAmount() {
        // When & Then
        assertThatThrownBy(() -> pantryService.add(1L, "たまねぎ", "   ", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount is required");

        verify(ingredientService, never()).findOrCreate(any());
        verify(pantryRepository, never()).save(any());
    }

    @Test
    @DisplayName("findByUserId - 正常系: フィルター無しでパントリー項目を取得")
    void findByUserId_NoFilters() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        IngredientEntity ingredient2 = new IngredientEntity("にんじん");

        List<PantryItemEntity> items = Arrays.asList(
                new PantryItemEntity(userId, ingredient1, "2個", LocalDate.now().plusDays(5)),
                new PantryItemEntity(userId, ingredient2, "3本", LocalDate.now().plusDays(10))
        );

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PantryItemEntity> query = mock(CriteriaQuery.class);
        Root<PantryItemEntity> root = mock(Root.class);
        TypedQuery<PantryItemEntity> typedQuery = mock(TypedQuery.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<PantryItemEntity> countRoot = mock(Root.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);
        Path userIdPath = mock(Path.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(PantryItemEntity.class)).thenReturn(query);
        when(query.from(PantryItemEntity.class)).thenReturn(root);
        when(root.get("userId")).thenReturn(userIdPath);
        when(cb.equal(userIdPath, userId)).thenReturn(mock(Predicate.class));
        when(query.where(any(Predicate[].class))).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(items);

        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(PantryItemEntity.class)).thenReturn(countRoot);
        when(countQuery.select(any())).thenReturn(countQuery);
        when(cb.count(any())).thenReturn(mock(Expression.class));
        when(countRoot.get("userId")).thenReturn(userIdPath);
        when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(2L);

        // When
        Page<PantryItemEntity> result = pantryService.findByUserId(userId, null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByUserId - 異常系: userIdがnull")
    void findByUserId_NullUserId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        assertThatThrownBy(() -> pantryService.findByUserId(null, null, null, null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }

    @Test
    @DisplayName("update - 正常系: パントリー項目を更新できる")
    void update_Success() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;
        String newAmount = "3個";
        LocalDate newExpiresOn = LocalDate.of(2026, 1, 1);

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, "2個", LocalDate.of(2025, 12, 31));
        pantryItem.setId(itemId);

        when(pantryRepository.findById(itemId)).thenReturn(Optional.of(pantryItem));
        when(pantryRepository.save(any(PantryItemEntity.class))).thenReturn(pantryItem);

        // When
        PantryItemEntity result = pantryService.update(itemId, userId, newAmount, newExpiresOn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(newAmount);
        assertThat(result.getExpiresOn()).isEqualTo(newExpiresOn);

        verify(pantryRepository, times(1)).findById(itemId);
        verify(pantryRepository, times(1)).save(pantryItem);
    }

    @Test
    @DisplayName("update - 正常系: 部分更新（amountのみ更新）")
    void update_OnlyAmount() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;
        String newAmount = "5個";
        LocalDate originalExpiresOn = LocalDate.of(2025, 12, 31);

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, "2個", originalExpiresOn);
        pantryItem.setId(itemId);

        when(pantryRepository.findById(itemId)).thenReturn(Optional.of(pantryItem));
        when(pantryRepository.save(any(PantryItemEntity.class))).thenReturn(pantryItem);

        // When
        PantryItemEntity result = pantryService.update(itemId, userId, newAmount, null);

        // Then
        assertThat(result.getAmount()).isEqualTo(newAmount);
        assertThat(result.getExpiresOn()).isEqualTo(originalExpiresOn); // 変更されていない
    }

    @Test
    @DisplayName("update - 異常系: IDがnull")
    void update_NullId() {
        // When & Then
        assertThatThrownBy(() -> pantryService.update(null, 1L, "3個", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pantry item ID is required");

        verify(pantryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("update - 異常系: userIdがnull")
    void update_NullUserId() {
        // When & Then
        assertThatThrownBy(() -> pantryService.update(1L, null, "3個", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");

        verify(pantryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("update - 異常系: パントリー項目が見つからない")
    void update_ItemNotFound() {
        // Given
        Long itemId = 999L;
        when(pantryRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pantryService.update(itemId, 1L, "3個", LocalDate.now()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Pantry item not found");

        verify(pantryRepository, times(1)).findById(itemId);
        verify(pantryRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - 異常系: 所有者でないユーザーが更新しようとする")
    void update_NotOwner() {
        // Given
        Long itemId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        PantryItemEntity pantryItem = new PantryItemEntity(ownerId, ingredient, "2個", LocalDate.now());
        pantryItem.setId(itemId);

        when(pantryRepository.findById(itemId)).thenReturn(Optional.of(pantryItem));

        // When & Then
        assertThatThrownBy(() -> pantryService.update(itemId, otherUserId, "3個", LocalDate.now()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You don't have permission to update this pantry item");

        verify(pantryRepository, times(1)).findById(itemId);
        verify(pantryRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - 正常系: パントリー項目を削除できる")
    void delete_Success() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, "2個", LocalDate.now());
        pantryItem.setId(itemId);

        when(pantryRepository.findById(itemId)).thenReturn(Optional.of(pantryItem));

        // When
        pantryService.delete(itemId, userId);

        // Then
        verify(pantryRepository, times(1)).findById(itemId);
        verify(pantryRepository, times(1)).delete(pantryItem);
    }

    @Test
    @DisplayName("delete - 異常系: IDがnull")
    void delete_NullId() {
        // When & Then
        assertThatThrownBy(() -> pantryService.delete(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pantry item ID is required");

        verify(pantryRepository, never()).findById(any());
        verify(pantryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - 異常系: userIdがnull")
    void delete_NullUserId() {
        // When & Then
        assertThatThrownBy(() -> pantryService.delete(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");

        verify(pantryRepository, never()).findById(any());
        verify(pantryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - 異常系: パントリー項目が見つからない")
    void delete_ItemNotFound() {
        // Given
        Long itemId = 999L;
        when(pantryRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pantryService.delete(itemId, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Pantry item not found");

        verify(pantryRepository, times(1)).findById(itemId);
        verify(pantryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - 異常系: 所有者でないユーザーが削除しようとする")
    void delete_NotOwner() {
        // Given
        Long itemId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        PantryItemEntity pantryItem = new PantryItemEntity(ownerId, ingredient, "2個", LocalDate.now());
        pantryItem.setId(itemId);

        when(pantryRepository.findById(itemId)).thenReturn(Optional.of(pantryItem));

        // When & Then
        assertThatThrownBy(() -> pantryService.delete(itemId, otherUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You don't have permission to delete this pantry item");

        verify(pantryRepository, times(1)).findById(itemId);
        verify(pantryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("findExpiringSoon - 正常系: 期限が近い項目を取得できる")
    void findExpiringSoon_Success() {
        // Given
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(7);

        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        IngredientEntity ingredient2 = new IngredientEntity("にんじん");

        List<PantryItemEntity> expiringItems = Arrays.asList(
                new PantryItemEntity(1L, ingredient1, "2個", from.plusDays(2)),
                new PantryItemEntity(1L, ingredient2, "3本", from.plusDays(5))
        );

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PantryItemEntity> query = mock(CriteriaQuery.class);
        Root<PantryItemEntity> root = mock(Root.class);
        TypedQuery<PantryItemEntity> typedQuery = mock(TypedQuery.class);
        Path expiresOnPath = mock(Path.class);
        Path userIdPath = mock(Path.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(PantryItemEntity.class)).thenReturn(query);
        when(query.from(PantryItemEntity.class)).thenReturn(root);
        when(root.get("expiresOn")).thenReturn(expiresOnPath);
        when(root.get("userId")).thenReturn(userIdPath);
        when(cb.between(expiresOnPath, from, to)).thenReturn(mock(Predicate.class));
        when(query.where(any(Predicate.class))).thenReturn(query);
        when(cb.asc(userIdPath)).thenReturn(mock(Order.class));
        when(cb.asc(expiresOnPath)).thenReturn(mock(Order.class));
        when(query.orderBy(any(Order.class), any(Order.class))).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expiringItems);

        // When
        List<PantryItemEntity> result = pantryService.findExpiringSoon(from, to);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findExpiringSoon - 異常系: fromがnull")
    void findExpiringSoon_NullFrom() {
        // When & Then
        assertThatThrownBy(() -> pantryService.findExpiringSoon(null, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From date is required");
    }

    @Test
    @DisplayName("findExpiringSoon - 異常系: toがnull")
    void findExpiringSoon_NullTo() {
        // When & Then
        assertThatThrownBy(() -> pantryService.findExpiringSoon(LocalDate.now(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("To date is required");
    }

    @Test
    @DisplayName("update - 正常系: 空白のamountは更新されない")
    void update_BlankAmount_NotUpdated() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;
        String originalAmount = "2個";
        String blankAmount = "   ";
        LocalDate newExpiresOn = LocalDate.of(2026, 1, 1);

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        PantryItemEntity pantryItem = new PantryItemEntity(userId, ingredient, originalAmount, LocalDate.of(2025, 12, 31));
        pantryItem.setId(itemId);

        when(pantryRepository.findById(itemId)).thenReturn(Optional.of(pantryItem));
        when(pantryRepository.save(any(PantryItemEntity.class))).thenReturn(pantryItem);

        // When
        PantryItemEntity result = pantryService.update(itemId, userId, blankAmount, newExpiresOn);

        // Then
        assertThat(result.getAmount()).isEqualTo(originalAmount); // 変更されていない
        assertThat(result.getExpiresOn()).isEqualTo(newExpiresOn); // 更新されている
    }

    @Test
    @DisplayName("findByUserId - 正常系: 材料名でフィルタリング")
    void findByUserId_WithIngredientFilter() {
        // Given
        Long userId = 1L;
        String ingredientFilter = "たま";
        Pageable pageable = PageRequest.of(0, 10);

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        List<PantryItemEntity> items = Arrays.asList(
                new PantryItemEntity(userId, ingredient, "2個", LocalDate.now().plusDays(5))
        );

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PantryItemEntity> query = mock(CriteriaQuery.class);
        Root<PantryItemEntity> root = mock(Root.class);
        TypedQuery<PantryItemEntity> typedQuery = mock(TypedQuery.class);

        Path userIdPath = mock(Path.class);
        Path ingredientPath = mock(Path.class);
        Path ingredientNamePath = mock(Path.class);
        Expression lowerExpr = mock(Expression.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(PantryItemEntity.class)).thenReturn(query);
        when(query.from(PantryItemEntity.class)).thenReturn(root);
        when(root.get("userId")).thenReturn(userIdPath);
        when(root.get("ingredient")).thenReturn(ingredientPath);
        when(ingredientPath.get("name")).thenReturn(ingredientNamePath);
        when(cb.lower(ingredientNamePath)).thenReturn(lowerExpr);
        when(cb.equal(userIdPath, userId)).thenReturn(mock(Predicate.class));
        when(cb.like(lowerExpr, "%たま%")).thenReturn(mock(Predicate.class));
        when(query.where(any(Predicate[].class))).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(items);

        // Count query
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<PantryItemEntity> countRoot = mock(Root.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);
        Path countUserIdPath = mock(Path.class);
        Path countIngredientPath = mock(Path.class);
        Path countIngredientNamePath = mock(Path.class);
        Expression countLowerExpr = mock(Expression.class);

        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(PantryItemEntity.class)).thenReturn(countRoot);
        when(countQuery.select(any())).thenReturn(countQuery);
        when(cb.count(any())).thenReturn(mock(Expression.class));
        when(countRoot.get("userId")).thenReturn(countUserIdPath);
        when(countRoot.get("ingredient")).thenReturn(countIngredientPath);
        when(countIngredientPath.get("name")).thenReturn(countIngredientNamePath);
        when(cb.lower(countIngredientNamePath)).thenReturn(countLowerExpr);
        when(cb.like(countLowerExpr, "%たま%")).thenReturn(mock(Predicate.class));
        when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        // When
        Page<PantryItemEntity> result = pantryService.findByUserId(userId, ingredientFilter, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(cb).like(lowerExpr, "%たま%");
    }
}
