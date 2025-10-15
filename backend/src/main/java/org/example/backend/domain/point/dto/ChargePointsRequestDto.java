package org.example.backend.domain.point.dto;

public record ChargePointsRequestDto(
        Long memberId,
        Long amount
) {}
