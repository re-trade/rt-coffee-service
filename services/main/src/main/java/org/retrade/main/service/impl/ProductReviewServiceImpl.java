package org.retrade.main.service.impl;

import jakarta.persistence.criteria.*;
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
import org.retrade.main.repository.jpa.OrderComboRepository;
import org.retrade.main.repository.jpa.ProductRepository;
import org.retrade.main.repository.jpa.ProductReviewRepository;
import org.retrade.main.repository.jpa.SellerRepository;
import org.retrade.main.service.ProductReviewService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        if (request.getVote() <= 0) {
            throw new ValidationException("Vote should be greater than 0");
        }
        var customer = getCustomer();
        OrderComboEntity orderComboEntity = orderComboRepository.findById(request.getOrderId()).orElseThrow(
                () -> new ValidationException("Order combo not found")
        );
        var checkMyOrder = orderComboRepository.existsByOrderDestination_Order_CustomerAndId(customer, orderComboEntity.getId());
        if (!checkMyOrder) {
            throw new ValidationException("This not your order");
        }
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
        SellerEntity sellerEntity = productEntity.getSeller();
        ProductReviewEntity productReviewEntity = new ProductReviewEntity();
        productReviewEntity.setCustomer(customer);
        productReviewEntity.setProduct(productEntity);
        productReviewEntity.setSeller(sellerEntity);
        productReviewEntity.setVote(request.getVote());
        productReviewEntity.setContent(request.getContent());
        productReviewEntity.setImageReview(request.getImageReview());
        productReviewEntity.setStatus(true);
        productReviewEntity.setHelpful(0);
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
    public PaginationWrapper<List<ProductReviewResponse>> geAllProductReviewBySeller(QueryWrapper queryWrapper) {
        var seller = getSeller();
        List<ProductReviewEntity> entities = productReviewRepository.findBySeller(seller);
        Page<ProductReviewEntity> productReviewPage = productReviewRepository.findBySeller(seller, queryWrapper.pagination());
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
                .name(entity.getCustomer().getFirstName() + " " + entity.getCustomer().getLastName())
                .avatarUrl(entity.getCustomer().getAvatarUrl())
                .build();
        var product = ProductBaseResponse.builder()
                .productId(entity.getProduct().getId())
                .productName(entity.getProduct().getName())
                .thumbnailUrl(entity.getProduct().getThumbnail())
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
        if (!seller.equals(productReview.getSeller())) {
            throw new ValidationException("Seller is not the same");
        }
        productReview.setReplyContent(content);
        productReview.setReplyCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            productReviewRepository.save(productReview);
            return maptoProductReviewResponse(productReview);
        } catch (Exception e) {
            throw new ValidationException("Product review could not be saved " + e.getMessage());
        }
    }

    @Override
    public ProductReviewResponse updateReplyProductReview(String id, String content) {
        ProductReviewEntity productReview = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Product review not found"));
        var seller = getSeller();
        if (!seller.equals(productReview.getSeller())) {
            throw new ValidationException("Seller is not the same");
        }
        productReview.setReplyContent(content);
        productReview.setReplyUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            productReviewRepository.save(productReview);
            return maptoProductReviewResponse(productReview);
        } catch (Exception e) {
            throw new ValidationException("Product review could not be saved " + e.getMessage());
        }
    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getAllProductReviewsBySellerAndSearch(Double vote,String isReply, QueryWrapper queryWrapper) {
        if (queryWrapper == null || queryWrapper.pagination() == null) {
            throw new ValidationException("QueryWrapper or pagination cannot be null");
        }

        SellerEntity seller = getSeller();
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");

        if (vote != null && (vote <= 0 || vote > 5)) {
            throw new ValidationException("Vote must be between 0 and 5, exclusive of 0");
        }

        return productReviewRepository.query(queryWrapper, (param) -> (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("seller"), seller));
            if ("NO_REPLY".equals(isReply)) {
                predicates.add(cb.isNull(root.get("replyContent")));
            } else if ("REPLY".equals(isReply)) {
                predicates.add(cb.and(
                        cb.isNotNull(root.get("replyContent")),
                        cb.notEqual(cb.trim(cb.literal(' '), root.get("replyContent")), "")
                ));
            }
            if (keyword != null && !keyword.getValue().toString().trim().isEmpty()) {
                String searchPattern = "%" + keyword.getValue().toString().toLowerCase() + "%";
                Join<ProductReviewEntity, ProductEntity> joinProduct = root.join("product", JoinType.LEFT);
                Join<ProductReviewEntity, CustomerEntity> joinCustomer = root.join("customer", JoinType.LEFT);

                Expression<String> fullNameExpression = cb.concat(
                        cb.lower(joinCustomer.get("firstName")),
                        cb.concat(" ", cb.lower(joinCustomer.get("lastName")))
                );

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("content")), searchPattern),
                        cb.like(cb.lower(root.get("replyContent")), searchPattern),
                        cb.like(fullNameExpression, searchPattern),
                        cb.like(cb.lower(joinProduct.get("name")), searchPattern)
                ));
            }

            // 🔍 Lọc theo vote nếu có
            if (vote != null) {
                predicates.add(cb.equal(root.get("vote"), vote));
            }

            return getProductReviewPredicate(param, root, cb, predicates);
        }, (items) -> {
            var list = items.map(this::maptoProductReviewResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductReviewResponse>>()
                    .setData(list)
                    .setPaginationInfo(items)
                    .build();
        });
    }


    private Predicate getProductReviewPredicate(Map<String, QueryFieldWrapper> param, Root<ProductReviewEntity> root, CriteriaBuilder cb, List<Predicate> predicates) {
        if (param != null || !param.isEmpty()) {
            Predicate[] predicateArray = productRepository.createDefaultPredicate(cb, root, param);
            predicates.addAll(Arrays.asList(predicateArray));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    @Override
    public ReviewStatsResponse getStatsSeller() {
        var seller = getSeller();
        long totalReviews = productReviewRepository.countTotalReviews(seller);
        long repliedReviews = productReviewRepository.countRepliedReviews(seller);
        List<Object[]> dist = productReviewRepository.getRatingDistribution(seller);
        Map<Integer, Long> voteCountMap = dist.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> ((Number) row[1]).longValue()
                ));
        List<RatingDistribution> distribution = IntStream.rangeClosed(1, 5)
                .mapToObj(vote -> {
                    long count = voteCountMap.getOrDefault(vote, 0L);
                    double percentage = totalReviews > 0
                            ? Math.round((count * 1000.0) / totalReviews) / 10.0
                            : 0.0;
                    return new RatingDistribution(vote, count, percentage);
                })
                .sorted(Comparator.comparingInt(RatingDistribution::getVote).reversed()) // để vote từ 5 → 1
                .toList();

        var averageRating = seller.getAvgVote();
        if (averageRating == null) {
            averageRating = 0.0;
        } else {
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }
        long positiveReviews = distribution.stream()
                .filter(d -> d.getVote() == 4 || d.getVote() == 5)
                .mapToLong(RatingDistribution::getCount)
                .sum();
        var replyRate = 0.0;
        var avgPositiveReviews = 0.0;
        if (totalReviews > 0) {
            replyRate = Math.round(((double) repliedReviews / totalReviews) * 1000.0) / 10.0;
            avgPositiveReviews = Math.round(((double) positiveReviews / totalReviews) * 1000.0) / 10.0;
        }


        return ReviewStatsResponse.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .repliedReviews(repliedReviews)
                .replyRate(replyRate)
                .totalPositiveReviews(positiveReviews)
                .averagePositiveReviews(avgPositiveReviews)
                .ratingDistribution(distribution)
                .build();

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
