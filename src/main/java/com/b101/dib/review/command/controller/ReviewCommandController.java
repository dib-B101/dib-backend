package com.b101.dib.review.command.controller;

import com.b101.dib.review.command.dto.ReviewRequest;
import com.b101.dib.review.command.service.ReviewCommandService;
import com.b101.dib.review.domain.Review;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 구매확정된 거래에 별점을 남긴다. 코멘트는 받지 않는다
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ReviewCommandController {

    private final ReviewCommandService reviewCommandService;

    @PostMapping("/{orderId}/review")
    public ResponseEntity<Map<String, Object>> write(@RequestHeader("X-Member-Id") Long memberId,
                                                     @PathVariable("orderId") Long orderId,
                                                     @Valid @RequestBody ReviewRequest request) {
        Review review = reviewCommandService.write(memberId, orderId, request.getRating());

        Map<String, Object> data = new HashMap<>();
        data.put("reviewId", String.valueOf(review.getReviewId()));
        data.put("orderId", String.valueOf(review.getOrderId()));
        data.put("rating", review.getRating());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "평가 완료");
        body.put("data", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
