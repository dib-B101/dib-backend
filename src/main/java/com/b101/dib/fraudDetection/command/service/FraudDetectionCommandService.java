package com.b101.dib.fraudDetection.command.service;

import com.b101.dib.fraudDetection.command.dto.BidAnomalyCallbackRequest;
import com.b101.dib.fraudDetection.domain.FraudDetection;

public interface FraudDetectionCommandService {
    // 경매 종료 후 입찰자마다 AI 분석 요청 (명세 92). 요청 보낸 건수 반환
    int requestAnalysis(Long auctionId);

    // 명세 93 콜백 저장. 같은 (auction, member, featureVersion) 이 이미 있으면 저장 안 함 (null 반환)
    FraudDetection saveCallback(BidAnomalyCallbackRequest request);
}
