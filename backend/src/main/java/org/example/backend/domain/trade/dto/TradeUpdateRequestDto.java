package org.example.backend.domain.trade.dto;

import java.util.List;
import org.example.backend.domain.trade.enums.BoardType;
import org.springframework.web.multipart.MultipartFile;

public record TradeUpdateRequestDto(
    BoardType boardType,
    Long tradeId,
    Long memberId,
    TradeRequestDto tradeData,
    List<MultipartFile> images
) {
    public static TradeUpdateRequestDto of(
        BoardType boardType,
        Long tradeId,
        Long memberId,
        TradeRequestDto tradeData,
        List<MultipartFile> images
    ) {
        return new TradeUpdateRequestDto(boardType, tradeId, memberId, tradeData, images);
    }
}
