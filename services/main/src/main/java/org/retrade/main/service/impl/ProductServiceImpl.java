package org.retrade.main.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.document.ProductDocument;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.request.UpdateProductRequest;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.CategoryRepository;
import org.retrade.main.repository.ProductElasticsearchRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.ProductService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository productSerachRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final AuthUtils authUtils;

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public ProductResponse createProduct(CreateProductRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("Seller profile not found");
        }

        validateCategories(request.getCategoryIds());

        var product = ProductEntity.builder()
                .name(request.getName())
                .seller(seller)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .productImages(request.getProductImages())
                .brand(request.getBrand())
                .discount(request.getDiscount())
                .model(request.getModel())
                .currentPrice(request.getCurrentPrice())
                .categories(convertCategoryIdsToEntities(request.getCategoryIds()))
                .keywords(request.getKeywords())
                .tags(request.getTags())
                .verified(false)
                .build();

        try {
            var savedProduct = productRepository.save(product);
            saveProductDocument(savedProduct);
            return mapToProductResponse(savedProduct);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to create product", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        var product = getProductEntityById(id);
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("Seller profile not found");
        }
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ValidationException("You can only update your own products");
        }
        product.setName(request.getName());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setThumbnail(request.getThumbnail());
        product.setProductImages(request.getProductImages());
        product.setBrand(request.getBrand());
        product.setDiscount(request.getDiscount());
        product.setModel(request.getModel());
        product.setCurrentPrice(request.getCurrentPrice());
        validateCategories(request.getCategoryIds());
        product.setCategories(convertCategoryIdsToEntities(request.getCategoryIds()));
        product.setKeywords(request.getKeywords());
        product.setTags(request.getTags());
        try {
            var updatedProduct = productRepository.save(product);
            saveProductDocument(updatedProduct, id);
            return mapToProductResponse(updatedProduct);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to update product", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void deleteProduct(String id) {
        var product = getProductEntityById(id);
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = sellerRepository.findByAccount(account)
                .orElseThrow(() -> new ValidationException("Seller profile not found"));
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ValidationException("You can only delete your own products");
        }
        try {
            productRepository.delete(product);
            productSerachRepository.deleteById(id);;
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to delete product", ex);
        }
    }

    @Override
    public ProductResponse getProductById(String id) {
        var product = getProductEntityById(id);
        return mapToProductResponse(product);
    }

    @Override
    public PaginationWrapper<List<ProductResponse>> getAllProducts(QueryWrapper queryWrapper) {;
        return productRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToProductResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<ProductResponse>> getProductsBySeller(String sellerId, QueryWrapper queryWrapper) {
        var seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ValidationException("Seller not found"));
        return getProductsBySeller(seller, queryWrapper);
    }

    @Override
    public PaginationWrapper<List<ProductResponse>> getMyProducts(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("Seller profile not found");
        }
       return getProductsBySeller(seller, queryWrapper);
    }

    @Override
    public List<ProductResponse> getProductsByBrand(String brand) {
        List<ProductEntity> products = productRepository.findByBrand(brand);
        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        List<ProductEntity> products = productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    public PaginationWrapper<List<ProductResponse>> searchProductByKeyword(QueryWrapper queryWrapper) {
        var search = queryWrapper.search();
        var keyword = search.get("keyword").getValue().toString();
        if (queryWrapper.search() == null  || queryWrapper.search().isEmpty()) {
            return productRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
                Predicate[] defaultPredicates = productRepository.createDefaultPredicate(criteriaBuilder, root, param);
                List<Predicate> predicates = new ArrayList<>(Arrays.asList(defaultPredicates));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }, (items) -> {
                var list = items.map(this::mapToProductResponse).stream().toList();
                return new PaginationWrapper.Builder<List<ProductResponse>>()
                        .setPaginationInfo(items)
                        .setData(list)
                        .build();
            });
        }
        var nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m.fields("name", "shortDescription", "description").query(keyword).fuzziness("AUTO")))
                .withPageable(queryWrapper.pagination())
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .build();
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
        var searchHit = searchHits.getSearchHits().stream().map(SearchHit::getId).collect(Collectors.toSet());
        return productRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("id").in(searchHit));
            if (param != null && !param.isEmpty()) {
                Predicate[] defaultPredicates = productRepository.createDefaultPredicate(criteriaBuilder, root, param);
                predicates.addAll(Arrays.asList(defaultPredicates));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, (items) -> {
            var list = items.map(this::mapToProductResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });

    }

    @Override
    public List<ProductResponse> getProductsByCategory(String categoryName) {
        List<ProductEntity> products = productRepository.findByCategoryName(categoryName);

        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    @Override
    public PaginationWrapper<List<ProductResponse>> getProductsByCategory(String categoryName, QueryWrapper queryWrapper) {
        Page<ProductEntity> productPage = productRepository.findByCategoryName(categoryName, queryWrapper.pagination());
        List<ProductResponse> productResponses = productPage.getContent()
                .stream()
                .map(this::mapToProductResponse)
                .toList();
        return new PaginationWrapper.Builder<List<ProductResponse>>()
                .setData(productResponses)
                .setPaginationInfo(productPage)
                .build();
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void verifyProduct(String id) {
        var product = getProductEntityById(id);
        product.setVerified(true);
        try {
            var result = productRepository.save(product);
            saveProductDocument(result, result.getId());
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to verify product", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void unverifyProduct(String id) {
        var product = getProductEntityById(id);
        product.setVerified(false);
        try {
            productRepository.save(product);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to unverify product", ex);
        }
    }

    private ProductEntity getProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Product not found with id: " + id));
    }

    private ProductResponse mapToProductResponse(ProductEntity product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sellerId(product.getSeller().getId())
                .sellerShopName(product.getSeller().getShopName())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .thumbnail(product.getThumbnail())
                .productImages(product.getProductImages())
                .brand(product.getBrand())
                .discount(product.getDiscount())
                .model(product.getModel())
                .currentPrice(product.getCurrentPrice())
                .categories(convertCategoryEntitiesToNames(product.getCategories()))
                .keywords(product.getKeywords())
                .tags(product.getTags())
                .verified(product.getVerified())
                .createdAt(product.getCreatedDate() != null ? product.getCreatedDate().toLocalDateTime() : null)
                .updatedAt(product.getUpdatedDate() != null ? product.getUpdatedDate().toLocalDateTime() : null)
                .build();
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = productRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void validateCategories(Set<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ValidationException("Invalid categories");
        }
        var categoryCount = categoryRepository.countDistinctByIdIn(categoryIds);
        if (categoryCount != categoryIds.size()) {
            throw new ValidationException("Invalid categories");
        }
    }
    private Set<String> convertCategoryEntitiesToNames(Set<CategoryEntity> categoryEntities) {
        if (categoryEntities == null || categoryEntities.isEmpty()) {
            return Set.of();
        }
        return categoryEntities.stream()
                .map(CategoryEntity::getName)
                .collect(Collectors.toSet());
    }
    private Set<CategoryEntity> convertCategoryIdsToEntities(Set<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ValidationException("Invalid categories");
        }
        return categoryRepository.findByIdIn(categoryIds);
    }

    private PaginationWrapper<List<ProductResponse>> getProductsBySeller (SellerEntity seller, QueryWrapper queryWrapper) {
        return productRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("seller"), seller));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToProductResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });

    }

    private void saveProductDocument(ProductEntity productEntity, String id) {
        var productDoc = productSerachRepository.findById(id).orElseThrow(() -> new ValidationException("Product not found with id: " + id));
        productDoc.setName(productEntity.getName());
        productDoc.setSellerId(productEntity.getSeller().getId());
        productDoc.setShortDescription(productEntity.getShortDescription());
        productDoc.setDescription(productEntity.getDescription());
        productDoc.setBrand(productEntity.getBrand());
        productDoc.setDiscount(productEntity.getDiscount());
        productDoc.setModel(productEntity.getModel());
        productDoc.setCurrentPrice(productEntity.getCurrentPrice());
        productDoc.setCategories(productEntity.getCategories().stream().map(item -> ProductDocument.CategoryInfo.builder()
                .id(item.getId())
                .name(item.getName())
                .type(item.getType())
                .build()).collect(Collectors.toSet()));
        productDoc.setUpdatedAt(productEntity.getUpdatedDate() != null ? productEntity.getUpdatedDate().toLocalDateTime() : null);
        productSerachRepository.save(productDoc);
    }

    private void saveProductDocument(ProductEntity productEntity) {
        var product = ProductDocument.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .sellerId(productEntity.getSeller().getId())
                .shortDescription(productEntity.getShortDescription())
                .description(productEntity.getDescription())
                .brand(productEntity.getBrand())
                .discount(productEntity.getDiscount())
                .model(productEntity.getModel())
                .currentPrice(productEntity.getCurrentPrice())
                .categories(productEntity.getCategories().stream().map(item -> ProductDocument.CategoryInfo.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .type(item.getType())
                        .build()).collect(Collectors.toSet()))
                .verified(productEntity.getVerified())
                .createdAt(productEntity.getCreatedDate() != null ? productEntity.getCreatedDate().toLocalDateTime() : null)
                .updatedAt(productEntity.getUpdatedDate() != null ? productEntity.getUpdatedDate().toLocalDateTime() : null)
                .build();
        productSerachRepository.save(product);
    }
}
