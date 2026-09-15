package com.b101.dib.chatting.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChattingPageDto {
    private List<ChattingQueryDto> items;
    private boolean hasMore;
    private boolean chattingReadOnly;
}
