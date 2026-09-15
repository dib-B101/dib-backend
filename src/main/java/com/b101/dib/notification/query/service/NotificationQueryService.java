package com.b101.dib.notification.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.notification.query.dto.NotificationQueryDto;

public interface NotificationQueryService {
    CursorPageDto<NotificationQueryDto> findMine(Long memberId, Boolean unreadOnly, String cursor, int size);
    long countUnread(Long memberId);
}
