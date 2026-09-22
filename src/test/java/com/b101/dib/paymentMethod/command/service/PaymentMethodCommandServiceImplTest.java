package com.b101.dib.paymentMethod.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.payment.toss.TossBillingKeyResponse;
import com.b101.dib.payment.toss.TossPaymentsClient;
import com.b101.dib.paymentMethod.repository.PaymentMethodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodCommandServiceImplTest {
    @Mock PaymentMethodRepository paymentMethodRepository;
    @Mock MemberRepository memberRepository;
    @Mock TossPaymentsClient tossPaymentsClient;
    @InjectMocks PaymentMethodCommandServiceImpl service;

    @Test
    void rejectsBillingResponseForDifferentCustomer() {
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(paymentMethodRepository.existsByMemberId(1L)).thenReturn(false);
        when(tossPaymentsClient.issueBillingKey("auth-key", "customer-1"))
                .thenReturn(new TossBillingKeyResponse("billing-key", "customer-2", "테스트카드", null));

        assertThatThrownBy(() -> service.register(1L, "auth-key", "customer-1"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BILLING_KEY_ISSUE_FAILED));
        verify(paymentMethodRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsMissingBillingKeyResponse() {
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(paymentMethodRepository.existsByMemberId(1L)).thenReturn(false);
        when(tossPaymentsClient.issueBillingKey("auth-key", "customer-1")).thenReturn(null);

        assertThatThrownBy(() -> service.register(1L, "auth-key", "customer-1"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BILLING_KEY_ISSUE_FAILED));
        verify(paymentMethodRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
