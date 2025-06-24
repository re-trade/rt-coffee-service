package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.ProductReviewRequest;
import org.retrade.main.model.dto.response.ProductReviewResponse;
import org.retrade.main.model.entity.CustomerEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.ProductReviewEntity;
import org.retrade.main.repository.OrderRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.ProductReviewRepository;
import org.retrade.main.service.ProductReviewService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {
    private final AuthUtils authUtils;
    private final ProductReviewRepository productReviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    @Override
    public ProductReviewResponse addProductReview(ProductReviewRequest request) {
        var customerEntity = getCurrentCustomerAccount();
        OrderEntity order = orderRepository.getById(request.getOrderId());
        List<ProductEntity> productList = productRepository.findProductsByOrder(order);
        ProductEntity product = productRepository.getById(request.getProductId());
        boolean exists = productList.stream()
                .anyMatch(p -> p.getId().equals(product.getId()));
        boolean customerExists = order.getCustomer().equals(customerEntity);
        if (!exists) throw new ValidationException("Product not found in order");
        if (!customerExists) throw new ValidationException("Customer is not in this order");
        ProductReviewEntity productReviewEntity = new ProductReviewEntity();
        productReviewEntity.setOrder(order);
        productReviewEntity.setProduct(product);
        productReviewEntity.setVote(request.getVote());
        productReviewEntity.setContent(request.getContent());
        productReviewRepository.save(productReviewEntity);
        return ProductReviewResponse.builder()
                .orderId(request.getOrderId())
                .productId(request.getProductId())
                .vote(request.getVote())
                .content(request.getContent())
                .build();
    }

    private CustomerEntity getCurrentCustomerAccount() {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();
        if (customerEntity == null) {
            throw new ValidationException("User is not a customer");
        }
        return customerEntity;
    }
}
