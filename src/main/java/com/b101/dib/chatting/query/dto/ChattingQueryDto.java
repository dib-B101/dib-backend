package com.b101.dib.chatting.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChattingQueryDto {
    private Long chattingId;
    private Long orderId;
    private Long memberId;
    private String memberNickname;
    private String content;
    private LocalDateTime time;
}
