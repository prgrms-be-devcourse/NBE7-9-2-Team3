package org.example.backend.domain.point.controller;

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
public class PointController {

    private final PointService pointService;

    // 포인트 충전
    @PostMapping("/members/charge")
    public ResponseEntity<Void> chargePoint(@RequestBody ChargePointsRequestDto request) {
        pointService.chargePoint(request);
        return ResponseEntity.ok().build();
    }

    // 포인트 전체 내역 조회 (최신순 - 기본)
    @GetMapping("/members/{id}/history")
    public ResponseEntity<List<PointHistoryResponseDto>> getPointHistory(@PathVariable Long id) {
        List<PointHistoryResponseDto> pointHistory = pointService.getPointHistory(id);
        return ResponseEntity.ok(pointHistory);
    }

    // 결제
    @PostMapping("/members/purchase")
    public ResponseEntity<Void> purchaseItem(@RequestBody PurchaseRequestDto request) {
        pointService.purchaseItem(request);
        return ResponseEntity.ok().build();
    }
}
