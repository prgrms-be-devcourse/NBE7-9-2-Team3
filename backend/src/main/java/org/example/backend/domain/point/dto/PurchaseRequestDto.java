package org.example.backend.domain.point.dto;

public record PurchaseRequestDto(
        Long buyerId,
        Long sellerId,
        Long amount
) {}
