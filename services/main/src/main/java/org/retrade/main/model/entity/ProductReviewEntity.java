package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "product_reviews")
public class ProductReviewEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = ProductEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @ManyToOne(targetEntity = CustomerEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
    @ManyToOne(targetEntity = OrderEntity.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderComboEntity order;
    @Column(name = "vote", nullable = false)
    private Double vote;
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name="image_review")
    private Set<String> imageReview;
    @Column(name = "status")
    private Boolean status;
    @Column(name ="helpful")
    private double helpful;
    @Column(name = "reply_content")
    private String replyContent;
    @Column(name = "reply_created_date")
    private Timestamp replyCreatedDate;
    @Column(name = "reply_updated_date")
    private Timestamp replyUpdatedDate;
}
