package com.b101.dib.review.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.review.domain.Review;
import com.b101.dib.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Review write(Long memberId, Long orderId, int rating) {
        Review.validateRating(rating);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!memberId.equals(order.getBuyerId())) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_BUYER);
        }
        // 구매확정 = "물건을 받았고 문제없다" 는 뜻이다. 그 전에는 평가할 근거가 없다
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_WRITTEN);
        }

        Review review = Review.of(orderId, memberId, order.getSellerId(), rating);
        try {
            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            // 위 existsByOrderId 와 저장 사이에 같은 요청이 두 번 들어온 경우 — uq_review_order 가 막는다
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_WRITTEN);
        }

        recalculateSellerScore(order.getSellerId());
        return review;
    }

    // 누적합을 따로 들고 있으면 누락·중복 보정이 어렵다. 후기가 들어올 때마다 다시 센다
    private void recalculateSellerScore(Long sellerId) {
        Member seller = memberRepository.findById(sellerId).orElse(null);
        if (seller == null) {
            return;
        }
        Double average = reviewRepository.averageRatingOf(sellerId);
        long count = reviewRepository.countBySellerId(sellerId);
        // 소수 둘째 자리까지. 4.3333... 을 그대로 내보내면 화면마다 반올림이 달라진다
        seller.setScore(average == null ? null : Math.round(average * 100) / 100.0);
        seller.setReviewCount((int) count);
        seller.setUpdatedAt(LocalDateTime.now());
    }
}
