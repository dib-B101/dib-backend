package com.b101.dib.memberEvent.repository;

import com.b101.dib.memberEvent.domain.MemberEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberEventRepository extends JpaRepository<MemberEvent, Long> {
}
