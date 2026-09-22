package com.b101.dib.review.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 거래 후기 = 별점만. 코멘트는 받지 않는다 (팀 결정).
// 주문 하나당 한 번, 구매자가 판매자를 평가한다. 중복은 DB 의 uq_review_order 가 막는다.
@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    public static final int MIN_RATING = 0;
    public static final int MAX_RATING = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private Long orderId;
    private Long reviewerId;
    private Long sellerId;
    private Short rating;
    private LocalDateTime createdAt;

    public static Review of(Long orderId, Long reviewerId, Long sellerId, int rating) {
        validateRating(rating);
        return Review.builder()
                .orderId(orderId)
                .reviewerId(reviewerId)
                .sellerId(sellerId)
                .rating((short) rating)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static void validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(ErrorCode.REVIEW_RATING_INVALID);
        }
    }
}
