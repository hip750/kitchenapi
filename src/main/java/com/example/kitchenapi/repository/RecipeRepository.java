package com.example.kitchenapi.repository;

import com.example.kitchenapi.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

    /**
     * Find all recipes owned by a specific user.
     *
     * @param ownerId the ID of the owner
     * @return a list of recipes owned by the user
     */
    List<RecipeEntity> findByOwnerId(Long ownerId);

    /**
     * Find recipes by title containing a search string (case-insensitive).
     *
     * @param title the title search string
     * @return a list of recipes with matching titles
     */
    List<RecipeEntity> findByTitleContainingIgnoreCase(String title);

    /**
     * Find recipes by owner and title containing a search string (case-insensitive).
     *
     * @param ownerId the ID of the owner
     * @param title the title search string
     * @return a list of recipes matching the criteria
     */
    List<RecipeEntity> findByOwnerIdAndTitleContainingIgnoreCase(Long ownerId, String title);

    /**
     * Find recipes where cooking time is less than or equal to the specified minutes.
     *
     * @param cookTimeMin the maximum cooking time in minutes
     * @return a list of recipes within the time limit
     */
    List<RecipeEntity> findByCookTimeMinLessThanEqual(Integer cookTimeMin);

    /**
     * Find recipes by tags containing a specific tag string.
     *
     * @param tag the tag to search for
     * @return a list of recipes containing the tag
     */
    @Query("SELECT r FROM RecipeEntity r WHERE r.tags LIKE %:tag%")
    List<RecipeEntity> findByTagsContaining(@Param("tag") String tag);

    /**
     * Find recipes by owner ordered by creation date descending.
     *
     * @param ownerId the ID of the owner
     * @return a list of recipes ordered by newest first
     */
    List<RecipeEntity> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
