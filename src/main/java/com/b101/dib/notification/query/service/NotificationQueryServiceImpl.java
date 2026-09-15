package com.b101.dib.notification.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.notification.query.dto.NotificationQueryDto;
import com.b101.dib.notification.repository.NotificationMapper;
import com.b101.dib.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {
    private final NotificationMapper notificationMapper;
    private final NotificationRepository notificationRepository;

    @Override
    public CursorPageDto<NotificationQueryDto> findMine(Long memberId, Boolean unreadOnly, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<NotificationQueryDto> rows = notificationMapper.findByMemberId(memberId, unreadOnly, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, NotificationQueryDto::getNotificationId);
    }

    @Override
    public long countUnread(Long memberId) {
        return notificationRepository.countByMemberIdAndIsReadFalse(memberId);
    }
}
