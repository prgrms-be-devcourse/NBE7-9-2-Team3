package org.example.backend.domain.tradechat.dto;

import org.example.backend.domain.tradechat.entity.TradeChatMessage;

import java.time.LocalDateTime;

public record TradeChatMessageDto(
        Long senderId,
        String content,
        LocalDateTime sendDate
) {
    public static TradeChatMessageDto from(TradeChatMessage message) {
        return new TradeChatMessageDto(
                message.getSender().getMemberId(),
                message.getContent(),
                message.getSendDate()
        );
    }
}
