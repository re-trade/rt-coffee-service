package org.retrade.main.model.dto.request;

import org.retrade.main.model.constant.ProductStatusEnum;

public record UpdateProductStatusRequest(String productId, ProductStatusEnum status) {
}
