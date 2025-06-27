package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.OrderComboRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.ProductReviewRepository;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.ProductReviewService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final AuthUtils authUtils;
    private final ProductRepository productRepository;
    private final OrderComboRepository orderComboRepository;
    private final SellerRepository sellerRepository;

    @Override
    public ProductReviewBaseResponse createProductReview(CreateProductReviewRequest request) {
        var customer = getCustomer();
        OrderComboEntity orderComboEntity = orderComboRepository.findById(request.getOrderId()).orElseThrow(
                () -> new ValidationException("Order combo not found")
        );
        ProductEntity productEntity = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new ValidationException("Product not found")
        );
        boolean containsProduct = orderComboEntity.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getProduct().getId().equals(productEntity.getId()));
        if (!containsProduct) {
            throw new ValidationException("Order does not contain product id: " + request.getProductId());
        }
        ProductReviewEntity productReviewEntity = new ProductReviewEntity();
        productReviewEntity.setCustomer(customer);
        productReviewEntity.setProduct(productEntity);
        productReviewEntity.setVote(request.getVote());
        productReviewEntity.setContent(request.getContent());
        productReviewEntity.setStatus(true);
        productReviewEntity.setOrder(orderComboEntity);
        try {
            productReviewEntity = productReviewRepository.save(productReviewEntity);
            return mapToProductReviewBaseResponse(productReviewEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Product review could not be saved " + e.getMessage());
        }

    }

    @Override
    public PaginationWrapper<List<ProductReviewBaseResponse>> getProductReviewByProductId(String productId, QueryWrapper queryWrapper) {
        ProductEntity productEntity = productRepository.findById(productId).orElseThrow(
                () -> new ValidationException("Product not found")
        );

        return productReviewRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("product"), productEntity));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToProductReviewBaseResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductReviewBaseResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private ProductReviewBaseResponse mapToProductReviewBaseResponse(ProductReviewEntity entity) {
        return ProductReviewBaseResponse.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedDate().toLocalDateTime())
                .updatedAt(entity.getUpdatedDate().toLocalDateTime())
                .vote(entity.getVote())
                .content(entity.getContent())
                .authorId(entity.getCustomer().getId())
                .orderId(entity.getOrder().getId())
                .status(entity.getStatus())
                .productId(entity.getProduct().getId())
                .build();
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ProductReviewEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = productReviewRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    @Override
    public ProductReviewBaseResponse getProductReviewDetails(String id) {
        var productReviewEntity = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found")
        );
        return mapToProductReviewBaseResponse(productReviewEntity);
    }

    @Override
    public ProductReviewBaseResponse updateProductReview(String id, UpdateProductReviewRequest request) {
        var customer = getCustomer();

        var productReviewEntity = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found")
        );
        if (!customer.getId().equals(productReviewEntity.getCustomer().getId())) {
            throw new ValidationException("Customer is not the same");
        }
        productReviewEntity.setVote(request.getVote());
        productReviewEntity.setContent(request.getContent());
        try {
            productReviewRepository.save(productReviewEntity);
            return mapToProductReviewBaseResponse(productReviewEntity);
        } catch (Exception e) {
            throw new ValidationException("Product review could not be saved " + e.getMessage());
        }

    }

    private CustomerEntity getCustomer() {
        var account = authUtils.getUserAccountFromAuthentication();
        return account.getCustomer();
    }

    private SellerEntity getSeller() {
        var account = authUtils.getUserAccountFromAuthentication();
        return account.getSeller();
    }

    @Override
    public ProductReviewBaseResponse deleteProductReview(String id) {
        var customer = getCustomer();
        var productReviewEntity = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found")
        );
        if (!customer.getId().equals(productReviewEntity.getCustomer().getId())) {
            throw new ValidationException("Customer is not the same");
        }
        productReviewEntity.setStatus(false);
        try {
            productReviewRepository.save(productReviewEntity);
            return mapToProductReviewBaseResponse(productReviewEntity);
        } catch (Exception e) {
            throw new ValidationException("Product review could not be delete " + e.getMessage());
        }

    }
    @Override
    public PaginationWrapper <List<ProductReviewResponse>> geAllProductReviewBySeller(QueryWrapper queryWrapper) {
        var seller = getSeller();
        Page<ProductReviewEntity> productReviewPage =productReviewRepository.findAllBySellerAndStatusTrueWithProduct(seller,queryWrapper.pagination());
        List<ProductReviewResponse> reviewResponses = productReviewPage.getContent()
                .stream()
                .map(this::maptoProductReviewResponse)
                .toList();
        return new PaginationWrapper.Builder<List<ProductReviewResponse>>()
                .setData(reviewResponses)
                .setPaginationInfo(productReviewPage)
                .build();
    }

    public ProductReviewResponse maptoProductReviewResponse(ProductReviewEntity entity) {

        var author = AuthorBaseResponse.builder()
                .authorId(entity.getCustomer().getId())
                .firstName(entity.getCustomer().getFirstName())
                .lastName(entity.getCustomer().getLastName())
                .avatarUrl(entity.getCustomer().getAvatarUrl())
                .build();
        var product = ProductBaseResponse.builder()
                .productId(entity.getProduct().getId())
                .productName(entity.getProduct().getName())
                .shortDescription(entity.getProduct().getDescription())
                .price(entity.getProduct().getCurrentPrice())
                .build();
        var reply = ReplyBaseResponse.builder()
                .content(entity.getReplyContent())
                .createdAt(entity.getReplyCreatedDate().toLocalDateTime())
                .updatedAt(entity.getReplyUpdatedDate().toLocalDateTime())
                .build();
        return ProductReviewResponse.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedDate().toLocalDateTime())
                .updatedAt(entity.getUpdatedDate().toLocalDateTime())
                .vote(entity.getVote())
                .content(entity.getContent())
                .author(author)
                .orderId(entity.getOrder().getId())
                .status(entity.getStatus())
                .product(product)
                .reply(reply)
                .build();
    }

    @Override
    public PaginationWrapper<List<ProductReviewBaseResponse>> getProductReviewBySellerId(String sellerId, QueryWrapper queryWrapper) {
//        SellerEntity sellerEntity = sellerRepository.findById(sellerId).orElseThrow(
//                () -> new ValidationException("Seller not found")
//        );
//        return productReviewRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            predicates.add(criteriaBuilder.equal(root.get("seller"), sellerEntity));
//            return getPredicate(param, root, criteriaBuilder, predicates);
//        }, (items) -> {
//            var list = items.map(this::mapToProductReviewResponse).stream().toList();
//            return new PaginationWrapper.Builder<List<ProductReviewResponse>>()
//                    .setPaginationInfo(items)
//                    .setData(list)
//                    .build();
//        });
        SellerEntity sellerEntity = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ValidationException("Seller not found"));

        List<ProductReviewEntity> productReviews = productReviewRepository.findAllBySellerWithProduct(sellerEntity);
        List<ProductReviewBaseResponse> reviewResponses = productReviews.stream()
                .map(this::mapToProductReviewBaseResponse)
                .toList();
        return new PaginationWrapper.Builder<List<ProductReviewBaseResponse>>()
                .setData(reviewResponses)
                .build();

    }

    public void updateRatingProduct(String id) {
        ProductEntity productEntity = productRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product not found")
        );
        var avgVote = productReviewRepository.calculateTotalRatingByProduct(productEntity);

