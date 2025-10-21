package org.example.backend.domain.point.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.point.dto.ChargePointsRequestDto;
import org.example.backend.domain.point.dto.PointHistoryResponseDto;
import org.example.backend.domain.point.dto.PurchaseRequestDto;
import org.example.backend.domain.point.service.PointService;
import org.springframework.http.ResponseEntity;
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
        @Parameter(description = "포인트 충전 정보", required = true)
        @RequestBody ChargePointsRequestDto request) {
        pointService.chargePoint(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "포인트 내역 조회", description = "회원의 포인트 사용 내역을 조회합니다.")
    @GetMapping("/members/{id}/history")
    public ResponseEntity<List<PointHistoryResponseDto>> getPointHistory(
        @Parameter(description = "회원 ID", required = true)
        @PathVariable Long id) {
        List<PointHistoryResponseDto> pointHistory = pointService.getPointHistory(id);
        return ResponseEntity.ok(pointHistory);
    }

    @Operation(summary = "포인트 결제", description = "포인트를 사용하여 아이템을 구매합니다.")
    @PostMapping("/members/purchase")
    public ResponseEntity<Void> purchaseItem(
        @Parameter(description = "구매 정보", required = true)
        @RequestBody PurchaseRequestDto request) {
        pointService.purchaseItem(request);
        return ResponseEntity.ok().build();
    }
}
