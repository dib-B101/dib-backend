package com.b101.dib.member.command.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50) String nickname,
        @Size(max = 500) String profileImageUrl
) {

    @AssertTrue
    public boolean isValidUpdate() {
        return (nickname != null || profileImageUrl != null)
                && (nickname == null || !nickname.isBlank());
    }
}
