package com.b101.dib.liveBroadcast.repository;

import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 홈 라이브 피드용 (LiveBroadcastMapper 와 분리 — 라이브 담당 파일 무수정)
@Mapper
public interface LiveFeedMapper {
    // LIVE 먼저, 그다음 SCHEDULED. 커서는 offset
    List<LiveBroadcastQueryDto> findFeed(@Param("offset") int offset, @Param("limit") int limit);

    List<LiveBroadcastQueryDto> findLive(@Param("limit") int limit);
}
