package org.example.backend.domain.tradechat.dto;

import org.example.backend.domain.tradechat.entity.ChatStatus;
import org.example.backend.domain.tradechat.entity.TradeChatRoom;

import java.time.LocalDateTime;

public record TradeChatRoomDto(
        Long Id,
        String tradeTitle,
        LocalDateTime createDate,
        ChatStatus status
) {
    public static TradeChatRoomDto from(TradeChatRoom room) {
        return new TradeChatRoomDto(
                room.getId(),
                room.getTrade().getTitle(),
                room.getCreateDate(),
                room.getStatus()
        );
    }
}
