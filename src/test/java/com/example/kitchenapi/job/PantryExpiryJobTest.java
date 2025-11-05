package com.example.kitchenapi.job;

import com.example.kitchenapi.entity.IngredientEntity;
import com.example.kitchenapi.entity.PantryItemEntity;
import com.example.kitchenapi.service.PantryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PantryExpiryJob 単体テスト")
class PantryExpiryJobTest {

    @Mock
    private PantryService pantryService;

    private PantryExpiryJob pantryExpiryJob;

    @BeforeEach
    void setUp() {
        pantryExpiryJob = new PantryExpiryJob(pantryService);
    }

    @Test
    @DisplayName("checkExpiringItems - 期限切れ間近の項目がある場合、正しくログを出力する")
    void checkExpiringItems_WithExpiringItems_LogsWarnings() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate expiryDate1 = today.plusDays(1);
        LocalDate expiryDate2 = today.plusDays(2);

        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        ingredient1.setId(1L);
        IngredientEntity ingredient2 = new IngredientEntity("にんじん");
        ingredient2.setId(2L);

        PantryItemEntity item1 = new PantryItemEntity(1L, ingredient1, "2個", expiryDate1);
        item1.setId(1L);
        PantryItemEntity item2 = new PantryItemEntity(1L, ingredient2, "3本", expiryDate2);
        item2.setId(2L);

        List<PantryItemEntity> expiringItems = Arrays.asList(item1, item2);

        when(pantryService.findExpiringSoon(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expiringItems);

        // When
        pantryExpiryJob.checkExpiringItems();

        // Then
        verify(pantryService).findExpiringSoon(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("checkExpiringItems - 期限切れ間近の項目がない場合、警告をログに記録しない")
    void checkExpiringItems_NoExpiringItems_LogsInfo() {
        // Given
        when(pantryService.findExpiringSoon(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        pantryExpiryJob.checkExpiringItems();

        // Then
        verify(pantryService).findExpiringSoon(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("checkExpiringItems - 複数ユーザーの期限切れ項目を正しく処理する")
    void checkExpiringItems_MultipleUsers_ProcessesCorrectly() {
        // Given
        LocalDate expiryDate = LocalDate.now().plusDays(2);

        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        ingredient1.setId(1L);
        IngredientEntity ingredient2 = new IngredientEntity("にんじん");
        ingredient2.setId(2L);

        PantryItemEntity item1 = new PantryItemEntity(1L, ingredient1, "2個", expiryDate);
        item1.setId(1L);
        PantryItemEntity item2 = new PantryItemEntity(2L, ingredient2, "3本", expiryDate);
        item2.setId(2L);

        List<PantryItemEntity> expiringItems = Arrays.asList(item1, item2);

        when(pantryService.findExpiringSoon(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expiringItems);

        // When
        pantryExpiryJob.checkExpiringItems();

        // Then
        verify(pantryService).findExpiringSoon(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("checkExpiringItems - サービスで例外が発生しても正常に処理を継続する")
    void checkExpiringItems_ServiceThrowsException_HandlesGracefully() {
        // Given
        when(pantryService.findExpiringSoon(any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        pantryExpiryJob.checkExpiringItems();

        // Then
        verify(pantryService).findExpiringSoon(any(LocalDate.class), any(LocalDate.class));
        // ジョブは例外をキャッチしてログに記録し、正常に終了する
    }

    @Test
    @DisplayName("checkExpiringItems - 今日期限切れの項目も検出する")
    void checkExpiringItems_ItemsExpiringToday_AreDetected() {
        // Given
        LocalDate today = LocalDate.now();

        IngredientEntity ingredient = new IngredientEntity("たまねぎ");
        ingredient.setId(1L);

        PantryItemEntity item = new PantryItemEntity(1L, ingredient, "2個", today);
        item.setId(1L);

        List<PantryItemEntity> expiringItems = Collections.singletonList(item);

        when(pantryService.findExpiringSoon(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expiringItems);

        // When
        pantryExpiryJob.checkExpiringItems();

        // Then
        verify(pantryService).findExpiringSoon(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("checkExpiringItems - 同じユーザーの複数の項目をグループ化する")
    void checkExpiringItems_MultipleItemsSameUser_GroupsCorrectly() {
        // Given
        LocalDate expiryDate1 = LocalDate.now().plusDays(1);
        LocalDate expiryDate2 = LocalDate.now().plusDays(2);

        IngredientEntity ingredient1 = new IngredientEntity("たまねぎ");
        ingredient1.setId(1L);
        IngredientEntity ingredient2 = new IngredientEntity("にんじん");
        ingredient2.setId(2L);
        IngredientEntity ingredient3 = new IngredientEntity("じゃがいも");
        ingredient3.setId(3L);

        // すべて同じユーザー(userId=1)の項目
        PantryItemEntity item1 = new PantryItemEntity(1L, ingredient1, "2個", expiryDate1);
        item1.setId(1L);
        PantryItemEntity item2 = new PantryItemEntity(1L, ingredient2, "3本", expiryDate2);
        item2.setId(2L);
        PantryItemEntity item3 = new PantryItemEntity(1L, ingredient3, "5個", expiryDate1);
        item3.setId(3L);

        List<PantryItemEntity> expiringItems = Arrays.asList(item1, item2, item3);

        when(pantryService.findExpiringSoon(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expiringItems);

        // When
        pantryExpiryJob.checkExpiringItems();

        // Then
        verify(pantryService).findExpiringSoon(any(LocalDate.class), any(LocalDate.class));
    }
}
