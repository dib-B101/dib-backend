package com.b101.dib.liveBroadcast.query.dto;

import com.b101.dib.common.util.Times;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트 LiveBroadcastDto. streamUrl 자리에는 LiveKit room name 을 준다 (프론트가 LiveKit 토큰 발급에 쓰는 값)
@Getter
@Setter
@NoArgsConstructor
public class LiveBroadcastCardDto {
    private Long liveBroadcastId;
    private Long memberId;
    private String title;
    private String description;
    private LiveBroadcastStatus status;
    private String streamUrl;
    private String livekitRoomName;
    private Integer viewCount;
    private String scheduledAt;
    private String startedAt;
    private String endedAt;
    // 홈 LIVE 카드는 영상 대신 대표 상품 사진·제목을 보여준다
    private Integer itemCount;
    private String firstItemTitle;
    private String firstItemThumbnailUrl;

    public static LiveBroadcastCardDto from(LiveBroadcastQueryDto q) {
        LiveBroadcastCardDto d = new LiveBroadcastCardDto();
        d.setLiveBroadcastId(q.getLiveBroadcastId());
        d.setMemberId(q.getMemberId());
        d.setTitle(q.getTitle());
        d.setDescription(q.getDescription());
        d.setStatus(q.getStatus());
        d.setStreamUrl(q.getLivekitRoomName());
        d.setLivekitRoomName(q.getLivekitRoomName());
        d.setViewCount(q.getViewCount());
        // 예약 방송은 started_at 에 방송 예정 시각을 담는다(create/update 가 request.startedAt 을 넣는다).
        // 예전엔 created_at 을 내려서 홈 카드가 "등록한 시각 예정" 으로 보였다
        d.setScheduledAt(Times.iso(q.getStartedAt() != null ? q.getStartedAt() : q.getCreatedAt()));
        d.setStartedAt(Times.iso(q.getStartedAt()));
        d.setEndedAt(Times.iso(q.getEndedAt()));
        d.setItemCount(q.getItemCount());
        d.setFirstItemTitle(q.getFirstItemTitle());
        d.setFirstItemThumbnailUrl(q.getFirstItemThumbnailUrl());
        return d;
    }
}
