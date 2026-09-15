package com.b101.dib.member.command.dto;

import java.time.LocalDateTime;

public record UpdateProfileResponse(
        Long memberId,
        String nickname,
        String profileImageUrl,
        LocalDateTime updatedAt
) {
}
