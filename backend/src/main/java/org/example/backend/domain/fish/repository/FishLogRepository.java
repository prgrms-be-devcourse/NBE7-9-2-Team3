package org.example.backend.domain.fish.repository;

import org.example.backend.domain.fish.entity.FishLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FishLogRepository extends JpaRepository<FishLog, Long> {
    
    // fishId로 로그 조회
    List<FishLog> findByFishId(Long fishId);
    
    // aquariumId와 fishId로 로그 조회 (특정 어항의 특정 물고기)
    List<FishLog> findByAquariumIdAndFishId(Long aquariumId, Long fishId);
}
