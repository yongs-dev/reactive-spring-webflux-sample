package com.mark.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {

    @Id
    private String reviewId;

    @NotNull(message = "rating.movieInfoId : must not be null")
    private Long movieInfoId;

    private String comment;

    @Min(value = 0L, message = "rating.negative : rating is negative and please pass a non-negative value")
    private Double rating;

    public void updateEvaluation(Review review) {
        this.comment = review.getComment();
        this.rating = review.getRating();
    }
}
