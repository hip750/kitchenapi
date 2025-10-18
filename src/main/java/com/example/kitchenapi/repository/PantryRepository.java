package com.example.kitchenapi.repository;

import com.example.kitchenapi.entity.PantryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PantryRepository extends JpaRepository<PantryItemEntity, Long> {

    /**
     * Find all pantry items for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of pantry items
     */
    List<PantryItemEntity> findByUserId(Long userId);

    /**
     * Find a pantry item by user and ingredient.
     *
     * @param userId the ID of the user
     * @param ingredientId the ID of the ingredient
     * @return an Optional containing the pantry item if found
     */
    Optional<PantryItemEntity> findByUserIdAndIngredientId(Long userId, Long ingredientId);

    /**
     * Find all pantry items for a user that expire on or before a specific date.
     *
     * @param userId the ID of the user
     * @param date the expiration date to check
     * @return a list of expiring pantry items
     */
    List<PantryItemEntity> findByUserIdAndExpiresOnLessThanEqual(Long userId, LocalDate date);

    /**
     * Find all pantry items expiring between two dates for a specific user.
     *
     * @param userId the ID of the user
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of pantry items expiring in the date range
     */
    List<PantryItemEntity> findByUserIdAndExpiresOnBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find pantry items by user ordered by expiration date ascending.
     *
     * @param userId the ID of the user
     * @return a list of pantry items ordered by expiration date
     */
    List<PantryItemEntity> findByUserIdOrderByExpiresOnAsc(Long userId);

    /**
     * Count the number of pantry items for a specific user.
     *
     * @param userId the ID of the user
     * @return the count of pantry items
     */
    long countByUserId(Long userId);

    /**
     * Delete pantry items for a user by ingredient ID.
     *
     * @param userId the ID of the user
     * @param ingredientId the ID of the ingredient
     */
    void deleteByUserIdAndIngredientId(Long userId, Long ingredientId);

    /**
     * Find all pantry items that are expired (expires before today).
     *
     * @param userId the ID of the user
     * @param today today's date
     * @return a list of expired pantry items
     */
    @Query("SELECT p FROM PantryItemEntity p WHERE p.userId = :userId AND p.expiresOn < :today")
    List<PantryItemEntity> findExpiredItems(@Param("userId") Long userId, @Param("today") LocalDate today);
}
