package com.b101.dib.order.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequest {
    @NotBlank
    private String carrier;
    @NotBlank
    private String trackingNumber;
}
