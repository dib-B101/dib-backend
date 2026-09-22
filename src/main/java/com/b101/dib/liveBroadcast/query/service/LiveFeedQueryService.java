package com.b101.dib.liveBroadcast.query.service;

import com.b101.dib.liveBroadcast.query.dto.LiveFeedDto;

public interface LiveFeedQueryService {
    LiveFeedDto feed(Long memberId, String cursor, int size);
}
