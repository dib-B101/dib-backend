package com.b101.dib.settlement.repository;

import java.util.Optional;
import com.b101.dib.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    boolean existsByOrderId(Long orderId);

    Optional<Settlement> findByOrderId(Long orderId);
}
