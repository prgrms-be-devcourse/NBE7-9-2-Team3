package org.example.backend.domain.aquarium.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumLogRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumLogResponseDto;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.entity.AquariumLog;
import org.example.backend.domain.aquarium.repository.AquariumLogRepository;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AquariumLogService {

    private final AquariumLogRepository aquariumLogRepository;
    private final AquariumRepository aquariumRepository;

    // Create - 로그 생성
    @Transactional
    public AquariumLogResponseDto createLog(AquariumLogRequestDto requestDto) {
        // 어항 엔티티 조회
        Aquarium aquarium = aquariumRepository.findById(requestDto.getAquariumId())
                .orElseThrow(() -> new IllegalArgumentException("어항을 찾을 수 없습니다. ID: " + requestDto.getAquariumId()));
        
        AquariumLog aquariumLog = AquariumLog.builder()
                .aquarium(aquarium)
                .temperature(requestDto.getTemperature())
                .ph(requestDto.getPh())
                .logDate(requestDto.getLogDate())
                .build();

        AquariumLog savedLog = aquariumLogRepository.save(aquariumLog);
        return AquariumLogResponseDto.from(savedLog);
    }

    // Read - 모든 로그 조회
    public List<AquariumLogResponseDto> getAllLogs() {
        return aquariumLogRepository.findAll().stream()
                .map(AquariumLogResponseDto::from)
                .collect(Collectors.toList());
    }

    // Read - 특정 로그 조회
    public AquariumLogResponseDto getLogById(Long logId) {
        AquariumLog aquariumLog = aquariumLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다. ID: " + logId));
        return AquariumLogResponseDto.from(aquariumLog);
    }

    // Read - aquariumId로 로그 조회
    public List<AquariumLogResponseDto> getLogsByAquariumId(Long aquariumId) {
        return aquariumLogRepository.findByAquariumId(aquariumId).stream()
                .map(AquariumLogResponseDto::from)
                .collect(Collectors.toList());
    }

    // Update - 로그 수정
    @Transactional
    public AquariumLogResponseDto updateLog(Long logId, AquariumLogRequestDto requestDto) {
        AquariumLog aquariumLog = aquariumLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다. ID: " + logId));

        // 어항 엔티티 조회
        Aquarium aquarium = aquariumRepository.findById(requestDto.getAquariumId())
                .orElseThrow(() -> new IllegalArgumentException("어항을 찾을 수 없습니다. ID: " + requestDto.getAquariumId()));

        aquariumLog.setAquarium(aquarium);
        aquariumLog.setTemperature(requestDto.getTemperature());
        aquariumLog.setPh(requestDto.getPh());
        aquariumLog.setLogDate(requestDto.getLogDate());

        return AquariumLogResponseDto.from(aquariumLog);
    }

    // Delete - 로그 삭제
    @Transactional
    public void deleteLog(Long logId) {
        if (!aquariumLogRepository.existsById(logId)) {
            throw new IllegalArgumentException("로그를 찾을 수 없습니다. ID: " + logId);
        }
        aquariumLogRepository.deleteById(logId);
    }
}
