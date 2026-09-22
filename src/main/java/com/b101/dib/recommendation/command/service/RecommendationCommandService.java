package com.b101.dib.recommendation.command.service;

public interface RecommendationCommandService {
    boolean request(String jobId, Long memberId, String scope);
}
