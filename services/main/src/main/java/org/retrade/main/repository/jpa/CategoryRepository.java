package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CategoryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends BaseJpaRepository<CategoryEntity, String> {
    Optional<CategoryEntity> findByName(String name);
    
    List<CategoryEntity> findByCategoryParent(CategoryEntity parent);
    
    boolean existsByName(String name);

    long countDistinctByIdIn(@NonNull Set<String> ids);

    Set<CategoryEntity> findByIdIn(@NonNull Set<String> ids);


    @Query(value = """
    WITH RECURSIVE root_chain AS (
        SELECT id, parent_id, id AS origin
        FROM main.categories
        WHERE id IN (:ids)

        UNION ALL

        SELECT c.id, c.parent_id, rc.origin
        FROM main.categories c
        JOIN root_chain rc ON c.id = rc.parent_id
    )
    SELECT COUNT(DISTINCT id) = 1
    FROM root_chain
    WHERE parent_id IS NULL
    """, nativeQuery = true)
    boolean isSameRootCategory(@Param("ids") Set<String> ids);

    @Query(value = """
      WITH RECURSIVE ancestors AS (
        SELECT id, parent_id
        FROM main.categories
        WHERE id = :newParentId
        UNION ALL
        SELECT c.id, c.parent_id
        FROM main.categories c
        JOIN ancestors a ON c.id = a.parent_id
      )
      SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
      FROM ancestors WHERE id = :categoryId
    """, nativeQuery = true)
    boolean isCategoryLoop(@Param("id") String id);


    @Query(value = """
    WITH RECURSIVE upward AS (
        SELECT c.id, c.parent_id
        FROM main.categories c
        WHERE c.id IN (:ids)
        UNION
        SELECT c2.id, c2.parent_id
        FROM main.categories c2
        JOIN upward u ON c2.id = u.parent_id
    ),
    downward AS (
        SELECT c.id, c.parent_id
        FROM main.categories c
        WHERE c.id IN (:ids)
        UNION
        SELECT c3.id, c3.parent_id
        FROM main.categories c3
        JOIN downward d ON c3.parent_id = d.id
    )
    SELECT DISTINCT id FROM upward
    UNION
    SELECT DISTINCT id FROM downward
    """, nativeQuery = true)
    Set<String> findCategoryValidIdByCategoryIds(@Param("ids") Set<String> ids);
}
