package org.example.backend.domain.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.enums.TradeStatus;

public record TradeRequestDto(
    @NotNull Long memberId,
    @NotBlank String title,
    @NotBlank String description,
    @NotNull @Positive Long price,
    @NotNull TradeStatus status,
    String category
) {

    public Trade toEntity(Member member, BoardType boardType) {
        return new Trade(
            member,
            boardType,
            this.title,
            this.description,
            this.price,
            this.status,
            this.category,
            LocalDateTime.now()
        );
    }
}