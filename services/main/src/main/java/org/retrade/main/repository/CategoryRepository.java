package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CategoryEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends BaseJpaRepository<CategoryEntity, String> {
    Optional<CategoryEntity> findByName(String name);
    
    List<CategoryEntity> findByVisible(Boolean visible);
    
    List<CategoryEntity> findByCategoryParent(CategoryEntity parent);
    
    List<CategoryEntity> findByCategoryParentIsNull();
    
    List<CategoryEntity> findByNameContainingIgnoreCase(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, String id);

    long countDistinctByIdIn(@NonNull Set<String> ids);

    Set<CategoryEntity> findByIdIn(@NonNull Set<String> ids);
}
