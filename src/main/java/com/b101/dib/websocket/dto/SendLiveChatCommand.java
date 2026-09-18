package com.b101.dib.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 클라이언트 → /app/live/{liveBroadcastId}/messages
// Setter 는 Jackson 이 STOMP 페이로드를 채우는 경로라 SendChatCommand 와 같이 남겨둔다
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendLiveChatCommand {
    private String commandId;
    private String content;
}
