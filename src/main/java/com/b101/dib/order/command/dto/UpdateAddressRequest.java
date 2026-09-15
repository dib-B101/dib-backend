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
public class UpdateAddressRequest {
    @NotBlank
    private String zip;
    @NotBlank
    private String address;
    private String detail;
    @NotBlank
    private String receiverName;
    @NotBlank
    private String receiverPhone;

    public String toJson() {
        return "{\"zip\":" + q(zip) + ",\"address\":" + q(address) + ",\"detail\":" + q(detail)
                + ",\"receiverName\":" + q(receiverName) + ",\"receiverPhone\":" + q(receiverPhone) + "}";
    }

    private static String q(String v) {
        if (v == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : v.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
