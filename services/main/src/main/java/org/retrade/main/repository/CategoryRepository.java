package org.retrade.main.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends BaseJpaRepository<CategoryEntity, String> {
    Optional<CategoryEntity> findByName(String name);
    
    List<CategoryEntity> findByVisible(Boolean visible);
    
    List<CategoryEntity> findByCategoryParent(CategoryEntity parent);
    
    List<CategoryEntity> findByCategoryParentIsNull();
    
    List<CategoryEntity> findByType(String type);
    
    List<CategoryEntity> findBySeller(SellerEntity seller);
    
    List<CategoryEntity> findBySellerAndVisible(SellerEntity seller, Boolean visible);
    
    List<CategoryEntity> findByVisibleAndType(Boolean visible, String type);
    
    List<CategoryEntity> findByNameContainingIgnoreCase(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, String id);
}
