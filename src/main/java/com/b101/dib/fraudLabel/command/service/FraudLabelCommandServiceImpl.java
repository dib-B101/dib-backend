package com.b101.dib.fraudLabel.command.service;

import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.fraudLabel.command.dto.CreateFraudLabelRequest;
import com.b101.dib.fraudLabel.domain.FraudLabel;
import com.b101.dib.fraudLabel.repository.FraudLabelRepository;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FraudLabelCommandServiceImpl implements FraudLabelCommandService {
    private final FraudLabelRepository fraudLabelRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;

    @Override
    public FraudLabel create(CreateFraudLabelRequest request) {
        if (!auctionRepository.existsById(request.getAuctionId())) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_FOUND);
        }
        if (!memberRepository.existsById(request.getMemberId())) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (fraudLabelRepository.existsByAuctionIdAndMemberId(request.getAuctionId(), request.getMemberId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LABEL);
        }

        FraudLabel fraudLabel = FraudLabel.builder()
                .auctionId(request.getAuctionId())
                .memberId(request.getMemberId())
                .label(request.getLabel())
                .labelSource(request.getLabelSource())
                .reason(request.getReason())
                .confirmedAt(LocalDateTime.now())
                .build();
        return fraudLabelRepository.save(fraudLabel);
    }
}
