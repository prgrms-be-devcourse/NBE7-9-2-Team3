package org.example.backend.domain.fish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.domain.fish.service.FishLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fish/{fishId}/fishLog")
@RequiredArgsConstructor
@Tag(name = "FishLog", description = "물고기 기록 관리 API")
public class FishLogController {
    
    private final FishLogService fishLogService;
    
    // Create - 특정 물고기에 로그 생성
    @Operation(summary = "물고기 기록 생성", description = "특정 물고기 관리를 위한 기록을 생성합니다.")
    @PostMapping
    public ResponseEntity<FishLogResponseDto> createLog(
            @PathVariable Long fishId,
            @RequestBody FishLogRequestDto requestDto) {
        // fishId를 requestDto에 설정
        requestDto.setFishId(fishId);
        FishLogResponseDto responseDto = fishLogService.createLog(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    
    // Read - 특정 물고기의 모든 로그 조회
    @Operation(summary = "물고기 기록 목록 조회", description = "특정 물고기의 모든 기록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<FishLogResponseDto>> getLogsByFishId(
            @PathVariable Long fishId) {
        List<FishLogResponseDto> logs = fishLogService.getLogsByFishId(fishId);
        return ResponseEntity.ok(logs);
    }
    
    
    // Update - 로그 수정
    @Operation(summary = "물고기 기록 수정", description = "특정 물고기의 특정 기록을 수정합니다.")
    @PutMapping("/{logId}")
    public ResponseEntity<FishLogResponseDto> updateLog(
            @PathVariable Long fishId,
            @PathVariable Long logId,
            @RequestBody FishLogRequestDto requestDto) {
        // fishId를 requestDto에 설정
        requestDto.setFishId(fishId);
        FishLogResponseDto responseDto = fishLogService.updateLog(logId, requestDto);
        return ResponseEntity.ok(responseDto);
    }
    
    // Delete - 로그 삭제
    @Operation(summary = "물고기 기록 삭제", description = "특정 물고기의 특정 기록을 삭제합니다.")
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable Long fishId,
            @PathVariable Long logId) {
        fishLogService.deleteLog(logId);
        return ResponseEntity.noContent().build();
    }
}
