package com.b101.dib.member.command.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-15T03:00:00Z");

    @Mock
    private MemberRepository memberRepository;

    private MemberCommandServiceImpl memberCommandService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        memberCommandService = new MemberCommandServiceImpl(memberRepository, clock);
    }

    @Test
    void updatesAuthenticatedMemberNickname() {
        Member member = member(1L, "기존닉네임");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByNickname("새닉네임")).willReturn(false);

        UpdateProfileResponse result = memberCommandService.updateProfile(
                1L,
                new UpdateProfileRequest(
                        "  새닉네임  ",
                        "  https://cdn.example.com/members/1/profile.webp  "
                )
        );

        assertThat(member.getNickname()).isEqualTo("새닉네임");
        assertThat(member.getProfileImageUrl())
                .isEqualTo("https://cdn.example.com/members/1/profile.webp");
        assertThat(member.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 9, 15, 3, 0));
        assertThat(result).isEqualTo(new UpdateProfileResponse(
                1L,
                "새닉네임",
                "https://cdn.example.com/members/1/profile.webp",
                LocalDateTime.of(2026, 9, 15, 3, 0)
        ));
        verify(memberRepository).saveAndFlush(member);
    }

    @Test
    void allowsKeepingCurrentNickname() {
        Member member = member(1L, "기존닉네임");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        memberCommandService.updateProfile(1L, new UpdateProfileRequest("기존닉네임", null));

        verify(memberRepository, never()).existsByNickname("기존닉네임");
        verify(memberRepository).saveAndFlush(member);
    }

    @Test
    void rejectsDuplicatedNickname() {
        Member member = member(1L, "기존닉네임");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByNickname("중복닉네임")).willReturn(true);

        assertThatThrownBy(() -> memberCommandService.updateProfile(
                1L,
                new UpdateProfileRequest("중복닉네임", null)
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.NICKNAME_DUPLICATED)
        );

        verify(memberRepository, never()).saveAndFlush(member);
    }

    @Test
    void translatesConcurrentNicknameConflict() {
        Member member = member(1L, "기존닉네임");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByNickname("새닉네임")).willReturn(false);
        given(memberRepository.saveAndFlush(member)).willThrow(
                new DataIntegrityViolationException("uq_member_nickname")
        );

        assertThatThrownBy(() -> memberCommandService.updateProfile(
                1L,
                new UpdateProfileRequest("새닉네임", null)
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.NICKNAME_DUPLICATED)
        );
    }

    @Test
    void rejectsWhenAuthenticatedMemberNoLongerExists() {
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberCommandService.updateProfile(
                1L,
                new UpdateProfileRequest("새닉네임", null)
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND)
        );
    }

    @Test
    void removesProfileImageWhenEmptyValueIsProvided() {
        Member member = member(1L, "기존닉네임");
        member.setProfileImageUrl("https://cdn.example.com/old.webp");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        UpdateProfileResponse result = memberCommandService.updateProfile(
                1L,
                new UpdateProfileRequest(null, "   ")
        );

        assertThat(member.getProfileImageUrl()).isNull();
        assertThat(result.profileImageUrl()).isNull();
        verify(memberRepository).saveAndFlush(member);
    }

    private Member member(Long memberId, String nickname) {
        return Member.builder()
                .id(memberId)
                .nickname(nickname)
                .build();
    }
}
