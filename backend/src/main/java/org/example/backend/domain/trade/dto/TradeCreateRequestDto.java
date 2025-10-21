package org.example.backend.domain.trade.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;
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

    public static TradeCreateRequestDto from(TradeRequestDto dto, BoardType type, Long memberId,
        List<MultipartFile> images) {
        return new TradeCreateRequestDto(
            memberId,
            type,
            dto.title(),
            dto.description(),
            dto.price(),
            dto.category(),
            images
        );
    }

    public Trade toEntity(Member member) {
        return new Trade(
            member,
            this.boardType,
            this.title,
            this.description,
            this.price,
            TradeStatus.SELLING,
            this.category,
            LocalDateTime.now()
        );
    }
}