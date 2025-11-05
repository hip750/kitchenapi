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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService 単体テスト")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private IngredientService ingredientService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("create - 正常系: 新しいレシピを作成できる")
    void create_Success() {
        // Given
        Long ownerId = 1L;
        String title = "カレーライス";
        String steps = "1. 材料を切る\\n2. 煮込む";
        Integer cookTimeMin = 30;
        String tags = "簡単,おいしい";
        List<RecipeDto.IngredientItem> ingredients = Arrays.asList(
                new RecipeDto.IngredientItem("たまねぎ", "1個"),
                new RecipeDto.IngredientItem("にんじん", "1本")
        );

        RecipeEntity savedRecipe = new RecipeEntity(title, steps, cookTimeMin, tags, ownerId);
        savedRecipe.setId(10L);

        IngredientEntity onion = new IngredientEntity("たまねぎ");
        onion.setId(1L);
        IngredientEntity carrot = new IngredientEntity("にんじん");
        carrot.setId(2L);

        when(recipeRepository.save(any(RecipeEntity.class))).thenReturn(savedRecipe);
        when(ingredientService.findOrCreate("たまねぎ")).thenReturn(onion);
        when(ingredientService.findOrCreate("にんじん")).thenReturn(carrot);
        when(recipeIngredientRepository.save(any(RecipeIngredientEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(savedRecipe));

        // When
        RecipeEntity result = recipeService.create(ownerId, title, steps, cookTimeMin, tags, ingredients);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getOwnerId()).isEqualTo(ownerId);

        verify(recipeRepository, times(1)).save(any(RecipeEntity.class));
        verify(entityManager, times(1)).flush();
        verify(ingredientService, times(2)).findOrCreate(any());
        verify(recipeIngredientRepository, times(2)).save(any(RecipeIngredientEntity.class));
        verify(recipeRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("create - 異常系: ownerIdがnull")
    void create_NullOwnerId() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = List.of(
                new RecipeDto.IngredientItem("たまねぎ", "1個")
        );

        // When & Then
        assertThatThrownBy(() -> recipeService.create(null, "Title", "Steps", 30, "tags", ingredients))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Owner ID is required");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: titleがnull")
    void create_NullTitle() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = List.of(
                new RecipeDto.IngredientItem("たまねぎ", "1個")
        );

        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, null, "Steps", 30, "tags", ingredients))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is required");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: titleが空白")
    void create_BlankTitle() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = List.of(
                new RecipeDto.IngredientItem("たまねぎ", "1個")
        );

        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, "   ", "Steps", 30, "tags", ingredients))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is required");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: stepsがnull")
    void create_NullSteps() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = List.of(
                new RecipeDto.IngredientItem("たまねぎ", "1個")
        );

        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, "Title", null, 30, "tags", ingredients))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Steps are required");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: stepsが空白")
    void create_BlankSteps() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = List.of(
                new RecipeDto.IngredientItem("たまねぎ", "1個")
        );

        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, "Title", "   ", 30, "tags", ingredients))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Steps are required");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: cookTimeMinがnull")
    void create_NullCookTime() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = List.of(
                new RecipeDto.IngredientItem("たまねぎ", "1個")
        );

        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, "Title", "Steps", null, "tags", ingredients))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cook time must be positive");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: cookTimeMinが0以下")
    void create_InvalidCookTime() {
        // Given
        List<RecipeDto.IngredientItem> ingredients = List.of(
                new RecipeDto.IngredientItem("たまねぎ", "1個")
        );

        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, "Title", "Steps", 0, "tags", ingredients))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cook time must be positive");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: ingredientsがnull")
    void create_NullIngredients() {
        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, "Title", "Steps", 30, "tags", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredients are required");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - 異常系: ingredientsが空リスト")
    void create_EmptyIngredients() {
        // When & Then
        assertThatThrownBy(() -> recipeService.create(1L, "Title", "Steps", 30, "tags", new ArrayList<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredients are required");

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById - 正常系: レシピを取得できる")
    void findById_Success() {
        // Given
        Long recipeId = 1L;
        RecipeEntity recipe = new RecipeEntity("Title", "Steps", 30, "tags", 1L);
        recipe.setId(recipeId);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        // When
        RecipeEntity result = recipeService.findById(recipeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(recipeId);
        verify(recipeRepository, times(1)).findById(recipeId);
    }

    @Test
    @DisplayName("findById - 異常系: IDがnull")
    void findById_NullId() {
        // When & Then
        assertThatThrownBy(() -> recipeService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recipe ID is required");

        verify(recipeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("findById - 異常系: レシピが見つからない")
    void findById_NotFound() {
        // Given
        Long recipeId = 999L;
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> recipeService.findById(recipeId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Recipe not found");

        verify(recipeRepository, times(1)).findById(recipeId);
    }

    @Test
    @DisplayName("search - 正常系: フィルター無しで全レシピを取得")
    void search_NoFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<RecipeEntity> recipes = Arrays.asList(
                new RecipeEntity("Recipe1", "Steps1", 20, "tag1", 1L),
                new RecipeEntity("Recipe2", "Steps2", 30, "tag2", 2L)
        );

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<RecipeEntity> query = mock(CriteriaQuery.class);
        Root<RecipeEntity> root = mock(Root.class);
        TypedQuery<RecipeEntity> typedQuery = mock(TypedQuery.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<RecipeEntity> countRoot = mock(Root.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(RecipeEntity.class)).thenReturn(query);
        when(query.from(RecipeEntity.class)).thenReturn(root);
        when(query.where(any(Predicate[].class))).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(recipes);

        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(RecipeEntity.class)).thenReturn(countRoot);
        when(countQuery.select(any())).thenReturn(countQuery);
        when(cb.count(any())).thenReturn(mock(Expression.class));
        when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(2L);

        // When
        Page<RecipeEntity> result = recipeService.search(null, null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("update - 正常系: レシピを更新できる")
    void update_Success() {
        // Given
        Long recipeId = 1L;
        Long ownerId = 1L;
        RecipeEntity recipe = new RecipeEntity("Old Title", "Old Steps", 20, "old", ownerId);
        recipe.setId(recipeId);

        RecipeDto.UpdateRequest updateRequest = new RecipeDto.UpdateRequest(
                "New Title",
                "New Steps",
                40,
                "new,tags"
        );

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(RecipeEntity.class))).thenReturn(recipe);

        // When
        RecipeEntity result = recipeService.update(recipeId, ownerId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getSteps()).isEqualTo("New Steps");
        assertThat(result.getCookTimeMin()).isEqualTo(40);
        assertThat(result.getTags()).isEqualTo("new,tags");

        verify(recipeRepository, times(1)).findById(recipeId);
        verify(recipeRepository, times(1)).save(recipe);
    }

    @Test
    @DisplayName("update - 正常系: 部分更新（一部のフィールドのみ更新）")
    void update_PartialUpdate() {
        // Given
        Long recipeId = 1L;
        Long ownerId = 1L;
        RecipeEntity recipe = new RecipeEntity("Old Title", "Old Steps", 20, "old", ownerId);
        recipe.setId(recipeId);

        RecipeDto.UpdateRequest updateRequest = new RecipeDto.UpdateRequest(
                "New Title",
                null,  // stepsは更新しない
                null,  // cookTimeMinは更新しない
                null   // tagsは更新しない
        );

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(RecipeEntity.class))).thenReturn(recipe);

        // When
        RecipeEntity result = recipeService.update(recipeId, ownerId, updateRequest);

        // Then
        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getSteps()).isEqualTo("Old Steps"); // 変更されていない
        assertThat(result.getCookTimeMin()).isEqualTo(20); // 変更されていない
        assertThat(result.getTags()).isEqualTo("old"); // 変更されていない
    }

    @Test
    @DisplayName("update - 異常系: IDがnull")
    void update_NullId() {
        // Given
        RecipeDto.UpdateRequest updateRequest = new RecipeDto.UpdateRequest("Title", "Steps", 30, "tags");

        // When & Then
        assertThatThrownBy(() -> recipeService.update(null, 1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recipe ID is required");

        verify(recipeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("update - 異常系: ownerIdがnull")
    void update_NullOwnerId() {
        // Given
        RecipeDto.UpdateRequest updateRequest = new RecipeDto.UpdateRequest("Title", "Steps", 30, "tags");

        // When & Then
        assertThatThrownBy(() -> recipeService.update(1L, null, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Owner ID is required");

        verify(recipeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("update - 異常系: updateRequestがnull")
    void update_NullRequest() {
        // When & Then
        assertThatThrownBy(() -> recipeService.update(1L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Update request is required");

        verify(recipeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("update - 異常系: レシピが見つからない")
    void update_RecipeNotFound() {
        // Given
        Long recipeId = 999L;
        RecipeDto.UpdateRequest updateRequest = new RecipeDto.UpdateRequest("Title", "Steps", 30, "tags");

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> recipeService.update(recipeId, 1L, updateRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Recipe not found");

        verify(recipeRepository, times(1)).findById(recipeId);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - 異常系: 所有者でないユーザーが更新しようとする")
    void update_NotOwner() {
        // Given
        Long recipeId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;
        RecipeEntity recipe = new RecipeEntity("Title", "Steps", 30, "tags", ownerId);
        recipe.setId(recipeId);

        RecipeDto.UpdateRequest updateRequest = new RecipeDto.UpdateRequest("New Title", "New Steps", 40, "new");

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        // When & Then
        assertThatThrownBy(() -> recipeService.update(recipeId, otherUserId, updateRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You don't have permission to update this recipe");

        verify(recipeRepository, times(1)).findById(recipeId);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - 異常系: cookTimeMinが0以下")
    void update_InvalidCookTime() {
        // Given
        Long recipeId = 1L;
        Long ownerId = 1L;
        RecipeEntity recipe = new RecipeEntity("Title", "Steps", 30, "tags", ownerId);
        recipe.setId(recipeId);

        RecipeDto.UpdateRequest updateRequest = new RecipeDto.UpdateRequest(
                "New Title",
                "New Steps",
                -10,  // 無効な値
                "new"
        );

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        // When & Then
        assertThatThrownBy(() -> recipeService.update(recipeId, ownerId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cook time must be positive");

        verify(recipeRepository, times(1)).findById(recipeId);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - 正常系: レシピを削除できる")
    void delete_Success() {
        // Given
        Long recipeId = 1L;
        Long ownerId = 1L;
        RecipeEntity recipe = new RecipeEntity("Title", "Steps", 30, "tags", ownerId);
        recipe.setId(recipeId);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        // When
        recipeService.delete(recipeId, ownerId);

        // Then
        verify(recipeRepository, times(1)).findById(recipeId);
        verify(recipeRepository, times(1)).delete(recipe);
    }

    @Test
    @DisplayName("delete - 異常系: IDがnull")
    void delete_NullId() {
        // When & Then
        assertThatThrownBy(() -> recipeService.delete(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recipe ID is required");

        verify(recipeRepository, never()).findById(any());
        verify(recipeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - 異常系: ownerIdがnull")
    void delete_NullOwnerId() {
        // When & Then
        assertThatThrownBy(() -> recipeService.delete(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Owner ID is required");

        verify(recipeRepository, never()).findById(any());
        verify(recipeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - 異常系: レシピが見つからない")
    void delete_RecipeNotFound() {
        // Given
        Long recipeId = 999L;
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> recipeService.delete(recipeId, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Recipe not found");

        verify(recipeRepository, times(1)).findById(recipeId);
        verify(recipeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - 異常系: 所有者でないユーザーが削除しようとする")
    void delete_NotOwner() {
        // Given
        Long recipeId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;
        RecipeEntity recipe = new RecipeEntity("Title", "Steps", 30, "tags", ownerId);
        recipe.setId(recipeId);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));

        // When & Then
        assertThatThrownBy(() -> recipeService.delete(recipeId, otherUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You don't have permission to delete this recipe");

        verify(recipeRepository, times(1)).findById(recipeId);
        verify(recipeRepository, never()).delete(any());
    }
}
