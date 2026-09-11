package com.b101.dib.payment.repository;

import com.b101.dib.payment.query.dto.PaymentDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {
    PaymentDetailDto findById(@Param("paymentId") Long paymentId);
}
