package com.b101.dib.chatting.repository;

import com.b101.dib.chatting.domain.Chatting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingRepository extends JpaRepository<Chatting, Long> {
}
