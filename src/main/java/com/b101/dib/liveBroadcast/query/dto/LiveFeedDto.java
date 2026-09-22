package com.b101.dib.liveBroadcast.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// 프론트 LiveFeedResponse
@Getter
@Setter
@NoArgsConstructor
public class LiveFeedDto {
    private List<LiveFeedItemDto> items = new ArrayList<>();
    private String nextCursor;
    private boolean hasNext;
    private String serverTime;
}
