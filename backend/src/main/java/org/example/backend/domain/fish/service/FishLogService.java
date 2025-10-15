package org.example.backend.domain.fish.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.FishLogRequestDto;
import org.example.backend.domain.fish.dto.FishLogResponseDto;
import org.example.backend.domain.fish.entity.FishLog;
import org.example.backend.domain.fish.repository.FishLogRepository;
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
    
    // Create - 물고기 로그 생성
    @Transactional
    public FishLogResponseDto createLog(FishLogRequestDto requestDto) {
        FishLog fishLog = FishLog.builder()
                .aquariumId(requestDto.getAquariumId())
                .fishId(requestDto.getFishId())
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
                .orElseThrow(() -> new IllegalArgumentException("물고기 로그를 찾을 수 없습니다. ID: " + logId));
        return FishLogResponseDto.from(fishLog);
    }
    
    // Read - fishId로 물고기 로그 조회
    public List<FishLogResponseDto> getLogsByFishId(Long fishId) {
        return fishLogRepository.findByFishId(fishId).stream()
                .map(FishLogResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // Read - aquariumId와 fishId로 물고기 로그 조회 (특정 어항의 특정 물고기)
    public List<FishLogResponseDto> getLogsByAquariumIdAndFishId(Long aquariumId, Long fishId) {
        return fishLogRepository.findByAquariumIdAndFishId(aquariumId, fishId).stream()
                .map(FishLogResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // Update - 물고기 로그 수정
    @Transactional
    public FishLogResponseDto updateLog(Long logId, FishLogRequestDto requestDto) {
        FishLog fishLog = fishLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("물고기 로그를 찾을 수 없습니다. ID: " + logId));

        fishLog.setAquariumId(requestDto.getAquariumId());
        fishLog.setFishId(requestDto.getFishId());
        fishLog.setStatus(requestDto.getStatus());
        fishLog.setLogDate(requestDto.getLogDate());

        return FishLogResponseDto.from(fishLog);
    }
    
    // Delete - 물고기 로그 삭제
    @Transactional
    public void deleteLog(Long logId) {
        FishLog fishLog = fishLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("물고기 로그를 찾을 수 없습니다. ID: " + logId));
        fishLogRepository.delete(fishLog);
    }
}
