package org.example.backend.domain.tradechat.dto;

/*
 클라이언트가 메시지를 전송할 때 사용하는 요청 DTO
 - content만 필요
 */
public record TradeChatMessageRequest(
        String content
) {}

