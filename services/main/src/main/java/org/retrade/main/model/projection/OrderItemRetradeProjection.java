package org.retrade.main.model.projection;

public interface OrderItemRetradeProjection {
    String getId();
    String getProductId();
    Long getRetradeQuantity();
    Long getQuantity();
    String getSellerId();
    String getSellerName();
    String getSellerAvatarUrl();
}
