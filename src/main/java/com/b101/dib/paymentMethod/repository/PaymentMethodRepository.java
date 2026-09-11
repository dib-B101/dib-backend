package com.b101.dib.paymentMethod.repository;

import com.b101.dib.paymentMethod.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByMemberId(Long memberId);
    boolean existsByMemberId(Long memberId);
}
