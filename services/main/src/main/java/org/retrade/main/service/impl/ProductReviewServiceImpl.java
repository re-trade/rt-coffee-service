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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private LocalDateTime lastSyncTime = LocalDateTime.now().minusDays(1);

    @Override
    public ProductReviewResponse createProductReview(CreateProductReviewRequest request) {
        if(request.getVote()<=0){
            throw new ValidationException("Vote should be greater than 0");
        }
        var customer = getCustomer();
        OrderComboEntity orderComboEntity = orderComboRepository.findById(request.getOrderId()).orElseThrow(
                () -> new ValidationException("Order combo not found")
        );
        ProductEntity productEntity = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new ValidationException("Product not found")
        );
        List<ProductReviewEntity> existingReviews = productReviewRepository.findByOrderCombo(orderComboEntity);
        if (!existingReviews.isEmpty()) {
            throw new ValidationException("Product review already exists for this order");
        }
        boolean containsProduct = orderComboEntity.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getProduct().getId().equals(productEntity.getId()));
        if (!containsProduct) {
            throw new ValidationException("Order does not contain product id: " + request.getProductId());
        }
        SellerEntity sellerEntity =productEntity.getSeller();
        ProductReviewEntity productReviewEntity = new ProductReviewEntity();
        productReviewEntity.setCustomer(customer);
        productReviewEntity.setProduct(productEntity);
        productReviewEntity.setSeller(sellerEntity);
        productReviewEntity.setVote(request.getVote());
        productReviewEntity.setContent(request.getContent());
        productReviewEntity.setStatus(true);
        productReviewEntity.setOrderCombo(orderComboEntity);
        try {
            productReviewEntity = productReviewRepository.save(productReviewEntity);
            updateRatingProduct(productEntity);
            updateRatingShop(sellerEntity);
            return maptoProductReviewResponse(productReviewEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Product review could not be saved " + e.getMessage());
        }

    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getProductReviewByProductId(String productId, QueryWrapper queryWrapper) {
        ProductEntity productEntity = productRepository.findById(productId).orElseThrow(
                () -> new ValidationException("Product not found")
        );

        return productReviewRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("product"), productEntity));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::maptoProductReviewResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductReviewResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }
    

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ProductReviewEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = productReviewRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    @Override
    public ProductReviewResponse getProductReviewDetails(String id) {
        ProductReviewEntity productReviewEntity = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found")
        );
        return maptoProductReviewResponse(productReviewEntity);
    }

    @Override
    public ProductReviewResponse updateProductReview(String id, UpdateProductReviewRequest request) {
        var customer = getCustomer();

        var productReviewEntity = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found")
        );
        if (!customer.getId().equals(productReviewEntity.getCustomer().getId())) {
            throw new ValidationException("Customer is not the same");
        }
        productReviewEntity.setVote(request.getVote());
        productReviewEntity.setContent(request.getContent());
        productReviewEntity.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            productReviewRepository.save(productReviewEntity);
            updateRatingProduct(productReviewEntity.getProduct());
            updateRatingShop(productReviewEntity.getSeller());
            return maptoProductReviewResponse(productReviewEntity);
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
    public ProductReviewResponse deleteProductReview(String id) {
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
            updateRatingProduct(productReviewEntity.getProduct());
            updateRatingShop(productReviewEntity.getSeller());
            return maptoProductReviewResponse(productReviewEntity);
        } catch (Exception e) {
            throw new ValidationException("Product review could not be delete " + e.getMessage());
        }

    }
    @Override
    public PaginationWrapper <List<ProductReviewResponse>> geAllProductReviewBySeller(QueryWrapper queryWrapper) {
        var seller = getSeller();
        List<ProductReviewEntity> entities = productReviewRepository.findBySeller(seller);
        Page<ProductReviewEntity> productReviewPage =productReviewRepository.findBySeller(seller,queryWrapper.pagination());
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
        ReplyBaseResponse reply = null;
        if (entity.getReplyContent() != null || entity.getReplyCreatedDate() != null || entity.getReplyUpdatedDate() != null) {
            reply = ReplyBaseResponse.builder()
                    .content(entity.getReplyContent())
                    .createdAt(entity.getReplyCreatedDate() != null ? entity.getReplyCreatedDate().toLocalDateTime() : null)
                    .updatedAt(entity.getReplyUpdatedDate() != null ? entity.getReplyUpdatedDate().toLocalDateTime() : null)
                    .build();
        }
        return ProductReviewResponse.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedDate().toLocalDateTime())
                .updatedAt(entity.getUpdatedDate().toLocalDateTime())
                .vote(entity.getVote())
                .content(entity.getContent())
                .author(author)
                .orderId(entity.getOrderCombo().getId())
                .status(entity.getStatus())
                .product(product)
                .reply(reply)
                .build();
    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getProductReviewBySellerId(String sellerId, QueryWrapper queryWrapper) {

        SellerEntity sellerEntity = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ValidationException("Seller not found"));

        List<ProductReviewEntity> productReviews = productReviewRepository.findBySeller(sellerEntity);
        List<ProductReviewResponse> reviewResponses = productReviews.stream()
                .map(this::maptoProductReviewResponse)
                .toList();
        return new PaginationWrapper.Builder<List<ProductReviewResponse>>()
                .setData(reviewResponses)
                .build();

    }
    @Transactional
    public void updateRatingProduct(ProductEntity productEntity) {
        var avgVote = productReviewRepository.calculateTotalRatingByProduct(productEntity);
        productEntity.setAvgVote(avgVote);
        try {
            productRepository.save(productEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Product review could not be saved " + e.getMessage());
        }

    }
    @Transactional
    public void updateRatingShop(SellerEntity sellerEntity) {

        var avgVote = productRepository.findAverageRatingBySeller(sellerEntity);
        sellerEntity.setAvgVote(avgVote);
        try {
            sellerRepository.save(sellerEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Seller review could not be saved " + e.getMessage());
        }
    }

    @Override
    public ProductReviewResponse createReplyProductReview(String id, String content) {
        ProductReviewEntity productReview = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found"));
        var seller = getSeller();
        if(!seller.equals(productReview.getSeller())) {
            throw new ValidationException("Seller is not the same");
        }
        productReview.setReplyContent(content);
        productReview.setReplyCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            productReviewRepository.save(productReview);
            return maptoProductReviewResponse(productReview);
        }catch (Exception e) {
            throw new ValidationException("Product review could not be saved " + e.getMessage());
        }
    }

    @Override
    public ProductReviewResponse updateReplyProductReview(String id, String content) {
        ProductReviewEntity productReview = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found"));
        var seller = getSeller();
        if(!seller.equals(productReview.getSeller())) {
            throw new ValidationException("Seller is not the same");
        }
        productReview.setReplyContent(content);
        productReview.setReplyUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            productReviewRepository.save(productReview);
            return maptoProductReviewResponse(productReview);
        }catch (Exception e) {
            throw new ValidationException("Product review could not be saved " + e.getMessage());
        }
    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getAllProductReviewsBySellerAndSearch(Double vote, QueryWrapper queryWrapper) {
        if (queryWrapper == null || queryWrapper.pagination() == null) {
            throw new ValidationException("QueryWrapper or pagination cannot be null");
        }
        String search = queryWrapper.search() != null && !queryWrapper.search().toString().isBlank()
                ? queryWrapper.search().toString() : null;
        SellerEntity seller = getSeller();

        if (vote != null && (vote <= 0 || vote > 5)) {
            throw new ValidationException("Vote must be between 0 and 5, exclusive of 0");
        }

        try {
            Page<ProductReviewEntity> productReviewPage = productReviewRepository
                    .findProductReviewsBySellerAndKeyword(vote, seller, search, queryWrapper.pagination());
            List<ProductReviewResponse> reviewResponses = productReviewPage.getContent()
                    .stream()
                    .map(this::maptoProductReviewResponse)
                    .toList();

            return new PaginationWrapper.Builder<List<ProductReviewResponse>>()
                    .setData(reviewResponses)
                    .setPaginationInfo(productReviewPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException("Failed to fetch product reviews: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void syncUpdatedRatings() {
        try {
            List<ProductEntity> updatedProducts = productReviewRepository.findProductsWithRecentReviews(lastSyncTime);
            for (ProductEntity product : updatedProducts) {
                Double avgRating = productReviewRepository.findAverageRatingByProduct(product);
                product.setAvgVote(avgRating != null ? avgRating : 0.0);
                productRepository.save(product);
            }

            List<SellerEntity> updatedSellers = productRepository.findSellersByProducts(updatedProducts);
            for (SellerEntity seller : updatedSellers) {
                Double avgShopRating = productRepository.findAverageRatingBySeller(seller);
                seller.setAvgVote(avgShopRating != null ? avgShopRating : 0.0);
                sellerRepository.save(seller);
            }
            lastSyncTime = LocalDateTime.now();
            System.out.println("Rating synchronization completed at " + lastSyncTime);
        } catch (Exception e) {
            System.out.println("Error during rating synchronization: " + e.getMessage());
        }
    }

}
