package org.retrade.main.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.ProductConditionEnum;
import org.retrade.main.model.entity.ProductEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String sellerShopName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String shortDescription;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Text, analyzer = "standard")
    private ProductConditionEnum condition;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private String brand;

    @Field(type = FieldType.Keyword)
    private String brandId;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private String model;

    @Field(type = FieldType.Double)
    private BigDecimal currentPrice;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String addressLine;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard"), otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
    })
    private String state;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String district;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String ward;

    @Field(type = FieldType.Nested)
    private List<CategoryInfoDocument> categories;

    @Field(type = FieldType.Boolean)
    private Boolean verified;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Date createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Date updatedAt;

    public static ProductDocument wrapEntityToDocument(ProductEntity productEntity) {
        var seller = productEntity.getSeller();
        return ProductDocument.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .sellerId(seller.getId())
                .condition(productEntity.getCondition())
                .sellerShopName(seller.getShopName())
                .addressLine(seller.getAddressLine())
                .ward(seller.getWard())
                .district(seller.getDistrict())
                .state(seller.getState())
                .shortDescription(productEntity.getShortDescription())
                .description(productEntity.getDescription())
                .brand(productEntity.getBrand().getName())
                .brandId(productEntity.getBrand().getId())
                .model(productEntity.getModel())
                .currentPrice(productEntity.getCurrentPrice())
                .categories(productEntity.getCategories().stream().map(item -> CategoryInfoDocument.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .build()).toList())
                .verified(productEntity.getVerified())
                .createdAt(productEntity.getCreatedDate() != null ? productEntity.getCreatedDate() : null)
                .updatedAt(productEntity.getUpdatedDate() != null ? productEntity.getUpdatedDate() : null)
                .build();
    }
}
