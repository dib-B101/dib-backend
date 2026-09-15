package com.b101.dib.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 클라이언트 → /app/orders/{orderId}/messages
@Getter
@Setter
@NoArgsConstructor
public class SendChatCommand {
    private String commandId;
    private String content;
}
