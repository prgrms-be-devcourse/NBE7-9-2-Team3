package org.example.backend.domain.aquarium.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto;
import org.example.backend.domain.aquarium.service.AquariumLogService;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aquarium/{aquariumId}/aquariumLog")
@RequiredArgsConstructor
@Tag(name = "AquariumLog", description = "어항 기록 관리 API")
public class AquariumLogController {

    private final AquariumLogService aquariumLogService;

    // Create - 특정 어항에 로그 생성
    @Operation(summary = "어항 기록 생성", description = "특정 어항 관리를 위한 기록을 생성합니다.")
    @PostMapping
    public ApiResponse<AquariumLogResponseDto> createLog(
            @PathVariable Long aquariumId,
            @RequestBody AquariumLogRequestDto requestDto) {
        // aquariumId를 requestDto에 설정
        requestDto.setAquariumId(aquariumId);
        AquariumLogResponseDto responseDto = aquariumLogService.createLog(requestDto);
        return ApiResponse.ok("어항 기록이 생성되었습니다.", responseDto);
    }

    // Read - 특정 어항의 모든 로그 조회
    @Operation(summary = "어항 기록 목록 조회", description = "특정 어항의 모든 기록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<AquariumLogResponseDto>> getLogsByAquariumId(@PathVariable Long aquariumId) {
        List<AquariumLogResponseDto> logs = aquariumLogService.getLogsByAquariumId(aquariumId);
        return ApiResponse.ok("어항 기록 목록이 조회되었습니다.", logs);
    }

    // Update - 로그 수정
    @Operation(summary = "어항 기록 수정", description = "특정 어항의 특정 기록을 수정합니다.")
    @PutMapping("/{logId}")
    public ApiResponse<AquariumLogResponseDto> updateLog(
            @PathVariable Long aquariumId,
            @PathVariable Long logId,
            @RequestBody AquariumLogRequestDto requestDto) {
        // aquariumId를 requestDto에 설정
        requestDto.setAquariumId(aquariumId);
        AquariumLogResponseDto responseDto = aquariumLogService.updateLog(logId, requestDto);
        return ApiResponse.ok("어항 기록이 수정되었습니다.", responseDto);
    }

    // Delete - 로그 삭제
    @Operation(summary = "어항 기록 삭제", description = "특정 어항의 특정 기록을 삭제합니다.")
    @DeleteMapping("/{logId}")
    public ApiResponse<Void> deleteLog(
            @PathVariable Long aquariumId,
            @PathVariable Long logId) {
        aquariumLogService.deleteLog(logId);
        return ApiResponse.ok("어항 기록이 삭제되었습니다.");
    }
}
