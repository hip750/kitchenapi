package com.example.kitchenapi.repository;

import com.example.kitchenapi.entity.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {

    /**
     * Find an ingredient by name.
     *
     * @param name the name of the ingredient to search for
     * @return an Optional containing the ingredient if found, or empty if not found
     */
    Optional<IngredientEntity> findByName(String name);

    /**
     * Check if an ingredient exists with the given name.
     *
     * @param name the ingredient name to check
     * @return true if an ingredient exists with the name, false otherwise
     */
    boolean existsByName(String name);
}
