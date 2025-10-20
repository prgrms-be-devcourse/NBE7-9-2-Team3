package org.example.backend.domain.trade.dto;

import java.util.List;
import org.example.backend.domain.trade.enums.BoardType;
import org.springframework.web.multipart.MultipartFile;

public record TradeCreateRequestDto(
    Long memberId,
    BoardType boardType,
    String title,
    String description,
    Long price,
    String category,
    List<MultipartFile> images
) {
    public static TradeCreateRequestDto from(TradeRequestDto dto, BoardType type, List<MultipartFile> images) {
        return new TradeCreateRequestDto(
            dto.memberId(),
            type,
            dto.title(),
            dto.description(),
            dto.price(),
            dto.category(),
            images
        );
    }
}