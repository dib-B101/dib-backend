package com.b101.dib.scenario;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import com.b101.dib.auction.command.service.AuctionCommandService;
import com.b101.dib.auction.command.service.AuctionEndTxService;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.command.service.BidCommandService;
import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.ai.AiServerClient;
import com.b101.dib.member.domain.Gender;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.liveBroadcast.command.dto.CreateRequest;
import com.b101.dib.liveBroadcast.command.dto.LiveItemRequest;
import com.b101.dib.liveBroadcast.command.service.LiveBroadcastCommandService;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.domain.NotificationType;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.command.dto.ShipmentRequest;
import com.b101.dib.order.command.dto.UpdateAddressRequest;
import com.b101.dib.order.command.service.OrderCommandService;
import com.b101.dib.order.command.service.OrderExpiryTxService;
import com.b101.dib.order.command.service.OrderOfferService;
import com.b101.dib.order.command.service.ShipmentCommandService;
import com.b101.dib.order.command.service.ShipmentTxService;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.outboxEvent.command.service.OutboxEventRecorder;
import com.b101.dib.payment.command.service.PaymentCommandService;
import com.b101.dib.payment.domain.Payment;
import com.b101.dib.payment.domain.PaymentType;
import com.b101.dib.payment.repository.PaymentRepository;
import com.b101.dib.payment.toss.TossPaymentResponse;
import com.b101.dib.payment.toss.TossPaymentsClient;
import com.b101.dib.paymentMethod.domain.PaymentMethod;
import com.b101.dib.paymentMethod.repository.PaymentMethodRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.command.dto.ProductModerationResponse;
import com.b101.dib.product.command.dto.ProductUpdateRequest;
import com.b101.dib.product.command.service.ProductCommandService;
import com.b101.dib.product.command.service.ProductModerationTxService;
import com.b101.dib.product.domain.ProductCondition;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.report.command.dto.CreateOrderReportRequest;
import com.b101.dib.report.command.dto.CreateReportRequest;
import com.b101.dib.report.command.dto.ProcessReportRequest;
import com.b101.dib.report.command.service.ReportCommandService;
import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.question.command.dto.AnswerQuestionRequest;
import com.b101.dib.question.command.dto.CreateQuestionRequest;
import com.b101.dib.question.command.service.QuestionCommandService;
import com.b101.dib.question.domain.Question;
import com.b101.dib.question.repository.QuestionRepository;
import com.b101.dib.settlement.command.service.SettlementCommandService;
import com.b101.dib.settlement.domain.Settlement;
import com.b101.dib.settlement.repository.SettlementRepository;
import com.b101.dib.websocket.controller.LiveWebsocketController;
import com.b101.dib.websocket.dto.SocketEnvelope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
        "spring.flyway.locations=classpath:db/migration",
        "spring.kafka.listener.auto-startup=false",
        "spring.task.scheduling.enabled=false",
        "dib.lock.redis-enabled=false",
        "dib.realtime.redis-enabled=false",
        "dib.cache.snapshot-enabled=false",
        "dib.ai.enabled=false",
        "dib.ai.moderation-enabled=true",
        "livekit.url=http://localhost:7880",
        "livekit.api-key=test-key",
        "livekit.api-secret=test-secret-test-secret-test-secret"
})
@Testcontainers
class TradeCycleIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg18")
                    .asCompatibleSubstituteFor("postgres")
    );

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired BidCommandService bidCommandService;
    @Autowired AuctionEndTxService auctionEndTxService;
    @Autowired AuctionCommandService auctionCommandService;
    @Autowired OrderExpiryTxService orderExpiryTxService;
    @Autowired OrderOfferService orderOfferService;
    @Autowired PaymentCommandService paymentCommandService;
    @Autowired ShipmentCommandService shipmentCommandService;
    @Autowired ShipmentTxService shipmentTxService;
    @Autowired OrderCommandService orderCommandService;
    @Autowired SettlementCommandService settlementCommandService;
    @Autowired ProductCommandService productCommandService;
    @Autowired ProductModerationTxService productModerationTxService;
    @Autowired ReportCommandService reportCommandService;
    @Autowired QuestionCommandService questionCommandService;
    @Autowired LiveBroadcastCommandService liveBroadcastCommandService;
    @Autowired LiveWebsocketController liveWebsocketController;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductRepository productRepository;
    @Autowired AuctionRepository auctionRepository;
    @Autowired BidRepository bidRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentMethodRepository paymentMethodRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired SettlementRepository settlementRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired LiveBroadcastRepository liveBroadcastRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @MockitoBean TossPaymentsClient tossPaymentsClient;
    @MockitoBean OutboxEventRecorder outboxEventRecorder;
    @MockitoBean AiServerClient aiServerClient;

    @Test
    void samePriceConcurrentBidAllowsExactlyOneWinner() throws Exception {
        Actors actors = actors();
        Auction auction = activeAuction(actors.seller().getId());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<BidOutcome> first = executor.submit(bid(auction.getAuctionId(), actors.buyerB().getId(), 10_000L, ready, start));
            Future<BidOutcome> second = executor.submit(bid(auction.getAuctionId(), actors.buyerC().getId(), 10_000L, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<BidOutcome> outcomes = List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
            assertThat(outcomes).filteredOn(BidOutcome::success).hasSize(1);
            assertThat(outcomes).filteredOn(outcome -> !outcome.success())
                    .extracting(BidOutcome::errorCode)
                    .containsExactly(ErrorCode.BID_TOO_LOW);
        }

        Auction saved = auctionRepository.findById(auction.getAuctionId()).orElseThrow();
        Bid top = bidRepository.findById(saved.getTopBidId()).orElseThrow();
        assertThat(saved.getCurrentPrice()).isEqualTo(10_000L);
        assertThat(saved.getBidCount()).isEqualTo(1);
        assertThat(top.getMemberId()).isIn(actors.buyerB().getId(), actors.buyerC().getId());
    }

    @Test
    void differentPriceConcurrentBidKeepsHigherPriceAndBidder() throws Exception {
        Actors actors = actors();
        Auction auction = activeAuction(actors.seller().getId());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<BidOutcome> lower = executor.submit(bid(auction.getAuctionId(), actors.buyerB().getId(), 11_000L, ready, start));
            Future<BidOutcome> higher = executor.submit(bid(auction.getAuctionId(), actors.buyerC().getId(), 12_000L, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<BidOutcome> outcomes = List.of(lower.get(10, TimeUnit.SECONDS), higher.get(10, TimeUnit.SECONDS));
            assertThat(outcomes).filteredOn(outcome -> !outcome.success())
                    .extracting(BidOutcome::errorCode)
                    .allMatch(code -> code == ErrorCode.BID_TOO_LOW);
        }

        Auction saved = auctionRepository.findById(auction.getAuctionId()).orElseThrow();
        Bid top = bidRepository.findById(saved.getTopBidId()).orElseThrow();
        assertThat(saved.getCurrentPrice()).isEqualTo(12_000L);
        assertThat(top.getMemberId()).isEqualTo(actors.buyerC().getId());
    }

    @Test
    void bidInsideClosingWindowResetsRemainingTimeToFifteenSeconds() {
        Actors actors = actors();
        Auction auction = activeAuction(actors.seller().getId());
        LocalDateTime originalEnd = LocalDateTime.now().plusSeconds(5);
        auction.setEndedAt(originalEnd);
        auctionRepository.saveAndFlush(auction);

        bidCommandService.place(auction.getAuctionId(), actors.buyerB().getId(), 10_000L);

        Auction saved = auctionRepository.findById(auction.getAuctionId()).orElseThrow();
        assertThat(saved.getExtensionCount()).isEqualTo(1);
        assertThat(saved.getEndedAt()).isAfter(originalEnd);
        assertThat(saved.getEndedAt()).isBetween(
                LocalDateTime.now().plusSeconds(12),
                LocalDateTime.now().plusSeconds(Auction.EXTEND_WINDOW_SECONDS + 1));
    }

    @Test
    void noBidAuctionEndsUnsoldAndCanBeRelisted() {
        Actors actors = actors();
        Auction auction = activeAuction(actors.seller().getId());
        auction.setEndedAt(LocalDateTime.now().minusSeconds(1));
        auctionRepository.saveAndFlush(auction);

        AuctionEndResultDto result = auctionEndTxService.endOne(auction.getAuctionId());
        assertThat(result.getResult()).isEqualTo("UNSOLD");
        Product product = productRepository.findById(auction.getProductId()).orElseThrow();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.REGISTERED);

        Auction relisted = auctionCommandService.relist(actors.seller().getId(), auction.getAuctionId());
        assertThat(relisted.getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
        assertThat(relisted.getStartPrice()).isNull();
        Auction restarted = auctionCommandService.startAuction(
                actors.seller().getId(), auction.getAuctionId(), 15_000L, 300);
        assertThat(restarted.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(restarted.getCurrentPrice()).isEqualTo(15_000L);
    }

    @Test
    void rejectedProductCanBeEditedAndApprovedOnReview() {
        Actors actors = actors();
        Product product = product(actors.seller().getId(), ProductStatus.PENDING);
        ProductModerationResponse rejected = moderation("REJECTED", "금칙어 포함");
        productModerationTxService.applyVerdict(product.getProductId(), rejected);
        assertThat(productRepository.findById(product.getProductId()).orElseThrow().getStatus())
                .isEqualTo(ProductStatus.REJECTED);

        given(aiServerClient.isEnabled()).willReturn(true);
        productCommandService.update(actors.seller().getId(), product.getProductId(),
                ProductUpdateRequest.builder().title("수정된 정상 상품").build());
        assertThat(productRepository.findById(product.getProductId()).orElseThrow().getStatus())
                .isEqualTo(ProductStatus.PENDING);

        ProductModerationResponse approved = moderation("REGISTERED", "검수 통과");
        productModerationTxService.applyVerdict(product.getProductId(), approved);
        assertThat(productRepository.findById(product.getProductId()).orElseThrow().getStatus())
                .isEqualTo(ProductStatus.REGISTERED);
        assertThat(auctionRepository.findByProductId(product.getProductId())).isNotNull();
    }

    @Test
    void paymentFailureRunnerUpDeliveryConfirmationAndSettlementComplete() {
        Actors actors = actors();
        Auction auction = activeAuction(actors.seller().getId());

        bidCommandService.place(auction.getAuctionId(), actors.buyerB().getId(), 10_000L);
        bidCommandService.place(auction.getAuctionId(), actors.buyerC().getId(), 11_000L);
        bidCommandService.place(auction.getAuctionId(), actors.buyerB().getId(), 12_000L);

        Auction ending = auctionRepository.findById(auction.getAuctionId()).orElseThrow();
        ending.setEndedAt(LocalDateTime.now().minusSeconds(1));
        auctionRepository.saveAndFlush(ending);
        AuctionEndResultDto ended = auctionEndTxService.endOne(auction.getAuctionId());
        assertThat(ended.getWinnerId()).isEqualTo(actors.buyerB().getId());

        assertThatThrownBy(() -> paymentCommandService.autoCharge(ended.getOrderId()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        Order firstOrder = orderRepository.findById(ended.getOrderId()).orElseThrow();
        firstOrder.setPaymentDue(LocalDateTime.now().minusMinutes(1));
        orderRepository.saveAndFlush(firstOrder);
        orderExpiryTxService.expireOne(firstOrder.getOrderId());
        assertThat(orderRepository.findById(firstOrder.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CANCELED);

        Notification offer = notificationRepository
                .findTopByAuctionIdAndMemberIdAndTypeAndTitleOrderByCreatedAtDesc(
                        auction.getAuctionId(), actors.buyerC().getId(), NotificationType.AUCTION_WON,
                        Notification.OFFER_TITLE)
                .orElseThrow();
        assertThat(offer.getMemberId()).isEqualTo(actors.buyerC().getId());

        Order runnerUpOrder = orderOfferService.accept(actors.buyerC().getId(), auction.getAuctionId());
        assertThat(runnerUpOrder.getFinalPrice()).isEqualTo(11_000L);

        paymentMethodRepository.save(PaymentMethod.builder()
                .memberId(actors.buyerC().getId())
                .type(PaymentType.CARD)
                .billingKey("billing-key")
                .customerKey("customer-key")
                .cardCompany("테스트카드")
                .cardNumber("1234")
                .createdAt(LocalDateTime.now())
                .build());
        given(tossPaymentsClient.chargeBilling(anyString(), anyString(), anyLong(), anyString(), anyString()))
                .willReturn(new TossPaymentResponse("payment-key", "toss-order", "DONE", null,
                        11_000L, new TossPaymentResponse.Receipt("https://receipt.test"), null));

        paymentCommandService.autoCharge(runnerUpOrder.getOrderId());
        assertThat(orderRepository.findById(runnerUpOrder.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PAID);

        shipmentCommandService.updateAddress(actors.buyerC().getId(), runnerUpOrder.getOrderId(),
                UpdateAddressRequest.builder()
                        .zip("06236")
                        .address("서울특별시 강남구 테헤란로")
                        .detail("101호")
                        .receiverName("구매자C")
                        .receiverPhone("01033334444")
                        .build());
        shipmentCommandService.ship(actors.seller().getId(), runnerUpOrder.getOrderId(),
                ShipmentRequest.builder()
                        .carrier("DUMMY")
                        .trackingNumber("2026-09-18T15:00:00Z")
                        .build());
        shipmentTxService.markDelivered(runnerUpOrder.getOrderId());
        orderCommandService.confirm(actors.buyerC().getId(), runnerUpOrder.getOrderId());

        Order completed = orderRepository.findById(runnerUpOrder.getOrderId()).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        Settlement settlement = settlementRepository.findByOrderId(completed.getOrderId()).orElseThrow();
        settlementCommandService.execute(settlement.getSettlementId());
        assertThat(settlementRepository.findById(settlement.getSettlementId()).orElseThrow().getPayoutAt()).isNotNull();
    }

    @Test
    void deliveredOrderIsAutomaticallyConfirmedAndSettled() {
        Actors actors = actors();
        Order order = paidOrder(actors, 25_000L);
        order.updateAddress("{\"address\":\"서울\"}");
        order.ship("DUMMY", "2026-09-18T15:00:00Z");
        order.deliver();
        order.setUpdatedAt(LocalDateTime.now().minusDays(6));
        orderRepository.saveAndFlush(order);

        orderExpiryTxService.confirmOne(order.getOrderId(), LocalDateTime.now().minusDays(5));

        assertThat(orderRepository.findById(order.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
        assertThat(settlementRepository.findByOrderId(order.getOrderId())).isPresent();
    }

    @Test
    void orderReportHoldsTransactionThenSupportsResumeOrRefund() {
        Actors actors = actors();
        Order resumed = paidOrder(actors, 30_000L);
        resumed.updateAddress("{\"address\":\"서울\"}");
        orderRepository.saveAndFlush(resumed);

        Long resumeReportId = reportCommandService.reportOrder(
                actors.buyerB().getId(), resumed.getOrderId(), orderReport("상품 상태 확인 요청"));
        assertThat(orderRepository.findById(resumed.getOrderId()).orElseThrow().isOnHold()).isTrue();
        assertThatThrownBy(() -> shipmentCommandService.ship(
                actors.seller().getId(), resumed.getOrderId(), dummyShipment()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ON_HOLD));

        reportCommandService.process(resumeReportId,
                ProcessReportRequest.builder().status(ReportStatus.REJECTED).adminNote("이상 없음").build());
        assertThat(orderRepository.findById(resumed.getOrderId()).orElseThrow().isOnHold()).isFalse();
        shipmentCommandService.ship(actors.seller().getId(), resumed.getOrderId(), dummyShipment());
        assertThat(orderRepository.findById(resumed.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.SHIPPED);

        Order refunded = paidOrder(actors, 40_000L);
        Payment payment = paymentRepository.saveAndFlush(Payment.builder()
                .orderId(refunded.getOrderId())
                .buyerId(refunded.getBuyerId())
                .amount(refunded.getFinalPrice())
                .type(PaymentType.CARD)
                .paymentKey("refund-payment-key")
                .paidAt(LocalDateTime.now())
                .build());
        Long refundReportId = reportCommandService.reportOrder(
                actors.buyerB().getId(), refunded.getOrderId(), orderReport("설명과 다른 상품"));
        given(tossPaymentsClient.cancel("refund-payment-key", "신고 환불", null))
                .willReturn(new TossPaymentResponse("refund-payment-key", "refund-order", "CANCELED", null,
                        40_000L, null, List.of(new TossPaymentResponse.Cancel("refund-transaction", 40_000L, "신고 환불"))));

        reportCommandService.process(refundReportId,
                ProcessReportRequest.builder()
                        .status(ReportStatus.REFUNDED)
                        .refundReason("신고 환불")
                        .build());

        assertThat(orderRepository.findById(refunded.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.REFUNDED);
        assertThat(orderRepository.findById(refunded.getOrderId()).orElseThrow().isOnHold()).isFalse();
        assertThat(paymentRepository.findById(payment.getPaymentId()).orElseThrow().isRefunded()).isTrue();
    }

    @Test
    void adminAnswerCreatesNotificationAndMemberCanAskAgain() {
        Actors actors = actors();
        Question first = questionCommandService.create(actors.buyerB().getId(),
                CreateQuestionRequest.builder()
                        .title("결제 문의")
                        .content("결제 실패 사유를 알고 싶습니다.")
                        .build());

        questionCommandService.answer(first.getQuestionId(),
                AnswerQuestionRequest.builder().answer("결제 수단을 다시 확인해 주세요.").build());

        Question answered = questionRepository.findById(first.getQuestionId()).orElseThrow();
        assertThat(answered.getAnsweredAt()).isNotNull();
        assertThat(notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(actors.buyerB().getId()))
                .anyMatch(notification -> "문의 답변 완료".equals(notification.getTitle()));

        Question followUp = questionCommandService.create(actors.buyerB().getId(),
                CreateQuestionRequest.builder()
                        .title("결제 추가 문의")
                        .content("카드를 변경한 뒤 다시 시도하면 되나요?")
                        .build());
        assertThat(followUp.getQuestionId()).isNotEqualTo(first.getQuestionId());
    }

    @Test
    void auctionAndLiveParticipantReportsCanBeProcessedWithSanction() {
        Actors actors = actors();
        Auction auction = activeAuction(actors.seller().getId());
        Long auctionReportId = reportCommandService.reportAuction(
                actors.buyerB().getId(), auction.getAuctionId(),
                CreateReportRequest.builder().content("허위 상품 정보").build());
        reportCommandService.process(auctionReportId,
                ProcessReportRequest.builder()
                        .status(ReportStatus.ACCEPTED)
                        .sanction(true)
                        .sanctionStatus(MemberStatus.SUSPENDED)
                        .build());
        assertThat(memberRepository.findById(actors.seller().getId()).orElseThrow().getStatus())
                .isEqualTo(MemberStatus.SUSPENDED);

        Long liveParticipantReportId = reportCommandService.reportMember(
                actors.buyerC().getId(), actors.buyerB().getId(),
                CreateReportRequest.builder()
                        .content("[라이브 방송 ID] 77\n채팅 욕설")
                        .build());
        reportCommandService.process(liveParticipantReportId,
                ProcessReportRequest.builder()
                        .status(ReportStatus.ACCEPTED)
                        .sanction(true)
                        .build());
        assertThat(memberRepository.findById(actors.buyerB().getId()).orElseThrow().getStatus())
                .isEqualTo(MemberStatus.SUSPENDED);
    }

    @Test
    void liveAuctionSnapshotRestoresStateAfterReconnectAndClosesNormally() {
        Actors actors = actors();
        Product product = product(actors.seller().getId(), ProductStatus.REGISTERED);
        Auction scheduled = auctionRepository.saveAndFlush(
                Auction.scheduled(product.getProductId(), null, null, LocalDateTime.now()));
        LiveBroadcast live = liveBroadcastCommandService.create(actors.seller().getId(),
                CreateRequest.builder()
                        .title("통합 라이브")
                        .description("라이브 경매 테스트")
                        .startedAt(LocalDateTime.now().plusMinutes(5))
                        .build());
        liveBroadcastCommandService.setItems(actors.seller().getId(), live.getLiveBroadcastId(),
                List.of(LiveItemRequest.builder()
                        .auctionId(scheduled.getAuctionId())
                        .startPrice(10_000L)
                        .auctionTime(300)
                        .build()));
        liveBroadcastCommandService.start(actors.seller().getId(), live.getLiveBroadcastId());
        liveBroadcastCommandService.startLiveAuction(
                actors.seller().getId(), live.getLiveBroadcastId(), scheduled.getAuctionId());

        bidCommandService.place(scheduled.getAuctionId(), actors.buyerB().getId(), 10_000L);
        bidCommandService.place(scheduled.getAuctionId(), actors.buyerC().getId(), 11_000L);

        SocketEnvelope firstSnapshot = liveWebsocketController.snapshot(
                live.getLiveBroadcastId(), () -> actors.buyerC().getId().toString());
        SocketEnvelope reconnectSnapshot = liveWebsocketController.snapshot(
                live.getLiveBroadcastId(), () -> actors.buyerC().getId().toString());
        assertThat(firstSnapshot.getEventType()).isEqualTo("LIVE_SNAPSHOT");
        assertThat(reconnectSnapshot.getPayload().get("activeAuction"))
                .isEqualTo(firstSnapshot.getPayload().get("activeAuction"));
        @SuppressWarnings("unchecked")
        var activeAuction = (java.util.Map<String, Object>) reconnectSnapshot.getPayload().get("activeAuction");
        assertThat(activeAuction.get("currentPrice")).isEqualTo(11_000L);

        Auction ending = auctionRepository.findById(scheduled.getAuctionId()).orElseThrow();
        ending.setEndedAt(LocalDateTime.now().minusSeconds(1));
        auctionRepository.saveAndFlush(ending);
        AuctionEndResultDto ended = auctionEndTxService.endOne(scheduled.getAuctionId());
        liveBroadcastCommandService.end(actors.seller().getId(), live.getLiveBroadcastId());

        assertThat(ended.getWinnerId()).isEqualTo(actors.buyerC().getId());
        assertThat(liveBroadcastRepository.findById(live.getLiveBroadcastId()).orElseThrow().getStatus())
                .isEqualTo(LiveBroadcastStatus.ENDED);
        assertThat(orderRepository.findById(ended.getOrderId())).isPresent();
    }

    private Callable<BidOutcome> bid(Long auctionId, Long memberId, Long amount,
                                     CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            start.await();
            try {
                bidCommandService.place(auctionId, memberId, amount);
                return new BidOutcome(memberId, true, null);
            } catch (BusinessException exception) {
                return new BidOutcome(memberId, false, exception.getErrorCode());
            }
        };
    }

    private Actors actors() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new Actors(
                member("seller-" + suffix, "0101" + suffix, true),
                member("buyer-b-" + suffix, "0102" + suffix, false),
                member("buyer-c-" + suffix, "0103" + suffix, false)
        );
    }

    private Member member(String key, String phone, boolean seller) {
        LocalDateTime now = LocalDateTime.now();
        return memberRepository.saveAndFlush(Member.builder()
                .email(key + "@example.com")
                .password("encoded-password")
                .nickname(key)
                .name(seller ? "판매자" : "구매자")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber(phone)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .score(50.0)
                .createdAt(now)
                .updatedAt(now)
                .bankName(seller ? "테스트은행" : null)
                .accountHolder(seller ? "판매자" : null)
                .accountNumber(seller ? "1234567890" : null)
                .warningCount(0)
                .build());
    }

    private Auction activeAuction(Long sellerId) {
        Product product = product(sellerId, ProductStatus.ON_AUCTION);
        return auctionRepository.saveAndFlush(Auction.builder()
                .productId(product.getProductId())
                .startPrice(10_000L)
                .currentPrice(10_000L)
                .auctionTime(300)
                .startedAt(LocalDateTime.now().minusMinutes(1))
                .endedAt(LocalDateTime.now().plusMinutes(4))
                .status(AuctionStatus.ACTIVE)
                .bidCount(0)
                .bidderCount(0)
                .viewCount(0)
                .bookmarkCount(0)
                .extensionCount(0)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Product product(Long sellerId, ProductStatus status) {
        String categoryName = "통합테스트-" + UUID.randomUUID();
        Long categoryId = jdbcTemplate.queryForObject(
                "INSERT INTO category (name) VALUES (?) RETURNING category_id", Long.class, categoryName);
        return productRepository.saveAndFlush(Product.builder()
                .memberId(sellerId)
                .categoryId(categoryId)
                .title("통합 테스트 상품")
                .description("거래 전체 사이클 검증")
                .condition(ProductCondition.GOOD)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private ProductModerationResponse moderation(String status, String reason) {
        ProductModerationResponse response = new ProductModerationResponse();
        response.setProductStatus(status);
        response.setReason(reason);
        response.setStage("rule");
        response.setContentHash(UUID.randomUUID().toString());
        return response;
    }

    private Order paidOrder(Actors actors, long price) {
        Auction auction = activeAuction(actors.seller().getId());
        auction.setStatus(AuctionStatus.ENDED);
        auction.setEndedAt(LocalDateTime.now());
        auctionRepository.saveAndFlush(auction);
        Order order = Order.create(auction.getAuctionId(), actors.seller().getId(),
                actors.buyerB().getId(), price, LocalDateTime.now());
        order.pay();
        return orderRepository.saveAndFlush(order);
    }

    private CreateOrderReportRequest orderReport(String content) {
        return CreateOrderReportRequest.builder()
                .content(content)
                .type(ReportType.ORDER)
                .build();
    }

    private ShipmentRequest dummyShipment() {
        return ShipmentRequest.builder()
                .carrier("DUMMY")
                .trackingNumber("2026-09-18T15:00:00Z")
                .build();
    }

    private record Actors(Member seller, Member buyerB, Member buyerC) {}

    private record BidOutcome(Long memberId, boolean success, ErrorCode errorCode) {}
}
