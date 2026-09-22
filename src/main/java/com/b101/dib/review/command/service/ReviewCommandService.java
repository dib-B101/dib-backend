package com.b101.dib.review.command.service;

import com.b101.dib.review.domain.Review;

public interface ReviewCommandService {
    // 구매확정된 거래에 별점을 남긴다. 판매자 평점(member.score)도 여기서 다시 계산한다
    Review write(Long memberId, Long orderId, int rating);
}
