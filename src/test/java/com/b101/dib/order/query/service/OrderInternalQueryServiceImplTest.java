package com.b101.dib.order.query.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.repository.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderInternalQueryServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderInternalQueryServiceImpl orderInternalQueryService;

    @Test
    void returnsOrderWithoutMemberAuthorization() {
        OrderDetailDto expected = new OrderDetailDto();
        expected.setOrderId(1L);
        given(orderMapper.findById(1L)).willReturn(expected);

        OrderDetailDto actual = orderInternalQueryService.find(1L);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void throwsWhenOrderDoesNotExist() {
        given(orderMapper.findById(1L)).willReturn(null);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> orderInternalQueryService.find(1L))
                .satisfies(exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }
}
