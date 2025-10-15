package org.example.backend.domain.fish.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.domain.fish.service.FishLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aquarium/{aquariumId}/fish/{fishId}/fishLog")
@RequiredArgsConstructor
public class FishLogController {
    
    private final FishLogService fishLogService;
    
    // Create - 특정 어항의 특정 물고기에 로그 생성
    @PostMapping
    public ResponseEntity<FishLogResponseDto> createLog(
            @PathVariable Long aquariumId,
            @PathVariable Long fishId,
            @RequestBody FishLogRequestDto requestDto) {
        // aquariumId와 fishId를 requestDto에 설정
        requestDto.setAquariumId(aquariumId);
        requestDto.setFishId(fishId);
        FishLogResponseDto responseDto = fishLogService.createLog(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    
    // Read - 특정 어항의 특정 물고기의 모든 로그 조회
    @GetMapping
    public ResponseEntity<List<FishLogResponseDto>> getLogsByAquariumIdAndFishId(
            @PathVariable Long aquariumId,
            @PathVariable Long fishId) {
        List<FishLogResponseDto> logs = fishLogService.getLogsByAquariumIdAndFishId(aquariumId, fishId);
        return ResponseEntity.ok(logs);
    }
    
    // 특정 로그 조회
    @GetMapping("/{logId}")
    public ResponseEntity<FishLogResponseDto> getLogById(
            @PathVariable Long aquariumId,
            @PathVariable Long fishId,
            @PathVariable Long logId) {
        FishLogResponseDto responseDto = fishLogService.getLogById(logId);
        return ResponseEntity.ok(responseDto);
    }
    
    // Update - 로그 수정
    @PutMapping("/{logId}")
    public ResponseEntity<FishLogResponseDto> updateLog(
            @PathVariable Long aquariumId,
            @PathVariable Long fishId,
            @PathVariable Long logId,
            @RequestBody FishLogRequestDto requestDto) {
        // aquariumId와 fishId를 requestDto에 설정
        requestDto.setAquariumId(aquariumId);
        requestDto.setFishId(fishId);
        FishLogResponseDto responseDto = fishLogService.updateLog(logId, requestDto);
        return ResponseEntity.ok(responseDto);
    }
    
    // Delete - 로그 삭제
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable Long aquariumId,
            @PathVariable Long fishId,
            @PathVariable Long logId) {
        fishLogService.deleteLog(logId);
        return ResponseEntity.noContent().build();
    }
}
