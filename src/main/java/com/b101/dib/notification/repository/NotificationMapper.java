package com.b101.dib.notification.repository;

import com.b101.dib.notification.query.dto.NotificationQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    List<NotificationQueryDto> findByMemberId(@Param("memberId") Long memberId,
                                              @Param("unreadOnly") Boolean unreadOnly,
                                              @Param("cursor") Long cursor,
                                              @Param("limit") int limit);
}
