package com.b101.dib.payment.repository;

import com.b101.dib.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByPaymentKey(String paymentKey);
    Optional<Payment> findByPaymentKey(String paymentKey);

    // 재시도로 한 주문에 실패 기록이 여러 건 남을 수 있어 가장 최근 결제를 환불 대상으로 본다
    Optional<Payment> findFirstByOrderIdOrderByPaymentIdDesc(Long orderId);
}
