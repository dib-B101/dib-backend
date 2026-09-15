package com.b101.dib.devtools;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevBidRequest {
    @NotNull
    private Long memberId;
    @NotNull
    @Positive
    private Long amount;
}
