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
        d.setScheduledAt(Times.iso(q.getCreatedAt()));
        d.setStartedAt(Times.iso(q.getStartedAt()));
        d.setEndedAt(Times.iso(q.getEndedAt()));
        return d;
    }
}
