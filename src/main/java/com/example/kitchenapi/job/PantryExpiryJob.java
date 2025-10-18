package com.example.kitchenapi.job;

import com.example.kitchenapi.entity.PantryItemEntity;
import com.example.kitchenapi.service.PantryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PantryExpiryJob - 期限切れ間近のパントリー項目をチェックするスケジュールジョブ
 *
 * 毎日午前9時に実行され、今後3日以内に期限切れになるパントリー項目を特定し、
 * 期限切れ項目を持つユーザーの警告をログに記録します。
 */
@Component
public class PantryExpiryJob {
    private static final Logger log = LoggerFactory.getLogger(PantryExpiryJob.class);
    private static final int EXPIRY_WARNING_DAYS = 3;

    private final PantryService pantryService;

    public PantryExpiryJob(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    /**
     * 毎日午前9時に期限切れ間近のパントリー項目をチェックします
     * Cron: 0 0 9 * * * (秒 分 時 日 月 曜日)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringItems() {
        log.info("Starting pantry expiry check job...");

        LocalDate today = LocalDate.now();
        LocalDate checkUntil = today.plusDays(EXPIRY_WARNING_DAYS);

        try {
            // 今日から3日後までの間に期限切れになる項目を検索
            List<PantryItemEntity> expiringItems = pantryService.findExpiringSoon(today, checkUntil);

            if (expiringItems.isEmpty()) {
                log.info("No items expiring within the next {} days", EXPIRY_WARNING_DAYS);
                return;
            }

            // 整理されたログのためにユーザーIDごとに項目をグループ化
            Map<Long, List<PantryItemEntity>> itemsByUser = expiringItems.stream()
                    .collect(Collectors.groupingBy(PantryItemEntity::getUserId));

            log.info("Found {} items expiring within {} days for {} users",
                    expiringItems.size(), EXPIRY_WARNING_DAYS, itemsByUser.size());

            // 各ユーザーの詳細をログに記録
            itemsByUser.forEach((userId, items) -> {
                log.warn("User {} has {} items expiring soon:", userId, items.size());
                items.forEach(item -> {
                    long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, item.getExpiresOn());
                    log.warn("  - {} ({}): expires on {} ({} days)",
                            item.getIngredient().getName(),
                            item.getAmount(),
                            item.getExpiresOn(),
                            daysUntilExpiry);
                });
            });

            log.info("Pantry expiry check job completed successfully");

        } catch (Exception e) {
            log.error("Error during pantry expiry check job", e);
        }
    }
}
