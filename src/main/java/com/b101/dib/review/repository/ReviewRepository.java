package com.b101.dib.review.repository;

import com.b101.dib.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    // 판매자 평점을 매번 다시 계산한다. 누적합을 따로 들고 있으면 누락·중복 보정이 어렵고,
    // 한 판매자의 후기 수는 인덱스(ix_review_seller)로 훑을 만한 규모다
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sellerId = :sellerId")
    Double averageRatingOf(@Param("sellerId") Long sellerId);

    long countBySellerId(Long sellerId);
}
