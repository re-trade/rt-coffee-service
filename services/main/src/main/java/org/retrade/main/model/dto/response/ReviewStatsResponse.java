package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewStatsResponse {
    private long totalReviews;
    private double averageRating;
    private long repliedReviews;
    private double replyRate;
    private long totalPositiveReviews;
    private double averagePositiveReviews;
    private List<RatingDistribution> ratingDistribution;
}
