package com.b101.dib.fraudDetection.command.service;

import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.ai.AiServerClient;
import com.b101.dib.common.util.Times;
import com.b101.dib.fraudDetection.command.dto.BidAnomalyBidDto;
import com.b101.dib.fraudDetection.command.dto.BidAnomalyCallbackRequest;
import com.b101.dib.fraudDetection.command.dto.BidAnomalyFeaturesDto;
import com.b101.dib.fraudDetection.command.dto.BidAnomalyRequest;
import com.b101.dib.fraudDetection.domain.FraudDetection;
import com.b101.dib.fraudDetection.repository.FraudDetectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionCommandServiceImpl implements FraudDetectionCommandService {
    public static final String CALLBACK_PATH = "/internal/v1/ai/callbacks/bid-anomalies";

    private final BidRepository bidRepository;
    private final FraudDetectionRepository fraudDetectionRepository;
    private final AiServerClient aiServerClient;

    @Override
    public int requestAnalysis(Long auctionId) {
        List<Bid> bids = bidRepository.findAllByAuctionIdOrderByAmountAsc(auctionId);
        if (bids.isEmpty()) {
            return 0;
        }
        List<BidAnomalyBidDto> bidDtos = bids.stream()
                .map(b -> BidAnomalyBidDto.builder()
                        .bidId(b.getBidId()).memberId(b.getMemberId()).amount(b.getAmount()).createdAt(Times.iso(b.getCreatedAt()))
                        .build())
                .toList();
        Map<Long, Bid> topBidByMember = new LinkedHashMap<>();   // 입찰자별 마지막(최고) 입찰
        for (Bid b : bids) {
            topBidByMember.put(b.getMemberId(), b);
        }
        if (!aiServerClient.isEnabled()) {
            log.info("AI 비활성 — 이상입찰 분석 생략 auctionId={} 입찰자 {}명", auctionId, topBidByMember.size());
            return 0;
        }
        int sent = 0;
        for (Bid top : topBidByMember.values()) {
            BidAnomalyRequest req = BidAnomalyRequest.builder()
                    .jobId("bid-anomaly-" + auctionId + "-" + top.getMemberId())   // 재시도해도 같은 값 → AI 가 중복 분석 안 함
                    .auctionId(auctionId)
                    .memberId(top.getMemberId())
                    .bidId(top.getBidId())
                    .bids(bidDtos)
                    .callbackUrl(aiServerClient.callbackUrl(CALLBACK_PATH))
                    .build();
            if (aiServerClient.postAccepted(AiServerClient.BID_ANOMALIES_PATH, req, "이상입찰 " + req.getJobId())) {
                sent++;
            }
        }
        return sent;
    }

    @Override
    @Transactional
    public FraudDetection saveCallback(BidAnomalyCallbackRequest r) {
        if (fraudDetectionRepository.existsByAuctionIdAndMemberIdAndFeatureVersion(r.getAuctionId(), r.getMemberId(), r.getFeatureVersion())) {
            return null;
        }
        BidAnomalyFeaturesDto f = r.getFeatures() == null ? new BidAnomalyFeaturesDto() : r.getFeatures();
        return fraudDetectionRepository.save(FraudDetection.builder()
                .auctionId(r.getAuctionId())
                .memberId(r.getMemberId())
                .bidId(r.getBidId())
                .bidderTendency(f.getBidderTendency())
                .biddingRatio(f.getBiddingRatio())
                .lastBidding(f.getLastBidding())
                .auctionBids(f.getAuctionBids())
                .startingPriceAverage(f.getStartingPriceAverage())
                .earlyBidding(f.getEarlyBidding())
                .winningRatio(f.getWinningRatio())
                .auctionDuration(f.getAuctionDuration())
                .ruleScore(f.getRuleScore())
                .mlScore(f.getMlScore())
                .riskScore(f.getRiskScore())
                .predictedLabel(r.getPredictedLabel() == null ? null : r.getPredictedLabel().shortValue())
                .decisionThreshold(r.getDecisionThreshold())
                .modelVersion(r.getModelVersion())
                .featureVersion(r.getFeatureVersion())
                .detectedAt(LocalDateTime.now())
                .build());
    }
}
