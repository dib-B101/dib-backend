package com.b101.dib.bid.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bid")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;

    private Long auctionId;
    private Long memberId;
    private Long amount;
    private LocalDateTime createdAt;

    public static Bid place(Long auctionId, Long memberId, Long amount) {
        return Bid.builder()
                .auctionId(auctionId)
                .memberId(memberId)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
