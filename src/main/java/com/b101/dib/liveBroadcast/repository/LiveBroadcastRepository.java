package com.b101.dib.liveBroadcast.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;

public interface LiveBroadcastRepository extends JpaRepository<LiveBroadcast, Long>{

	boolean existsByMemberIdAndStatus(Long memberId, LiveBroadcastStatus status);

}
