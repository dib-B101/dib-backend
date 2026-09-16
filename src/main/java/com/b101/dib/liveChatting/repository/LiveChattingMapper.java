package com.b101.dib.liveChatting.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.b101.dib.liveChatting.query.dto.LiveChattingQueryDto;

@Mapper
public interface LiveChattingMapper {

	List<LiveChattingQueryDto> findByLiveBroadcastId(Long liveBroadcastId);

}