//        productEntity.setRatingValue(avgVote);
//        try {
//            productRepository.save(productEntity);
//        } catch (Exception e) {
//            throw new ActionFailedException("Product review could not be saved " + e.getMessage());
//        }

    }

    public void updateRatingShop(String id) {
        var sellerEntity = sellerRepository.findById(id).orElseThrow(
                () -> new ValidationException("Seller not found")
        );
        var avgVote = productReviewRepository.calculateAverageRatingBySeller(sellerEntity);
//        var avgVoteProduct = productRepository.findAverageRatingBySeller(sellerEntity);

//        sellerEntity.setRatingValue(avgVote);
//        try {
//            sellerRepository.save(sellerEntity);
//        } catch (Exception e) {
//            throw new ActionFailedException("Seller review could not be saved " + e.getMessage());
//        }
    }


//    @Scheduled(cron = "0 0 2 * * *")
//    @Transactional
//    public void syncUpdatedRatings() {
//        try {
//            List<ProductEntity> updatedProducts = productReviewRepository.findProductsWithRecentReviews(lastSyncTime);
//            for (ProductEntity product : updatedProducts) {
//                Double avgRating = productReviewRepository.findAverageRatingByProduct(product);
//                product.setProductRating(avgRating != null ? avgRating : 0.0);
//                productRepository.save(product);
//            }
//
//            // Cập nhật shop_rating cho các seller liên quan
//            List<SellerEntity> updatedSellers = productReviewRepository.findSellersByProducts(updatedProducts);
//            for (SellerEntity seller : updatedSellers) {
//                Double avgShopRating = productReviewRepository.findAverageRatingBySeller(seller);
//                seller.setShopRating(avgShopRating != null ? avgShopRating : 0.0);
//                sellerRepository.save(seller);
//            }
//
//            lastSyncTime = LocalDateTime.now();
//            System.out.println("Rating synchronization completed at {}", lastSyncTime);
//        } catch (Exception e) {
//            System.out.println("Error during rating synchronization: {}"+ e.getMessage());
//        }
//    }

}
