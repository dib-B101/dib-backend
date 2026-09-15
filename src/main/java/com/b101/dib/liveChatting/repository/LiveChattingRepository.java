package com.b101.dib.liveChatting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.liveChatting.domain.LiveChatting;

public interface LiveChattingRepository extends JpaRepository<LiveChatting, Long>{

}
