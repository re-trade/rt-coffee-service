package org.retrade.voucher.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.proto.product.ProductInfo;
import org.retrade.voucher.client.ProductGrpcClient;
import org.retrade.voucher.model.constant.VoucherTypeEnum;
import org.retrade.voucher.model.dto.request.CreateProductAwareVoucherRequest;
import org.retrade.voucher.model.dto.response.ProductAwareVoucherResponse;
import org.retrade.voucher.model.dto.response.ProductInfoResponse;
import org.retrade.voucher.model.dto.response.ProductSimpleResponse;
import org.retrade.voucher.model.dto.response.VoucherSimpleResponse;
import org.retrade.voucher.model.entity.VoucherCategoryRestrictionEntity;
import org.retrade.voucher.model.entity.VoucherEntity;
import org.retrade.voucher.model.entity.VoucherRestrictionEntity;
import org.retrade.voucher.model.entity.VoucherSellerRestrictionEntity;
import org.retrade.voucher.repository.VoucherCategoryRestrictionRepository;
import org.retrade.voucher.repository.VoucherRepository;
import org.retrade.voucher.repository.VoucherRestrictionRepository;
import org.retrade.voucher.repository.VoucherSellerRestrictionRepository;
import org.retrade.voucher.service.ProductAwareVoucherService;
import org.retrade.voucher.service.VoucherService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAwareVoucherServiceImpl implements ProductAwareVoucherService {
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;
    private final VoucherRestrictionRepository voucherRestrictionRepository;
    private final VoucherCategoryRestrictionRepository voucherCategoryRestrictionRepository;
    private final VoucherSellerRestrictionRepository voucherSellerRestrictionRepository;
    private final ProductGrpcClient productGrpcClient;

    @Override
    @Transactional
    public ProductAwareVoucherResponse createProductAwareVoucher(CreateProductAwareVoucherRequest request) {
        if (request.getValidateProducts() && request.getProductRestrictions() != null && !request.getProductRestrictions().isEmpty()) {
            var validationResponse = productGrpcClient.validateProducts(request.getProductRestrictions());
            if (!validationResponse.getInvalidProductIdsList().isEmpty()) {
                throw new ValidationException("Invalid product IDs: " + validationResponse.getInvalidProductIdsList());
            }
        }

        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new ValidationException("Voucher with code " + request.getCode() + " already exists");
        }

        VoucherEntity voucherEntity = createVoucherEntity(request);
        VoucherEntity savedVoucher = voucherRepository.save(voucherEntity);

        if (request.getProductRestrictions() != null && !request.getProductRestrictions().isEmpty()) {
            saveProductRestrictions(savedVoucher, request.getProductRestrictions());
        }

        if (request.getCategoryRestrictions() != null && !request.getCategoryRestrictions().isEmpty()) {
            saveCategoryRestrictions(savedVoucher, request.getCategoryRestrictions());
        }

        if (request.getSellerRestrictions() != null && !request.getSellerRestrictions().isEmpty()) {
            saveSellerRestrictions(savedVoucher, request.getSellerRestrictions());
        }

        return mapToProductAwareVoucherResponse(savedVoucher);
    }

    @Override
    public ProductAwareVoucherResponse getProductAwareVoucherById(String id) {
        VoucherEntity voucher = voucherService.getVoucherEntityById(id);
        return mapToProductAwareVoucherResponse(voucher);
    }

    @Override
    public ProductAwareVoucherResponse getProductAwareVoucherByCode(String code) {
        VoucherEntity voucher = voucherService.getVoucherEntityByCode(code);
        return mapToProductAwareVoucherResponse(voucher);
    }

    @Override
    public PaginationWrapper<List<ProductAwareVoucherResponse>> getProductAwareVouchers(QueryWrapper queryWrapper) {
        Page<VoucherEntity> voucherPage = voucherRepository.queryAny(queryWrapper, queryWrapper.pagination());
        
        List<ProductAwareVoucherResponse> voucherResponses = voucherPage.getContent().stream()
                .map(this::mapToProductAwareVoucherResponse)
                .collect(Collectors.toList());
        
        return new PaginationWrapper.Builder<List<ProductAwareVoucherResponse>>()
                .setData(voucherResponses)
                .setPaginationInfo(voucherPage)
                .build();
    }

    @Override
    public List<ProductAwareVoucherResponse> getVouchersForProduct(String productId) {
        ProductInfo product = productGrpcClient.getProduct(productId);
        if (product == null) {
            throw new ValidationException("Product not found: " + productId);
        }

        List<VoucherRestrictionEntity> productRestrictions = voucherRestrictionRepository.findByProductId(productId);
        List<VoucherEntity> applicableVouchers = productRestrictions.stream()
                .map(VoucherRestrictionEntity::getVoucher).collect(Collectors.toList());

        for (String category : product.getCategoriesList()) {
            List<VoucherCategoryRestrictionEntity> categoryRestrictions = 
                    voucherCategoryRestrictionRepository.findByCategory(category);
            applicableVouchers.addAll(categoryRestrictions.stream()
                    .map(VoucherCategoryRestrictionEntity::getVoucher)
                    .toList());
        }

        List<VoucherSellerRestrictionEntity> sellerRestrictions = 
                voucherSellerRestrictionRepository.findBySellerId(product.getSellerId());
        applicableVouchers.addAll(sellerRestrictions.stream()
                .map(VoucherSellerRestrictionEntity::getVoucher)
                .toList());

        return applicableVouchers.stream()
                .distinct()
                .filter(VoucherEntity::getActivated)
                .map(this::mapToProductAwareVoucherResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductAwareVoucherResponse> getVouchersForCategory(String category) {
        List<VoucherCategoryRestrictionEntity> categoryRestrictions = 
                voucherCategoryRestrictionRepository.findByCategory(category);
        
        return categoryRestrictions.stream()
                .map(VoucherCategoryRestrictionEntity::getVoucher)
                .filter(VoucherEntity::getActivated)
                .map(this::mapToProductAwareVoucherResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductAwareVoucherResponse> getVouchersForSeller(String sellerId) {
        List<VoucherSellerRestrictionEntity> sellerRestrictions = 
                voucherSellerRestrictionRepository.findBySellerId(sellerId);
        
        return sellerRestrictions.stream()
                .map(VoucherSellerRestrictionEntity::getVoucher)
                .filter(VoucherEntity::getActivated)
                .map(this::mapToProductAwareVoucherResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductInfoResponse> getApplicableProducts(String voucherCode) {
        VoucherEntity voucher = voucherService.getVoucherEntityByCode(voucherCode);
        List<ProductInfo> applicableProducts = new ArrayList<>();

        List<String> productIds = getProductRestrictions(voucher);
        if (!productIds.isEmpty()) {
            applicableProducts.addAll(productGrpcClient.getProducts(productIds));
        }

        List<String> categories = getCategoryRestrictions(voucher);
        for (String category : categories) {
            applicableProducts.addAll(productGrpcClient.getProductsByCategory(category, 0, 100));
        }

        List<String> sellerIds = getSellerRestrictions(voucher);
        for (String sellerId : sellerIds) {
            applicableProducts.addAll(productGrpcClient.getProductsBySeller(sellerId, 0, 100));
        }

        return applicableProducts.stream()
                .distinct()
                .map(this::mapToProductInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isVoucherApplicableToProduct(String voucherCode, String productId) {
        try {
            VoucherEntity voucher = voucherService.getVoucherEntityByCode(voucherCode);
            if (!hasAnyRestrictions(voucher)) {
                return true;
            }

            List<String> productRestrictions = getProductRestrictions(voucher);
            if (!productRestrictions.isEmpty() && productRestrictions.contains(productId)) {
                return true;
            }

            ProductInfo product = productGrpcClient.getProduct(productId);
            if (product == null) {
                return false;
            }

            List<String> categoryRestrictions = getCategoryRestrictions(voucher);
            if (!categoryRestrictions.isEmpty()) {
                for (String category : product.getCategoriesList()) {
                    if (categoryRestrictions.contains(category)) {
                        return true;
                    }
                }
            }

            List<String> sellerRestrictions = getSellerRestrictions(voucher);
            return !sellerRestrictions.isEmpty() && sellerRestrictions.contains(product.getSellerId());
        } catch (Exception e) {
            log.error("Error checking voucher applicability: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isVoucherApplicableToProducts(String voucherCode, List<String> productIds) {
        return productIds.stream().allMatch(productId -> isVoucherApplicableToProduct(voucherCode, productId));
    }

    private VoucherEntity createVoucherEntity(CreateProductAwareVoucherRequest request) {
        return getVoucherEntity(request.getCode(), request.getType(), request.getDiscount(), request.getStartDate(), request.getExpiryDate(), request.getActive(), request.getMaxUses(), request.getMaxUsesPerUser(), request.getMinSpend(), request);
    }

    static VoucherEntity getVoucherEntity(String code, VoucherTypeEnum type, Double discount, LocalDateTime startDate, LocalDateTime expiryDate, Boolean active, Integer maxUses, Integer maxUsesPerUser, BigDecimal minSpend, CreateProductAwareVoucherRequest request) {
        VoucherEntity voucherEntity = new VoucherEntity();
        voucherEntity.setCode(code);
        voucherEntity.setType(type.name());
        voucherEntity.setDiscount(discount);
        voucherEntity.setStartDate(startDate);
        voucherEntity.setExpiredDate(expiryDate);
        voucherEntity.setActivated(active);
        voucherEntity.setMaxUses(maxUses);
        voucherEntity.setMaxUsesPerUser(maxUsesPerUser);
        voucherEntity.setMinSpend(minSpend);
        return voucherEntity;
    }

    private void saveProductRestrictions(VoucherEntity voucher, List<String> productIds) {
        for (String productId : productIds) {
            VoucherRestrictionEntity restriction = new VoucherRestrictionEntity();
            restriction.setVoucher(voucher);
            restriction.setProductId(productId);
            voucherRestrictionRepository.save(restriction);
        }
    }

    private void saveCategoryRestrictions(VoucherEntity voucher, List<String> categories) {
        for (String category : categories) {
            VoucherCategoryRestrictionEntity restriction = new VoucherCategoryRestrictionEntity();
            restriction.setVoucher(voucher);
            restriction.setCategory(category);
            voucherCategoryRestrictionRepository.save(restriction);
        }
    }

    private void saveSellerRestrictions(VoucherEntity voucher, List<String> sellerIds) {
        for (String sellerId : sellerIds) {
            VoucherSellerRestrictionEntity restriction = new VoucherSellerRestrictionEntity();
            restriction.setVoucher(voucher);
            restriction.setSellerId(sellerId);
            voucherSellerRestrictionRepository.save(restriction);
        }
    }

    private ProductInfoResponse mapToProductInfoResponse(ProductInfo productInfo) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return ProductInfoResponse.builder()
                .id(productInfo.getId())
                .name(productInfo.getName())
                .sellerId(productInfo.getSellerId())
                .sellerShopName(productInfo.getSellerShopName())
                .shortDescription(productInfo.getShortDescription())
                .description(productInfo.getDescription())
                .thumbnail(productInfo.getThumbnail())
                .productImages(productInfo.getProductImagesList())
                .brand(productInfo.getBrand())
                .discount(productInfo.getDiscount())
                .model(productInfo.getModel())
                .currentPrice(BigDecimal.valueOf(productInfo.getCurrentPrice()))
                .categories(productInfo.getCategoriesList())
                .keywords(productInfo.getKeywordsList())
                .tags(productInfo.getTagsList())
                .verified(productInfo.getVerified())
                .createdAt(productInfo.getCreatedAt().isEmpty() ? null : LocalDateTime.parse(productInfo.getCreatedAt(), formatter))
                .updatedAt(productInfo.getUpdatedAt().isEmpty() ? null : LocalDateTime.parse(productInfo.getUpdatedAt(), formatter))
                .build();
    }

    private ProductAwareVoucherResponse mapToProductAwareVoucherResponse(VoucherEntity voucher) {
        List<String> productRestrictions = getProductRestrictions(voucher);
        List<String> categoryRestrictions = getCategoryRestrictions(voucher);
        List<String> sellerRestrictions = getSellerRestrictions(voucher);

        return ProductAwareVoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .type(org.retrade.voucher.model.constant.VoucherTypeEnum.valueOf(voucher.getType()))
                .discount(voucher.getDiscount())
                .startDate(voucher.getStartDate())
                .expiryDate(voucher.getExpiredDate())
                .active(voucher.getActivated())
                .maxUses(voucher.getMaxUses())
                .maxUsesPerUser(voucher.getMaxUsesPerUser())
                .minSpend(voucher.getMinSpend())
                .productRestrictions(productRestrictions)
                .categoryRestrictions(categoryRestrictions)
                .sellerRestrictions(sellerRestrictions)
                .isProductSpecific(!productRestrictions.isEmpty())
                .isCategorySpecific(!categoryRestrictions.isEmpty())
                .isSellerSpecific(!sellerRestrictions.isEmpty())
                .build();
    }

    private List<String> getProductRestrictions(VoucherEntity voucher) {
        return voucherRestrictionRepository.findByVoucher(voucher).stream()
                .map(VoucherRestrictionEntity::getProductId)
                .collect(Collectors.toList());
    }

    private List<String> getCategoryRestrictions(VoucherEntity voucher) {
        return voucherCategoryRestrictionRepository.findByVoucher(voucher).stream()
                .map(VoucherCategoryRestrictionEntity::getCategory)
                .collect(Collectors.toList());
    }

    private List<String> getSellerRestrictions(VoucherEntity voucher) {
        return voucherSellerRestrictionRepository.findByVoucher(voucher).stream()
                .map(VoucherSellerRestrictionEntity::getSellerId)
                .collect(Collectors.toList());
    }

    private boolean hasAnyRestrictions(VoucherEntity voucher) {
        return !getProductRestrictions(voucher).isEmpty() ||
               !getCategoryRestrictions(voucher).isEmpty() ||
               !getSellerRestrictions(voucher).isEmpty();
    }

    @Override
    public PaginationWrapper<List<VoucherSimpleResponse>> getVouchersSimple(QueryWrapper queryWrapper) {
        Page<VoucherEntity> voucherPage = voucherRepository.findByActivated(true, queryWrapper.pagination());
        List<VoucherSimpleResponse> voucherResponses = voucherPage.getContent().stream()
                .map(this::mapToVoucherSimpleResponse)
                .collect(Collectors.toList());

        return new PaginationWrapper.Builder<List<VoucherSimpleResponse>>()
                .setPaginationInfo(voucherPage)
                .setData(voucherResponses)
                .build();
    }

    @Override
    public VoucherSimpleResponse getVoucherSimpleByCode(String code) {
        VoucherEntity voucher = voucherService.getVoucherEntityByCode(code);
        return mapToVoucherSimpleResponse(voucher);
    }

    @Override
    public List<VoucherSimpleResponse> getVouchersSimpleForProduct(String productId) {
        List<VoucherRestrictionEntity> productRestrictions = voucherRestrictionRepository.findByProductId(productId);

        return productRestrictions.stream()
                .map(VoucherRestrictionEntity::getVoucher)
                .filter(VoucherEntity::getActivated)
                .map(this::mapToVoucherSimpleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductSimpleResponse> getApplicableProductsSimple(String voucherCode) {
        VoucherEntity voucher = voucherService.getVoucherEntityByCode(voucherCode);
        List<ProductInfo> applicableProducts = new ArrayList<>();

        List<String> productIds = getProductRestrictions(voucher);
        if (!productIds.isEmpty()) {
            applicableProducts.addAll(productGrpcClient.getProducts(productIds));
        }

        List<String> categories = getCategoryRestrictions(voucher);
        for (String category : categories) {
            applicableProducts.addAll(productGrpcClient.getProductsByCategory(category, 0, 100));
        }

        List<String> sellerIds = getSellerRestrictions(voucher);
        for (String sellerId : sellerIds) {
            applicableProducts.addAll(productGrpcClient.getProductsBySeller(sellerId, 0, 100));
        }

        return applicableProducts.stream()
                .distinct()
                .map(this::mapToProductSimpleResponse)
                .collect(Collectors.toList());
    }

    private VoucherSimpleResponse mapToVoucherSimpleResponse(VoucherEntity voucher) {
        return VoucherSimpleResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .type(voucher.getType())
                .discount(voucher.getDiscount())
                .expiryDate(voucher.getExpiredDate())
                .minSpend(voucher.getMinSpend())
                .title(voucher.getName())
                .description(voucher.getDescription())
                .build();
    }

    private ProductSimpleResponse mapToProductSimpleResponse(ProductInfo productInfo) {
        return ProductSimpleResponse.builder()
                .id(productInfo.getId())
                .name(productInfo.getName())
                .thumbnail(productInfo.getThumbnail())
                .currentPrice(BigDecimal.valueOf(productInfo.getCurrentPrice()))
                .sellerShopName(productInfo.getSellerShopName())
                .build();
    }
}
