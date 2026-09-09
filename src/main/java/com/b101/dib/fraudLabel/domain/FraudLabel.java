package com.b101.dib.fraudLabel.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_label")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fraudLabelId;

    private Long auctionId;
    private Long memberId;
    private Short label;
    private String labelSource;
    private String reason;
    private LocalDateTime confirmedAt;
}
