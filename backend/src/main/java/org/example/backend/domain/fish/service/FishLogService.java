package org.example.backend.domain.fish.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.entity.FishLog;
import org.example.backend.domain.fish.repository.FishLogRepository;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FishLogService {
    
    private final FishLogRepository fishLogRepository;
    private final FishRepository fishRepository;
    
    // Create - 물고기 로그 생성
    @Transactional
    public FishLogResponseDto createLog(FishLogRequestDto requestDto) {
        // 물고기 엔티티 조회
        Fish fish = fishRepository.findById(requestDto.getFishId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FISH_NOT_FOUND));
        
        FishLog fishLog = FishLog.builder()
                .fish(fish)
                .status(requestDto.getStatus())
                .logDate(requestDto.getLogDate() != null ? requestDto.getLogDate() : LocalDateTime.now())
                .build();
        
        FishLog savedLog = fishLogRepository.save(fishLog);
        return FishLogResponseDto.from(savedLog);
    }
    
    // Read - 모든 물고기 로그 조회
    public List<FishLogResponseDto> getAllLogs() {
        return fishLogRepository.findAll().stream()
                .map(FishLogResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // Read - 특정 물고기 로그 조회
    public FishLogResponseDto getLogById(Long logId) {
        FishLog fishLog = fishLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FISH_LOG_NOT_FOUND));
        return FishLogResponseDto.from(fishLog);
    }
    
    // Read - fishId로 물고기 로그 조회
    public List<FishLogResponseDto> getLogsByFishId(Long fishId) {
        return fishLogRepository.findByFishId(fishId).stream()
                .map(FishLogResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // Update - 물고기 로그 수정
    @Transactional
    public FishLogResponseDto updateLog(Long logId, FishLogRequestDto requestDto) {
        FishLog fishLog = fishLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FISH_LOG_NOT_FOUND));

        // 물고기 엔티티 조회
        Fish fish = fishRepository.findById(requestDto.getFishId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FISH_NOT_FOUND));

        fishLog.setFish(fish);
        fishLog.setStatus(requestDto.getStatus());
        fishLog.setLogDate(requestDto.getLogDate());

        return FishLogResponseDto.from(fishLog);
    }
    
    // Delete - 물고기 로그 삭제
    @Transactional
    public void deleteLog(Long logId) {
        FishLog fishLog = fishLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FISH_LOG_NOT_FOUND));
        fishLogRepository.delete(fishLog);
    }
}
