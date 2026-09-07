package com.b101.dib.auction.command.entity;

import com.b101.dib.auction.query.dto.AuctionStatus;
import com.b101.dib.auction.query.dto.AuctionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "auction")
public class Auction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionId;
    private Long memberId;
    private Long productId;
    private Long categoryId;
    private Long startPrice;
    private Long currentPrice;
    private Long bidIncrement;
    private Integer auctionTime;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AuctionStatus status;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AuctionType auctionType;
    private Integer bidCount;
    private Integer bidderCount;
    private Integer viewCount;
    private Integer bookmarkCount;
    private Long topBidId;
    private Integer extensionCount;
    private Long depositAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long liveBroadcastId;
    @Version
    private Long version;
}
