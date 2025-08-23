package org.retrade.main.service.impl;

import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.service.ProductReviewService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
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
    private final OrderItemRepository orderItemRepository;

    @Override
    public ProductReviewResponse createProductReview(CreateProductReviewRequest request) {
        if (request.getVote() <= 0) {
            throw new ValidationException("Số sao phải lớn hơn 0");
        }
        var customer = getCustomer();
        OrderComboEntity orderComboEntity = orderComboRepository.findById(request.getOrderId()).orElseThrow(
                () -> new ValidationException("Không tìm thấy đơn hàng")
        );
        var checkMyOrder = orderComboRepository.existsByOrderDestination_Order_CustomerAndId(customer, orderComboEntity.getId());
        if (!checkMyOrder) {
            throw new ValidationException("Đây không phải đơn hàng của bạn");
        }
        ProductEntity productEntity = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new ValidationException("Không tìm thấy sản phẩm")
        );
        List<ProductReviewEntity> existingReviews = productReviewRepository.findByOrderCombo(orderComboEntity);
        if (!existingReviews.isEmpty()) {
            throw new ValidationException("Đơn hàng này đã có đánh giá sản phẩm");
        }
        boolean containsProduct = orderComboEntity.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getProduct().getId().equals(productEntity.getId()));
        if (!containsProduct) {
            throw new ValidationException("Đơn hàng không chứa sản phẩm có ID: " + request.getProductId());
        }
        SellerEntity sellerEntity = productEntity.getSeller();
        ProductReviewEntity productReviewEntity = new ProductReviewEntity();
        productReviewEntity.setOrderCombo(orderComboEntity);
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
            throw new ActionFailedException("Không thể lưu đánh giá sản phẩm " + e.getMessage());
        }

    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getProductReviewByProductId(String productId, QueryWrapper queryWrapper) {
        ProductEntity productEntity = productRepository.findById(productId).orElseThrow(
                () -> new ValidationException("Không tìm thấy sản phẩm")
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
                () -> new ValidationException("Không tìm thấy đánh giá sản phẩm")
        );
        return maptoProductReviewResponse(productReviewEntity);
    }

    @Override
    public ProductReviewResponse updateProductReview(String id, UpdateProductReviewRequest request) {
        var customer = getCustomer();

        var productReviewEntity = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Không tìm thấy đánh giá sản phẩm")
        );
        if (!customer.getId().equals(productReviewEntity.getCustomer().getId())) {
            throw new ValidationException("Khách hàng không khớp");
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
            throw new ValidationException("Không thể lưu đánh giá sản phẩm " + e.getMessage());
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
                () -> new ValidationException("Không tìm thấy đánh giá sản phẩm")
        );
        if (!customer.getId().equals(productReviewEntity.getCustomer().getId())) {
            throw new ValidationException("Khách hàng không khớp");
        }
        productReviewEntity.setStatus(false);
        try {
            productReviewRepository.save(productReviewEntity);
            updateRatingProduct(productReviewEntity.getProduct());
            updateRatingShop(productReviewEntity.getSeller());
            return maptoProductReviewResponse(productReviewEntity);
        } catch (Exception e) {
            throw new ValidationException("Không thể xóa đánh giá sản phẩm " + e.getMessage());
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
                .images(entity.getImageReview())
                .author(author)
                .orderId(entity.getOrderCombo().getId())
                .status(entity.getStatus())
                .product(product)
                .reply(reply)
                .build();
    }

    @Override
    public Long totalReviewByProductId(String productId) {
        ProductEntity productEntity = productRepository.findById(productId).orElseThrow(
                () -> new ValidationException("Không tìm thấy sản phẩm")
        );
        return productReviewRepository.countByProductAndStatusTrue(productEntity);
    }

    @Override
    public PaginationWrapper<List<ProductOrderNoReview>> getAllProductNoReviewByCustomer(QueryWrapper queryWrapper) {
        var customer = getCustomer();
        if (customer == null) {
            throw new ValidationException("Không tìm thấy khách hàng");
        }

        Page<OrderItemEntity> pageResult = orderItemRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            var orderJoin = root.join("order", JoinType.INNER);
            var customerJoin = orderJoin.join("customer", JoinType.INNER);
            var orderComboJoin = root.join("orderCombo", JoinType.INNER);
            var orderStatusJoin = orderComboJoin.join("orderStatus", JoinType.INNER);
            var productJoin = root.join("product", JoinType.INNER);

            predicates.add(criteriaBuilder.equal(customerJoin.get("id"), customer.getId()));
            predicates.add(criteriaBuilder.equal(orderStatusJoin.get("code"), OrderStatusCodes.COMPLETED));

            // Subquery kiểm tra xem có đánh giá nào cho cặp product và orderCombo không
            Subquery<ProductReviewEntity> reviewSubquery = query.subquery(ProductReviewEntity.class);
            Root<ProductReviewEntity> reviewRoot = reviewSubquery.from(ProductReviewEntity.class);
            reviewSubquery.select(reviewRoot);
            reviewSubquery.where(
                    criteriaBuilder.equal(reviewRoot.get("product"), productJoin),
                    criteriaBuilder.equal(reviewRoot.get("customer").get("id"), customer.getId()),
                    criteriaBuilder.equal(reviewRoot.get("orderCombo"), orderComboJoin) // Sử dụng orderCombo thay vì orderItem
            );
            predicates.add(criteriaBuilder.not(criteriaBuilder.exists(reviewSubquery)));

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        List<ProductOrderNoReview> productOrderNoReviews = pageResult.getContent().stream()
                .map(this::convertToProductOrderNoReview)
                .collect(Collectors.toList());

        return new PaginationWrapper.Builder<List<ProductOrderNoReview>>()
                .setData(productOrderNoReviews)
                .setPaginationInfo(pageResult)
                .build();
    }
    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getAllProductReviewByCustomer(QueryWrapper queryWrapper) {
        var customer = getCustomer();
        if (customer == null) {
            throw new ValidationException("Không tìm thấy khách hàng");
        }
        var reviews = productReviewRepository.findProductReviewsByCustomerAndStatusTrue(customer,queryWrapper.pagination());
        List<ProductReviewResponse> productReviewResponses = reviews.getContent()
                .stream()
                .map(this::maptoProductReviewResponse)
                .toList();
        return new PaginationWrapper.Builder<List<ProductReviewResponse>>()
                .setPaginationInfo(reviews)
                .setData(productReviewResponses)
                .build();
    }

    // Phương thức chuyển đổi từ OrderItemEntity sang ProductOrderNoReview
    private ProductOrderNoReview convertToProductOrderNoReview(OrderItemEntity entity) {
        ProductOrderNoReview result = new ProductOrderNoReview();
        result.setProduct(mapToProductResponse(entity.getProduct()));
        result.setOrderDate(entity.getCreatedDate().toLocalDateTime());
        result.setOrderId(entity.getOrder().getId());
        result.setOrderComboId(entity.getOrderCombo().getId());
        return result;
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
                .categories(covertCategoryEntitiesToCategories(product.getCategories()))
                .tags(product.getTags())
                .verified(product.getVerified())
                .createdAt(product.getCreatedDate() != null ? product.getCreatedDate().toLocalDateTime() : null)
                .updatedAt(product.getUpdatedDate() != null ? product.getUpdatedDate().toLocalDateTime() : null)
                .avgVote(Optional.ofNullable(product.getAvgVote()).orElse(0.0))
                .build();
    }

    private List<CategoryBaseResponse> covertCategoryEntitiesToCategories(Set<CategoryEntity> categoryEntities) {
        if (categoryEntities == null || categoryEntities.isEmpty()) {
            return Collections.emptyList();
        }

        return categoryEntities.stream()
                .map(cat -> new CategoryBaseResponse(cat.getId(), cat.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getProductReviewBySellerId(String sellerId, QueryWrapper queryWrapper) {

        SellerEntity sellerEntity = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ValidationException("SKhông tìm thấy ngời bán"));

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
            throw new ActionFailedException("Không thể lưu đánh giá sản phẩm " + e.getMessage());
        }

    }

    @Transactional
    public void updateRatingShop(SellerEntity sellerEntity) {

        var avgVote = productRepository.findAverageRatingBySeller(sellerEntity);
        sellerEntity.setAvgVote(avgVote);
        try {
            sellerRepository.save(sellerEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Không thể lưu đánh giá người bán " + e.getMessage());
        }
    }

    @Override
    public ProductReviewResponse createReplyProductReview(String id, String content) {
        ProductReviewEntity productReview = productReviewRepository.findById(id).orElseThrow(
                () -> new ValidationException("Không tìm thấy đánh giá sản phẩm"));
        var seller = getSeller();
        if (!seller.equals(productReview.getSeller())) {
            throw new ValidationException("Người bán không khớp");
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
                () -> new ValidationException("Không thể lưu đánh giá sản phẩm"));
        var seller = getSeller();
        if (!seller.equals(productReview.getSeller())) {
            throw new ValidationException("Người bán không khớp");
        }
        productReview.setReplyContent(content);
        productReview.setReplyUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            productReviewRepository.save(productReview);
            return maptoProductReviewResponse(productReview);
        } catch (Exception e) {
            throw new ValidationException("Không thể lưu đánh giá sản phẩm " + e.getMessage());
        }
    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getAllProductReviewsBySellerAndSearch(Double vote,String isReply, QueryWrapper queryWrapper) {
        if (queryWrapper == null || queryWrapper.pagination() == null) {
            throw new ValidationException("Từ khóa hoặc phân trang không được để trống");
        }

        SellerEntity seller = getSeller();
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");

        if (vote != null && (vote <= 0 || vote > 5)) {
            throw new ValidationException("Số sao phải nằm trong khoảng từ 1 đến 5");
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

}
