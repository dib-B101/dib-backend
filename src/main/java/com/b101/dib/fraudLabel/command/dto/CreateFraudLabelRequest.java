package com.b101.dib.fraudLabel.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFraudLabelRequest {
    @NotNull
    private Long auctionId;

    @NotNull
    private Long memberId;

    @NotNull
    @Min(0)
    @Max(1)
    private Short label;

    @NotBlank
    @Size(max = 30)
    private String labelSource;

    private String reason;
}
