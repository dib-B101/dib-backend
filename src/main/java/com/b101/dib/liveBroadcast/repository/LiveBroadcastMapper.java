package com.b101.dib.liveBroadcast.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastQueryDto;

@Mapper
public interface LiveBroadcastMapper {

	List<LiveBroadcastQueryDto> findAll();

	LiveBroadcastQueryDto findById(Long liveBroadcastId);

	List<LiveBroadcastQueryDto> findByMemberId(Long memberId);

}
