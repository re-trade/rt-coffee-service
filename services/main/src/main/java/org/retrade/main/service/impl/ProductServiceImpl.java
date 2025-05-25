package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.request.UpdateProductRequest;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.ProductService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final AuthUtils authUtils;

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public ProductResponse createProduct(CreateProductRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = sellerRepository.findByAccount(account)
                .orElseThrow(() -> new ValidationException("Seller profile not found"));

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
                .categories(request.getCategories())
                .keywords(request.getKeywords())
                .tags(request.getTags())
                .verified(false)
                .build();

        try {
            var savedProduct = productRepository.save(product);
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
        var seller = sellerRepository.findByAccount(account)
                .orElseThrow(() -> new ValidationException("Seller profile not found"));
        
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ValidationException("You can only update your own products");
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getShortDescription() != null) {
            product.setShortDescription(request.getShortDescription());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getThumbnail() != null) {
            product.setThumbnail(request.getThumbnail());
        }
        if (request.getProductImages() != null) {
            product.setProductImages(request.getProductImages());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getDiscount() != null) {
            product.setDiscount(request.getDiscount());
        }
        if (request.getModel() != null) {
            product.setModel(request.getModel());
        }
        if (request.getCurrentPrice() != null) {
            product.setCurrentPrice(request.getCurrentPrice());
        }
        if (request.getCategories() != null) {
            product.setCategories(request.getCategories());
        }
        if (request.getKeywords() != null) {
            product.setKeywords(request.getKeywords());
        }
        if (request.getTags() != null) {
            product.setTags(request.getTags());
        }

        try {
            var updatedProduct = productRepository.save(product);
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

    @Override
    public PaginationWrapper<List<ProductResponse>> getMyProducts(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = sellerRepository.findByAccount(account)
                .orElseThrow(() -> new ValidationException("Seller profile not found"));

        Page<ProductEntity> productPage = productRepository.findBySeller(seller, queryWrapper.pagination());

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

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void verifyProduct(String id) {
        var product = getProductEntityById(id);
        product.setVerified(true);
        try {
            productRepository.save(product);
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
                .categories(product.getCategories())
                .keywords(product.getKeywords())
                .tags(product.getTags())
                .verified(product.getVerified())
                .createdAt(product.getCreatedDate().toLocalDateTime())
                .updatedAt(product.getUpdatedDate().toLocalDateTime())
                .build();
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = productRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
