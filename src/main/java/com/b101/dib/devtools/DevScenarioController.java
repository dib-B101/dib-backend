package com.b101.dib.devtools;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev")
@Profile("local")
@RequiredArgsConstructor
public class DevScenarioController {
    private final DevSmsCaptureSender smsCaptureSender;
    private final DevScenarioService devScenarioService;
    private final NotificationRepository notificationRepository;

    @GetMapping("/sms/{phone}/last-code")
    public ResponseEntity<Map<String, Object>> lastSmsCode(@PathVariable("phone") String phone) {
        String code = smsCaptureSender.lastCode(phone);
        Map<String, Object> map = new HashMap<>();
        map.put("message", code == null ? "[DEV] 발송된 인증번호 없음" : "[DEV] 마지막 인증번호");
        map.put("code", code);
        return ResponseEntity.status(code == null ? HttpStatus.NOT_FOUND : HttpStatus.OK).body(map);
    }

    @PostMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<Map<String, Object>> bid(@PathVariable("auctionId") Long auctionId,
                                                   @RequestBody @Valid DevBidRequest request) {
        Map<String, Object> map = devScenarioService.bid(auctionId, request.getMemberId(), request.getAmount());
        map.put("message", "[DEV] 입찰 성공");
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @PostMapping("/auctions/{auctionId}/end")
    public ResponseEntity<Map<String, Object>> end(@PathVariable("auctionId") Long auctionId) {
        Auction auction = devScenarioService.end(auctionId);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "[DEV] 경매 종료");
        map.put("auctionId", auction.getAuctionId());
        map.put("status", auction.getStatus());
        map.put("currentPrice", auction.getCurrentPrice());
        map.put("topBidId", auction.getTopBidId());
        return ResponseEntity.ok(map);
    }

    @PostMapping("/orders/{orderId}/expire")
    public ResponseEntity<Map<String, Object>> expire(@PathVariable("orderId") Long orderId) {
        devScenarioService.expireNow(orderId);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "[DEV] 결제 기한 만료 처리 실행");
        map.put("orderId", orderId);
        return ResponseEntity.ok(map);
    }

    @PostMapping("/orders/{orderId}/auto-confirm")
    public ResponseEntity<Map<String, Object>> autoConfirm(@PathVariable("orderId") Long orderId) {
        devScenarioService.autoConfirmNow(orderId);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "[DEV] 자동 구매확정 실행");
        map.put("orderId", orderId);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/members/{memberId}/notifications")
    public ResponseEntity<Map<String, Object>> notifications(@PathVariable("memberId") Long memberId) {
        List<Notification> list = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "[DEV] 알림 목록");
        map.put("data", list);
        return ResponseEntity.ok(map);
    }
}
