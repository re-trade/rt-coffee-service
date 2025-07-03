package org.retrade.main.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.ProductStatusEnum;
import org.retrade.main.model.document.CategoryInfoDocument;
import org.retrade.main.model.document.ProductDocument;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.request.UpdateProductRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.BrandEntity;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.CategoryRepository;
import org.retrade.main.repository.ProductElasticsearchRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.BrandRepository;
import org.retrade.main.service.ProductService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final AuthUtils authUtils;
    private final BrandRepository brandEntityRepository;

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
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .warrantyExpiryDate(request.getWarrantyExpiryDate())
                .condition(request.getCondition())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .productImages(request.getProductImages())
                .brand(convertBrandIdToEntity(request.getBrandId()))
                .model(request.getModel())
                .currentPrice(request.getCurrentPrice())
                .categories(convertCategoryIdsToEntities(request.getCategoryIds()))
                .tags(request.getTags())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatusEnum.DRAFT)
                .verified(false)
                .build();

        if (request.getStatus() != null) {
            var enumSet = Set.of(ProductStatusEnum.DRAFT, ProductStatusEnum.INIT);
            if (!enumSet.contains(request.getStatus())) {
                throw new ValidationException("Invalid product status");
            }
            product.setStatus(request.getStatus());
        }
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
        product.setBrand(convertBrandIdToEntity(request.getBrandId()));
        product.setModel(request.getModel());
        product.setCurrentPrice(request.getCurrentPrice());
        validateCategories(request.getCategoryIds());
        product.setCategories(convertCategoryIdsToEntities(request.getCategoryIds()));
        product.setTags(request.getTags());
        product.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        product.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        product.setCondition(request.getCondition());
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
            productSearchRepository.deleteById(id);
            ;
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
    public PaginationWrapper<List<ProductResponse>> getAllProducts(QueryWrapper queryWrapper) {
        ;
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
    public PaginationWrapper<List<ProductResponse>> searchProductByKeyword(QueryWrapper queryWrapper) {
        var search = queryWrapper.search();
        QueryFieldWrapper keyword = search.remove("keyword");
        return productRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) {
                Set<String> searchHitIds = getElasticSearchIds(keyword, queryWrapper.pagination());
                if (searchHitIds.isEmpty()) {
                    predicates.add(criteriaBuilder.disjunction());
                } else {
                    predicates.add(root.get("id").in(searchHitIds));
                }
            }
            if (param != null && !param.isEmpty()) {
                Predicate[] advanceFilterPredicates = getAdvanceFilterPredicate(param, root, criteriaBuilder);
                Predicate[] defaultPredicates = productRepository.createDefaultPredicate(criteriaBuilder, root, param);
                predicates.addAll(Arrays.asList(defaultPredicates));
                predicates.addAll(Arrays.asList(advanceFilterPredicates));
            }
            return predicates.isEmpty() ?
                    criteriaBuilder.conjunction() :
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, (items) -> {
            var list = items.map(this::mapToProductResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private Set<String> getElasticSearchIds(QueryFieldWrapper keyword, Pageable pagination) {
        var searchHits = queryElasticSearchByKeyword(keyword, pagination);
        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getId)
                .collect(Collectors.toSet());
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


    @Override
    public FieldAdvanceSearch filedAdvanceSearch(QueryWrapper queryWrapper) {
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");

        if (keyword == null) {
            return FieldAdvanceSearch.builder()
                    .brands(Collections.emptySet())
                    .states(Collections.emptySet())
                    .sellers(Collections.emptySet())
                    .minPrice(BigDecimal.ZERO)
                    .maxPrice(BigDecimal.ZERO)
                    .categoriesAdvanceSearch(Collections.emptyList())
                    .build();
        }

        AggregationsContainer<?> aggregations = aggregateFilterFields(keyword.getValue().toString());

        Set<String> brandIds = extractBucketKeys(aggregations, "brandIds");
        Set<String> sellerIds = extractBucketKeys(aggregations, "sellerIds");
        Set<String> categoryIds = extractNestedBucketKeys(
                aggregations,
                "categoryIds",
                "ids"
        );

        Set<String> states = extractBucketKeys(aggregations, "states");

        BigDecimal minPrice = extractMin(aggregations, "minPrice");
        BigDecimal maxPrice = extractMax(aggregations, "maxPrice");

        Set<BrandResponse> brands = brandRepository.findAllById(brandIds).stream()
                .map(b -> BrandResponse.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .imgUrl(b.getImgUrl())
                        .build())
                .collect(Collectors.toSet());

        Set<SellerFilterResponse> sellers = sellerRepository.findAllById(sellerIds).stream()
                .map(s -> SellerFilterResponse.builder()
                        .sellerId(s.getId())
                        .sellerName(s.getShopName())
                        .sellerAvatarUrl(s.getAvatarUrl())
                        .build())
                .collect(Collectors.toSet());

        List<CategoriesAdvanceSearch> categories = categoryRepository.findAllById(categoryIds).stream()
                .map(c -> new CategoriesAdvanceSearch(c.getId(), c.getName()))
                .toList();

        return FieldAdvanceSearch.builder()
                .brands(brands)
                .sellers(sellers)
                .states(states)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .categoriesAdvanceSearch(categories)
                .build();
    }

    private Set<String> extractBucketKeys(AggregationsContainer<?> aggregations, String aggName) {
        Set<String> keys = new HashSet<>();
        if (aggregations == null) return keys;

        @SuppressWarnings("unchecked")
        List<ElasticsearchAggregation> aggList = (List<ElasticsearchAggregation>) aggregations.aggregations();
        for (ElasticsearchAggregation aggregation : aggList) {
            if (aggName.equals(aggregation.aggregation().getName())) {
                Aggregate agg = aggregation.aggregation().getAggregate();
                if (agg.isSterms()) {
                    for (StringTermsBucket bucket : agg.sterms().buckets().array()) {
                        keys.add(bucket.key().stringValue());
                    }
                }
                break;
            }
        }
        return keys;
    }


    private Set<String> extractNestedBucketKeys(AggregationsContainer<?> aggregations, String nestedAggName, String termsAggName) {
        Set<String> keys = new HashSet<>();
        if (aggregations == null) return keys;

        @SuppressWarnings("unchecked")
        List<ElasticsearchAggregation> aggList = (List<ElasticsearchAggregation>) aggregations.aggregations();
        for (ElasticsearchAggregation aggregation : aggList) {
            if (nestedAggName.equals(aggregation.aggregation().getName())) {
                Aggregate nested = aggregation.aggregation().getAggregate();
                if (nested.isNested()) {
                    Aggregate termsAgg = nested.nested().aggregations().get(termsAggName);
                    if (termsAgg != null && termsAgg.isSterms()) {
                        for (StringTermsBucket bucket : termsAgg.sterms().buckets().array()) {
                            keys.add(bucket.key().stringValue());
                        }
                    }
                }
                break;
            }
        }
        return keys;
    }



    private BigDecimal extractMin(AggregationsContainer<?> aggregations, String aggName) {
        if (aggregations != null) {
            @SuppressWarnings("unchecked")
            var aggList = (List<ElasticsearchAggregation>) aggregations.aggregations();
            Map<String, Aggregate> aggMap = new HashMap<>();
            aggList.forEach(item -> {
                aggMap.put(item.aggregation().getName(), item.aggregation().getAggregate());
            });

            if (aggMap.containsKey(aggName)) {
                Aggregate agg = aggMap.get(aggName);
                if (agg.isMin()) {
                    double value = agg.min().value();
                    return BigDecimal.valueOf(value);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal extractMax(AggregationsContainer<?> aggregations, String aggName) {
        if (aggregations != null) {
            @SuppressWarnings("unchecked")
            var aggList = (List<ElasticsearchAggregation>) aggregations.aggregations();
            Map<String, Aggregate> aggMap = new HashMap<>();
            aggList.forEach(item -> {
                aggMap.put(item.aggregation().getName(), item.aggregation().getAggregate());
            });

            if (aggMap.containsKey(aggName)) {
                Aggregate agg = aggMap.get(aggName);
                if (agg.isMax()) {
                    double value = agg.max().value();
                    return BigDecimal.valueOf(value);
                }
            }
        }

        return BigDecimal.ZERO;
    }


    private ProductEntity getProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Product not found with id: " + id));
    }

    private ProductResponse mapToProductResponse(ProductEntity product) {
        var brand = product.getBrand();
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sellerId(product.getSeller().getId())
                .sellerShopName(product.getSeller().getShopName())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .thumbnail(product.getThumbnail())
                .productImages(product.getProductImages())
                .brand(brand != null ? brand.getName() : "N/A")
                .brandId(brand != null ? brand.getId() : null)
                .quantity(product.getQuantity())
                .warrantyExpiryDate(product.getWarrantyExpiryDate())
                .condition(product.getCondition())
                .status(product.getStatus())
                .model(product.getModel())
                .currentPrice(product.getCurrentPrice())
                .categories(convertCategoryEntitiesToNames(product.getCategories()))
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

    private Predicate[] getAdvanceFilterPredicate(Map<String, QueryFieldWrapper> param, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder) {
        if (param == null || param.isEmpty()) {
            return new Predicate[0];
        }
        Set<Predicate> predicates = new HashSet<>();
        addCategoryPredicate(param.remove("categoryId"), root, predicates);
        addBrandPredicate(param.remove("brand"), root, criteriaBuilder, predicates);
        addSellerPredicate(param.remove("seller"), root, criteriaBuilder, predicates);
        addStatePredicate(param.remove("state"), root, criteriaBuilder, predicates);
        return predicates.toArray(new Predicate[0]);
    }

    private void addCategoryPredicate(QueryFieldWrapper categoryIds, Root<ProductEntity> root, Set<Predicate> predicates) {
        if (categoryIds == null) return;

        Set<String> categoryIdList = extractStringValues(categoryIds);
        if (!categoryIdList.isEmpty()) {
            var categoryJoin = root.join("categories");
            predicates.add(categoryJoin.get("id").in(categoryIdList));
        }
    }

    private void addBrandPredicate(QueryFieldWrapper brand, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder, Set<Predicate> predicates) {
        if (brand == null) return;
        Set<String> brandNames = extractStringValues(brand);
        if (!brandNames.isEmpty()) {
            predicates.add(root.get("brand").get("id").in(brandNames));
        }
    }

    private void addSellerPredicate(QueryFieldWrapper seller, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder, Set<Predicate> predicates) {
        if (seller == null) return;
        Set<String> sellerIds = extractStringValues(seller);
        if (!sellerIds.isEmpty()) {
            predicates.add(root.get("seller").get("id").in(sellerIds));
        }
    }

    private void addStatePredicate(QueryFieldWrapper state, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder, Set<Predicate> predicates) {
        if (state == null) return;
        Set<String> states = extractStringValues(state);
        if (!states.isEmpty()) {
            predicates.add(root.get("seller").get("state").in(states));
        }
    }


    private Set<String> extractStringValues(QueryFieldWrapper wrapper) {
        if (wrapper == null) return Set.of();
        return switch (wrapper.getOperator()) {
            case EQ -> Set.of(wrapper.getValue().toString());
            case IN -> {
                var value = wrapper.getValue();
                if (value instanceof Collection<?> collection) {
                    yield collection.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.toSet());
                }
                yield Set.of();
            }
            default -> Set.of();
        };
    }

    private SearchHits<ProductDocument> queryElasticSearchByKeyword(QueryFieldWrapper keyword, Pageable pageable) {
        var nativeQuery = NativeQuery.builder()
                .withQuery(elasticSearchKeywordQueryBuild(keyword.getValue().toString()))
                .withPageable(pageable)
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .build();
        return elasticsearchOperations.search(nativeQuery, ProductDocument.class);
    }

    private AggregationsContainer<?> aggregateFilterFields(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(elasticSearchKeywordQueryBuild(keyword))

                .withAggregation("brandIds", Aggregation.of(a -> a
                        .terms(t -> t.field("brandId").size(50))))

                .withAggregation("sellerIds", Aggregation.of(a -> a
                        .terms(t -> t.field("sellerId").size(50))))

                .withAggregation("categoryIds", Aggregation.of(a -> a
                        .nested(n -> n.path("categories"))
                        .aggregations("ids", a2 -> a2
                                .terms(t -> t.field("categories.id").size(50))
                        )
                ))
                .withAggregation("states", Aggregation.of(a -> a
                        .terms(t -> t.field("state.keyword").size(50))))
                .withAggregation("minPrice", Aggregation.of(a -> a
                        .min(m -> m.field("currentPrice"))))
                .withAggregation("maxPrice", Aggregation.of(a -> a
                        .max(m -> m.field("currentPrice"))))
                .withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{"*"}))
                .build();

        return elasticsearchOperations.search(query, ProductDocument.class).getAggregations();
    }




    private Query elasticSearchKeywordQueryBuild(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.multiMatch(m -> m
                        .fields("name^3", "shortDescription^2", "description^1")
                        .query(keyword)
                        .type(TextQueryType.BestFields)
                        .fuzziness("1")
                        .prefixLength(2)
                ))
                .should(s -> s.multiMatch(m -> m
                        .fields("addressLine", "state", "district", "ward")
                        .query(keyword)
                        .type(TextQueryType.BestFields)
                        .fuzziness("1")
                ))
                .should(s -> s.multiMatch(m -> m
                        .fields("name^5", "shortDescription^3")
                        .query(keyword)
                        .type(TextQueryType.Phrase)
                ))
                .should(s -> s.nested(n -> n
                        .path("categories")
                        .query(nq -> nq
                                .match(m -> m.field("categories.name").query(keyword))
                        )
                ))
                .minimumShouldMatch("1")
        ));
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

    private BrandEntity convertBrandIdToEntity(String id) {
        return brandEntityRepository.findById(id).orElseThrow(() -> new ValidationException("Brand not found with id: " + id));
    }

    private PaginationWrapper<List<ProductResponse>> getProductsBySeller(SellerEntity seller, QueryWrapper queryWrapper) {
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
        var productDoc = productSearchRepository.findById(id).orElseThrow(() -> new ValidationException("Product not found with id: " + id));
        productDoc.setName(productEntity.getName());
        productDoc.setSellerId(productEntity.getSeller().getId());
        productDoc.setShortDescription(productEntity.getShortDescription());
        productDoc.setDescription(productEntity.getDescription());
        productDoc.setBrand(productEntity.getBrand().getName());
        productDoc.setModel(productEntity.getModel());
        productDoc.setCurrentPrice(productEntity.getCurrentPrice());
        productDoc.setCategories(productEntity.getCategories().stream().map(item -> CategoryInfoDocument.builder()
                .id(item.getId())
                .name(item.getName())
                .build()).toList());
        productDoc.setUpdatedAt(productEntity.getUpdatedDate() != null ? productEntity.getUpdatedDate() : null);
        productSearchRepository.save(productDoc);
    }

    private void saveProductDocument(ProductEntity productEntity) {
        var product = ProductDocument.wrapEntityToDocument(productEntity);
        productSearchRepository.save(product);
    }
}
