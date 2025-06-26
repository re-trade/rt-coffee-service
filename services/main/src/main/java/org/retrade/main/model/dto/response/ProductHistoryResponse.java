package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistoryResponse {
    private String productId;
    private String productName;
    private String productThumbnail;
    private String productDescription;
    private String ownerId;
    private String ownerName;
    private String ownerAvatarUrl;
}
