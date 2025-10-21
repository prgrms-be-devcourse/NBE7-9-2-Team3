package org.example.backend.domain.point.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.point.dto.PointHistoryResponseDto;
import org.example.backend.domain.point.dto.PurchaseRequestDto;
import org.example.backend.domain.point.service.PointService;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
@Tag(name = "Point", description = "포인트 관리 API")
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 충전", description = "회원의 포인트를 충전합니다.")
    @PostMapping("/members/charge")
    public ResponseEntity<Void> chargePoint(
        @Parameter(description = "포인트 충전 요금", required = true)
        @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long amount) {
        Long memberId = userDetails.getId();
        pointService.chargePoint(memberId, amount);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "포인트 내역 조회", description = "회원의 포인트 사용 내역을 조회합니다.")
    @GetMapping("/members/{id}/history")
    public ResponseEntity<List<PointHistoryResponseDto>> getPointHistory(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId =  userDetails.getId();
        List<PointHistoryResponseDto> pointHistory = pointService.getPointHistory(memberId);
        return ResponseEntity.ok(pointHistory);
    }

    @Operation(summary = "포인트 결제", description = "포인트를 사용하여 아이템을 구매합니다.")
    @PostMapping("/members/purchase")
    public ResponseEntity<Void> purchaseItem(
        @Parameter(description = "판매 정보", required = true)
        @RequestBody PurchaseRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long buyerId = userDetails.getId();
        pointService.purchaseItem(buyerId, request);
        return ResponseEntity.ok().build();
    }
}
