package com.b101.dib.payment.repository;

import com.b101.dib.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByPaymentKey(String paymentKey);
    Optional<Payment> findByPaymentKey(String paymentKey);
}
