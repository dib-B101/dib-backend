package com.b101.dib.member.query.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.query.dto.MemberDetailDto;
import com.b101.dib.member.repository.MemberMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceImplTest {

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberQueryServiceImpl memberQueryService;

    @Test
    void returnsAuthenticatedMemberInformation() {
        MemberDetailDto member = new MemberDetailDto();
        member.setMemberId(1L);
        member.setEmail("member@example.com");
        given(memberMapper.findById(1L)).willReturn(member);

        MemberDetailDto result = memberQueryService.findMine(1L);

        assertThat(result).isSameAs(member);
    }

    @Test
    void rejectsWhenAuthenticatedMemberNoLongerExists() {
        given(memberMapper.findById(1L)).willReturn(null);

        assertThatThrownBy(() -> memberQueryService.findMine(1L))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND)
                );
    }
}
